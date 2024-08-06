package keystrokesmod.module.impl.combat;

import akka.japi.Pair;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.event.RotationEvent;
import keystrokesmod.module.impl.combat.autoclicker.*;
import keystrokesmod.module.impl.fun.HitLog;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.impl.other.anticheats.utils.world.PlayerRotation;
import keystrokesmod.module.impl.player.Blink;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.ModeValue;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemHoe;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ArmedAura extends IAutoClicker {
    private final ModeValue clickMode;
    private final ModeSetting mode;
    private final SliderSetting switchDelay;
    private final ModeSetting sortMode;
    private final ModeSetting weaponMode;
    private final ModeSetting priorityHitBox;
    private final SliderSetting range;
    private final SliderSetting fov;
    private final ButtonSetting perfect;
    private final ModeSetting moveFix;
    private final ButtonSetting prediction;
    private final ButtonSetting smart;
    private final SliderSetting predictionTicks;
    private final ButtonSetting drawPos;
    private final ButtonSetting autoSwitch;
    private final ButtonSetting lookView;
    private final ButtonSetting rapidFire;
    private final ButtonSetting rapidFireLegit;
    private final SliderSetting rapidFireAmount;
    private final ButtonSetting targetPlayers;
    private final ButtonSetting targetEntities;
    private final ButtonSetting targetInvisible;
    private final ButtonSetting ignoreTeammates;
    private final ButtonSetting notWhileKillAura;

    private boolean targeted = false;
    private Pair<Pair<EntityLivingBase, Vec3>, Triple<Double, Float, Float>> target = null;
    private int predTicks = 0;
    private net.minecraft.util.Vec3 pos = null;
    private final Set<Integer> firedSlots = new HashSet<>();
    private final Set<EntityLivingBase> switchedTarget = new HashSet<>();
    private long lastSwitched = -1;

    public ArmedAura() {
        super("ArmedAura", category.combat);
        this.registerSetting(clickMode = new ModeValue("Click mode", this)
                .add(new LowCPSAutoClicker("Normal", this, false, true))
                .add(new NormalAutoClicker("NormalFast", this, false, true))
                .add(new RecordAutoClicker("Record", this, false, true))
                .setDefaultValue("Normal")
        );
        this.registerSetting(mode = new ModeSetting("Mode", new String[]{"Single", "Switch"}, 0));
        this.registerSetting(switchDelay = new SliderSetting("Switch delay", 200, 50, 1000, 50, "ms", new ModeOnly(mode, 1)));
        this.registerSetting(sortMode = new ModeSetting("Sort mode", new String[]{"Distance", "Health", "Hurt time", "Yaw", "Hypixel Zombie"}, 0));
        this.registerSetting(weaponMode = new ModeSetting("Weapon mode", new String[]{"Hypixel BedWars", "Hypixel Zombie", "CubeCraft"}, 0));
        this.registerSetting(priorityHitBox = new ModeSetting("Priority hit box", Arrays.stream(HitLog.HitPos.values()).map(HitLog.HitPos::getEnglish).toArray(String[]::new), 0));
        this.registerSetting(range = new SliderSetting("Range", 50, 0, 100, 5));
        this.registerSetting(fov = new SliderSetting("FOV", 360, 40, 360, 5));
        this.registerSetting(moveFix = new ModeSetting("Move fix", RotationHandler.MoveFix.MODES, 0));
        this.registerSetting(perfect = new ButtonSetting("Perfect", false));
        this.registerSetting(prediction = new ButtonSetting("Prediction", false));
        this.registerSetting(smart = new ButtonSetting("Smart", true, prediction::isToggled));
        this.registerSetting(predictionTicks = new SliderSetting("Prediction ticks", 2, 0, 10, 1, "ticks", () -> prediction.isToggled() && !smart.isToggled()));
        this.registerSetting(drawPos = new ButtonSetting("Draw pos", false, prediction::isToggled));
        this.registerSetting(autoSwitch = new ButtonSetting("Auto switch", true));
        this.registerSetting(lookView = new ButtonSetting("Look view", false));
        this.registerSetting(rapidFire = new ButtonSetting("Fast fire", false, autoSwitch::isToggled));
        this.registerSetting(rapidFireLegit = new ButtonSetting("Fast fire Legit", false, () -> autoSwitch.isToggled() && rapidFire.isToggled()));
        this.registerSetting(rapidFireAmount = new SliderSetting("Fast fire amount", 1, 1, 4, 1, () -> autoSwitch.isToggled() && rapidFire.isToggled() && !rapidFireLegit.isToggled()));
        this.registerSetting(targetPlayers = new ButtonSetting("Target players", true));
        this.registerSetting(targetEntities = new ButtonSetting("Target entities", false));
        this.registerSetting(targetInvisible = new ButtonSetting("Target invisible", false));
        this.registerSetting(ignoreTeammates = new ButtonSetting("Ignore teammates", true));
        this.registerSetting(notWhileKillAura = new ButtonSetting("Not while killAura", true));
    }

    @SubscribeEvent
    public void onRotation(RotationEvent event) {
        if (!targeted && !(perfect.isToggled() && mc.thePlayer.experience % 1 != 0) && !(notWhileKillAura.isToggled() && KillAura.target != null)) {
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
                            return !AntiBot.isBot(entity) && !(ignoreTeammates.isToggled() && Utils.isTeamMate(entity));
                        } else return targetEntities.isToggled();
                    })
                    .filter(entity -> targetInvisible.isToggled() || !entity.isInvisible())
                    .filter(p -> p.getDistanceToEntity(mc.thePlayer) < range.getInput())
                    .filter(p -> fov.getInput() == 360 || Utils.inFov((float) fov.getInput(), p))
                    .map(p -> new Pair<>(p, getHitPos(p, new Vec3(p.motionX, p.motionY, p.motionZ))))
                    .map(pair -> new Pair<>(pair, Triple.of(pair.second().distanceTo(pair.second()), PlayerRotation.getYaw(pair.second()), PlayerRotation.getPitch(pair.second()))))
                    .filter(pair -> RotationUtils.rayCast(pair.second().getLeft(), pair.second().getMiddle(), pair.second().getRight()) == null)
                    .min(fromSortMode());
            if (target.isPresent()) {
                if (SlotHandler.getHeldItem() != null && SlotHandler.getHeldItem().getItem() instanceof ItemHoe) {
                    if (lookView.isToggled()) {
                        mc.thePlayer.rotationYaw = target.get().second().getMiddle();
                        mc.thePlayer.rotationPitch = target.get().second().getRight();
                    } else {
                        event.setYaw(target.get().second().getMiddle());
                        event.setPitch(target.get().second().getRight());
                        event.setMoveFix(RotationHandler.MoveFix.values()[(int) moveFix.getInput()]);
                    }
                    pos = target.get().first().second().add(0, -target.get().first().first().getEyeHeight(), 0).toVec3();
                    this.target = target.get();
                    long time = System.currentTimeMillis();
                    if (time - lastSwitched > switchDelay.getInput()) {
                        switchedTarget.add(target.get().first().first());
                        lastSwitched = time;
                    }
                    targeted = true;
                }
            } else {
                if (!switchedTarget.isEmpty()) {
                    switchedTarget.clear();
                    onRotation(event);
                }
                targeted = false;
                pos = null;
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
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (drawPos.isToggled() && prediction.isToggled() && pos != null) {
            Blink.drawBox(pos);
        }
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        if (targeted && autoSwitch.isToggled() && !(SlotHandler.getHeldItem() == null || !(SlotHandler.getHeldItem().getItem() instanceof ItemHoe))) {
            int bestArm = getBestArm();
            SlotHandler.setCurrentSlot(bestArm);
        }

        if (SlotHandler.getHeldItem() == null || !(SlotHandler.getHeldItem().getItem() instanceof ItemHoe)) {
            targeted = false;
        }
    }

    private int getBestArm() {
        int arm;
        Set<Integer> ignoreSlots = rapidFire.isToggled() ? firedSlots : Collections.emptySet();

        switch ((int) weaponMode.getInput()) {
            default:
            case 0:
                arm = ArmedAuraUtils.getArmHypixelBedWars(ignoreSlots);
                break;
            case 1:
                arm = ArmedAuraUtils.getArmHypixelZombie(ignoreSlots);
                break;
            case 2:
                arm = ArmedAuraUtils.getArmCubeCraft(ignoreSlots);
                break;
        }

        if (arm == -1 && !firedSlots.isEmpty()) {
            firedSlots.clear();
            return getBestArm();
        }
        firedSlots.add(arm);
        return arm;
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

    private @NotNull Vec3 getHitPos(@NotNull EntityLivingBase entity, Vec3 motion) {
        Vec3 result;

        switch ((int) priorityHitBox.getInput()) {
            default:
            case 0:
                result = Utils.getEyePos(entity);
                break;
            case 1:
                result = Utils.getEyePos(entity).add(0, -1, 0);
                break;
            case 2:
                result = new Vec3(entity).add(0, 0.5, 0);
                break;
        }

        for (int i = 0; i < predTicks; i++) {
            result = result.add(
                    MoveUtil.predictedMotionXZ(motion.x(), i, MoveUtil.isMoving(entity)),
                    entity.onGround || !BlockUtils.replaceable(new BlockPos(result.toVec3())) ? 0 : MoveUtil.predictedMotion(motion.y(), i),
                    MoveUtil.predictedMotionXZ(motion.z(), i, MoveUtil.isMoving(entity))
            );
        }
        return result;
    }

    @Override
    public void onEnable() {
        clickMode.enable();
        targeted = false;
        predTicks = 0;
        firedSlots.clear();
        pos = null;
    }

    @Override
    public void onDisable() {
        clickMode.disable();
    }

    @Override
    public boolean click() {
        if (targeted) {
            Utils.sendClick(1, true);
            if (rapidFire.isToggled() && autoSwitch.isToggled() && !rapidFireLegit.isToggled()) {
                for (int i = 0; i < (int) rapidFireAmount.getInput(); i++) {
                    int bestArm = getBestArm();
                    SlotHandler.setCurrentSlot(bestArm);
                    Utils.sendClick(1, true);
                }
            }
            if (target != null)
                HitLog.onAttack(predTicks, target.first().first(), Utils.getEyePos(target.first().first()), new Vec3(mc.thePlayer), RotationHandler.getRotationYaw(), RotationHandler.getRotationPitch());
            targeted = false;
            return true;
        }
        return false;
    }
}
