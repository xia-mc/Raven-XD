package keystrokesmod.module.impl.combat;

import akka.japi.Pair;
import keystrokesmod.Raven;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.event.RotationEvent;
import keystrokesmod.module.impl.combat.autoclicker.*;
import keystrokesmod.module.impl.combat.ragebot.IRageBotFeature;
import keystrokesmod.module.impl.combat.ragebot.nospread.LegitNoSpread;
import keystrokesmod.module.impl.combat.ragebot.nospread.SwitchNoSpread;
import keystrokesmod.module.impl.combat.ragebot.rapidfire.*;
import keystrokesmod.module.impl.fun.HitLog;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.impl.other.anticheats.utils.world.PlayerRotation;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.*;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;

public class RageBot extends IAutoClicker {
    private final ModeValue clickMode;
    private final ModeSetting mode;
    private final SliderSetting switchDelay;
    private final ModeSetting sortMode;
    public final ModeSetting weaponMode;
    public final ModeSetting priorityHitBox;
    private final SliderSetting range;
    private final SliderSetting fov;
    private final ButtonSetting perfect;
    private final ModeSetting moveFix;
    private final ButtonSetting prediction;
    private final ButtonSetting smart;
    private final SliderSetting predictionTicks;
    private final ButtonSetting autoSwitch;
    private final ButtonSetting lookView;
    private final ModeValue rapidFire;
    private final ModeValue noSpread;
    private final ButtonSetting targetPlayers;
    private final ButtonSetting targetEntities;
    private final ButtonSetting targetInvisible;
    private final ButtonSetting ignoreTeammates;
    private final ButtonSetting ignoreTeammatesCSGO;
    private final ButtonSetting notWhileKillAura;

    public boolean targeted = false;
    private boolean armed = false;
    public Pair<Pair<EntityLivingBase, Vec3>, Triple<Double, Float, Float>> target = null;
    private int predTicks = 0;
    private final Set<EntityLivingBase> switchedTarget = new HashSet<>();
    private long lastSwitched = -1;

    public RageBot() {
        super("RageBot", category.combat, "Auto-aim and fire like CS2 cheats");
        this.registerSetting(clickMode = new ModeValue("Click mode", this)
                .add(new LowCPSAutoClicker("Normal", this, false, true))
                .add(new NormalAutoClicker("NormalFast", this, false, true))
                .add(new RecordAutoClicker("Record", this, false, true))
                .setDefaultValue("Normal")
        );
        this.registerSetting(mode = new ModeSetting("Mode", new String[]{"Single", "Switch"}, 0));
        this.registerSetting(switchDelay = new SliderSetting("Switch delay", 200, 50, 1000, 50, "ms", new ModeOnly(mode, 1)));
        this.registerSetting(sortMode = new ModeSetting("Sort mode", new String[]{"Distance", "Health", "Hurt time", "Yaw", "Hypixel Zombie"}, 0));
        this.registerSetting(weaponMode = new ModeSetting("Weapon mode", new String[]{"Hypixel BedWars", "Hypixel Zombie", "CubeCraft", "CS:GO"}, 0));
        this.registerSetting(priorityHitBox = new ModeSetting("Priority hit box", Arrays.stream(HitLog.HitPos.values()).map(HitLog.HitPos::getEnglish).toArray(String[]::new), 0));
        this.registerSetting(range = new SliderSetting("Range", 50, 0, 100, 5));
        this.registerSetting(fov = new SliderSetting("FOV", 360, 40, 360, 5));
        this.registerSetting(moveFix = new ModeSetting("Move fix", RotationHandler.MoveFix.MODES, 0));
        this.registerSetting(perfect = new ButtonSetting("Perfect", false));
        this.registerSetting(prediction = new ButtonSetting("Prediction", false));
        this.registerSetting(smart = new ButtonSetting("Smart", true, prediction::isToggled));
        this.registerSetting(predictionTicks = new SliderSetting("Prediction ticks", 2, 0, 10, 1, "ticks", () -> prediction.isToggled() && !smart.isToggled()));
        this.registerSetting(autoSwitch = new ButtonSetting("Auto switch", true));
        this.registerSetting(lookView = new ButtonSetting("Look view", false));
        this.registerSetting(rapidFire = new ModeValue("Rapid fire", this)
                .add(new IRapidFire("Disabled", this))
                .add(new LegitRapidFire("Legit", this))
                .add(new PacketRapidFire("Packet", this))
                .add(new StoreRapidFire("Store", this))
                .add(new TimerRapidFire("Timer", this))
        );
        this.registerSetting(noSpread = new ModeValue("No spread", this)
                .add(new IRageBotFeature("Disabled", this))
                .add(new LegitNoSpread("Legit", this))
                .add(new SwitchNoSpread("Switch", this))
        );
        this.registerSetting(targetPlayers = new ButtonSetting("Target players", true));
        this.registerSetting(targetEntities = new ButtonSetting("Target entities", false));
        this.registerSetting(targetInvisible = new ButtonSetting("Target invisible", false));
        this.registerSetting(ignoreTeammates = new ButtonSetting("Ignore teammates", true));
        this.registerSetting(ignoreTeammatesCSGO = new ButtonSetting("Ignore teammates CSGO", false, ignoreTeammates::isToggled));
        this.registerSetting(notWhileKillAura = new ButtonSetting("Not while killAura", true));
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event) {
        if (Raven.debugger) {
            ItemStack stack = mc.thePlayer.inventory.armorInventory[3];
            Utils.sendMessage(String.valueOf(((ItemArmor) stack.getItem()).getColor(stack)));
        }
    }

    @SubscribeEvent
    public void onRotation(RotationEvent event) {
        if (!(perfect.isToggled() && mc.thePlayer.experience % 1 != 0) && !(notWhileKillAura.isToggled() && KillAura.target != null)) {
            final Vec3 eyePos = Utils.getEyePos();
            final Optional<Pair<Pair<EntityLivingBase, Vec3>, Triple<Double, Float, Float>>> target = mc.theWorld.loadedEntityList.parallelStream()
                    .filter(entity -> entity instanceof EntityLivingBase)
                    .map(entity -> (EntityLivingBase) entity)
                    .filter(entity -> entity != mc.thePlayer)
                    .filter(entity -> !(mode.getInput() == 1 && switchedTarget.contains(entity)))
                    .filter(entity -> {
                        if (entity instanceof EntityArmorStand) return false;
                        if (entity instanceof EntityPlayer) {
                            if (!targetPlayers.isToggled()) return false;
                            if (Utils.isFriended((EntityPlayer) entity)) {
                                return false;
                            }
                            if (entity.deathTime != 0) {
                                return false;
                            }
                            if (ignoreTeammates.isToggled()) {
                                if (ignoreTeammatesCSGO.isToggled()) {
                                    if (RageBotUtils.isTeammateCSGO((EntityPlayer) entity))
                                        return false;
                                } else {
                                    if (Utils.isTeamMate(entity))
                                        return false;
                                }
                            }
                            AxisAlignedBB box = entity.getCollisionBoundingBox();
                            if (box == null || box.maxY - box.minY < 1)
                                return false;
                            return !AntiBot.isBot(entity);
                        } else return targetEntities.isToggled();
                    })
                    .filter(entity -> targetInvisible.isToggled() || !entity.isInvisible())
                    .filter(p -> p.getDistanceToEntity(mc.thePlayer) < range.getInput())
                    .filter(p -> fov.getInput() == 360 || Utils.inFov((float) fov.getInput(), p))
                    .map(p -> new Pair<>(p, RageBotUtils.getHitPos(p, predTicks)))
                    .filter(p -> p.second() != null)
                    .map(pair -> new Pair<>(pair, Triple.of(pair.second().distanceTo(eyePos), PlayerRotation.getYaw(pair.second()), PlayerRotation.getPitch(pair.second()))))
                    .min(fromSortMode());
            if (target.isPresent()) {
                if (armed) {
                    event.setYaw(target.get().second().getMiddle());
                    event.setPitch(target.get().second().getRight());
                    event.setMoveFix(RotationHandler.MoveFix.values()[(int) moveFix.getInput()]);
                    if (lookView.isToggled()) {
                        mc.thePlayer.rotationYaw = target.get().second().getMiddle();
                        mc.thePlayer.rotationPitch = target.get().second().getRight();
                    }
                    this.target = target.get();
                    long time = System.currentTimeMillis();
                    if (time - lastSwitched > switchDelay.getInput()) {
                        switchedTarget.add(target.get().first().first());
                        lastSwitched = time;
                    }
                }
                targeted = true;
            } else {
                if (!switchedTarget.isEmpty()) {
                    switchedTarget.clear();
                    onRotation(event);
                }
                targeted = false;
            }
        }
    }

    private Comparator<Pair<Pair<EntityLivingBase, Vec3>, Triple<Double, Float, Float>>> fromSortMode() {
        switch ((int) sortMode.getInput()) {
            default:
            case 0:
                return Comparator.comparingDouble(pair -> mc.thePlayer.getDistanceSqToEntity(pair.first().first()));
            case 1:
                return Comparator.comparingDouble(pair -> pair.first().first().getHealth());
            case 2:
                return Comparator.comparingDouble(pair -> pair.first().first().hurtTime);
            case 3:
                return Comparator.comparingDouble(pair -> Utils.getFov(pair.first().first().posX, pair.first().first().posZ));
            case 4:
                return (o1, o2) -> {
                    final EntityLivingBase first = o1.first().first();
                    final EntityLivingBase second = o2.first().first();

                    if (first == second) return 0;
                    if (second instanceof EntityGiantZombie) {
                        return 1;
                    }
                    if (first instanceof EntityGiantZombie) {
                        return -1;
                    }
                    if (second instanceof EntityZombie) {
                        if (second.isChild()) return 1;
                    }
                    if (first instanceof EntityZombie) {
                        if (first.isChild()) return -1;
                    }
                    return Double.compare(first.getHealth(), second.getHealth());
                };
        }
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        if (targeted && autoSwitch.isToggled()) {
            int bestArm = ((IRapidFire) rapidFire.getSelected()).getBestArm();
            SlotHandler.setCurrentSlot(bestArm);
        }

        if (isArmed()) {
            armed = true;
        } else {
            targeted = false;
            armed = false;
        }
    }

    private boolean isArmed() {
        ItemStack stack = SlotHandler.getHeldItem();
        if (stack == null) return false;

        Item item = stack.getItem();

        switch ((int) weaponMode.getInput()) {
            default:
            case 0:
                return RageBotUtils.isArmHypixelBedWars(item);
            case 1:
                return RageBotUtils.isArmHypixelZombie(item);
            case 2:
                return RageBotUtils.isArmCubeCraft(item);
            case 3:
                return RageBotUtils.isArmCSGO(item);
        }
    }

    @Override
    public void onUpdate() {
        if (prediction.isToggled()) {
            if (smart.isToggled()) {
                predTicks = (int) Math.floor(mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID()).getResponseTime() / 50.0);
            } else {
                predTicks = (int) predictionTicks.getInput();
            }
        } else {
            predTicks = 0;
        }
    }

    @Override
    public void onEnable() {
        clickMode.enable();
        rapidFire.enable();
        noSpread.enable();
        targeted = false;
        predTicks = 0;
    }

    @Override
    public void onDisable() {
        clickMode.disable();
        rapidFire.disable();
        noSpread.disable();
        Utils.sendClick(1, false);
    }

    @Override
    public boolean click() {
        if (targeted && armed) {
            mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, SlotHandler.getHeldItem());
            ((IRapidFire) rapidFire.getSelected()).onFire();
            ((IRageBotFeature) noSpread.getSelected()).onFire();
            if (target != null)
                HitLog.onAttack(predTicks, target.first().first(), Utils.getEyePos(target.first().first()), new Vec3(mc.thePlayer), RotationHandler.getRotationYaw(), RotationHandler.getRotationPitch());
            targeted = false;
            return true;
        } else {
            Utils.sendClick(1, false);
        }
        return false;
    }
}
