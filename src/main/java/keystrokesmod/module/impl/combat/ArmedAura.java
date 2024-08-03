package keystrokesmod.module.impl.combat;

import akka.japi.Pair;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.event.RotationEvent;
import keystrokesmod.module.impl.client.Notifications;
import keystrokesmod.module.impl.combat.autoclicker.DragClickAutoClicker;
import keystrokesmod.module.impl.combat.autoclicker.IAutoClicker;
import keystrokesmod.module.impl.combat.autoclicker.NormalAutoClicker;
import keystrokesmod.module.impl.combat.autoclicker.RecordAutoClicker;
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
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.RotationUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ArmedAura extends IAutoClicker {
    private final ModeValue clickMode;
    private final SliderSetting range;
    private final ButtonSetting perfect;
    private final ModeSetting moveFix;
    private final ButtonSetting prediction;
    private final ButtonSetting smart;
    private final SliderSetting predictionTicks;
    private final ButtonSetting drawPos;
    private final ButtonSetting autoSwitch;
    private final ButtonSetting fastFire;
    private final SliderSetting fastFireAmount;
    private final ButtonSetting targetInvisible;
    private final ButtonSetting ignoreTeammates;
    private final ButtonSetting notWhileKillAura;

    private boolean targeted = false;
    private Pair<Pair<EntityPlayer, Vec3>, Triple<Double, Float, Float>> target = null;
    private boolean click = false;
    private int predTicks = 0;
    private net.minecraft.util.Vec3 pos = null;
    private final Set<Integer> fired = new HashSet<>();

    public ArmedAura() {
        super("ArmedAura", category.combat);
        this.registerSetting(clickMode = new ModeValue("Click mode", this)
                .add(new NormalAutoClicker("Normal", this, false, true))
                .add(new DragClickAutoClicker("Drag Click", this, false, true))
                .add(new RecordAutoClicker("Record", this, false, true))
                .setDefaultValue("Normal")
        );
        this.registerSetting(range = new SliderSetting("Range", 50, 0, 100, 5));
        this.registerSetting(moveFix = new ModeSetting("Move fix", RotationHandler.MoveFix.MODES, 0));
        this.registerSetting(perfect = new ButtonSetting("Perfect", false));
        this.registerSetting(prediction = new ButtonSetting("Prediction", false));
        this.registerSetting(smart = new ButtonSetting("Smart", true, prediction::isToggled));
        this.registerSetting(predictionTicks = new SliderSetting("Prediction ticks", 2, 0, 10, 1, "ticks", () -> prediction.isToggled() && !smart.isToggled()));
        this.registerSetting(drawPos = new ButtonSetting("Draw pos", false, prediction::isToggled));
        this.registerSetting(autoSwitch = new ButtonSetting("Auto switch", true));
        this.registerSetting(fastFire = new ButtonSetting("Fast fire", false, autoSwitch::isToggled));
        this.registerSetting(fastFireAmount = new SliderSetting("Fast fire amount", 1, 1, 4, 1, () -> autoSwitch.isToggled() && fastFire.isToggled()));
        this.registerSetting(targetInvisible = new ButtonSetting("Target invisible", false));
        this.registerSetting(ignoreTeammates = new ButtonSetting("Ignore teammates", true));
        this.registerSetting(notWhileKillAura = new ButtonSetting("Not while killAura", true));
    }

    @SubscribeEvent
    public void onRotation(RotationEvent event) {
        if (!targeted && !(perfect.isToggled() && mc.thePlayer.experience % 1 != 0) && !(notWhileKillAura.isToggled() && KillAura.target != null)) {
            final Optional<Pair<Pair<EntityPlayer, Vec3>, Triple<Double, Float, Float>>> target = mc.theWorld.playerEntities.parallelStream()
                    .filter(p -> p != mc.thePlayer)
                    .filter(p -> !AntiBot.isBot(p))
                    .filter(p -> !Utils.isTeamMate(p) || !ignoreTeammates.isToggled())
                    .filter(entity -> targetInvisible.isToggled() || !entity.isInvisible())
                    .filter(p -> p.getDistanceToEntity(mc.thePlayer) < range.getInput())
                    .map(p -> new Pair<>(p, doPrediction(Utils.getEyePos(p), new Vec3(p.motionX, p.motionY, p.motionZ))))
                    .map(pair -> new Pair<>(pair, Triple.of(pair.second().distanceTo(Utils.getEyePos()), PlayerRotation.getYaw(pair.second()), PlayerRotation.getPitch(pair.second()))))
                    .filter(pair -> RotationUtils.rayCast(pair.second().getLeft(), pair.second().getMiddle(), pair.second().getRight()) == null)
                    .min(Comparator.comparingDouble(pair -> mc.thePlayer.getDistanceSqToEntity(pair.first().first())));
            if (target.isPresent()) {
                if (SlotHandler.getHeldItem() != null && SlotHandler.getHeldItem().getItem() instanceof ItemHoe) {
                    event.setYaw(target.get().second().getMiddle());
                    event.setPitch(target.get().second().getRight());
                    event.setMoveFix(RotationHandler.MoveFix.values()[(int) moveFix.getInput()]);
                    pos = target.get().first().second().add(0, -target.get().first().first().getEyeHeight(), 0).toVec3();
                    this.target = target.get();
                }
                targeted = true;
            } else {
                targeted = false;
                pos = null;
            }
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
        if (targeted && autoSwitch.isToggled()) {
            int bestArm = getBestArm();
            SlotHandler.setCurrentSlot(bestArm);
        }

        if (SlotHandler.getHeldItem() == null || !(SlotHandler.getHeldItem().getItem() instanceof ItemHoe)) {
            targeted = false;
            return;
        }

        if (targeted && click) {
            mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, SlotHandler.getHeldItem());
            if (fastFire.isToggled() && autoSwitch.isToggled()) {
                for (int i = 0; i < (int) fastFireAmount.getInput(); i++) {
                    int bestArm = getBestArm();
                    SlotHandler.setCurrentSlot(bestArm);
                    mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, SlotHandler.getHeldItem());
                }
            }
            if (target != null)
                HitLog.onAttack(predTicks, target.first().first(), Utils.getEyePos(target.first().first()), new Vec3(mc.thePlayer), RotationHandler.getRotationYaw(), RotationHandler.getRotationPitch());
            targeted = false;
        }
    }

    private int getBestArm() {
        int arm = -1;
        int level = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack != null && stack.getItem() instanceof ItemHoe) {
                if (fastFire.isToggled() && fired.contains(i))
                    continue;

                int curLevel;
                String name = ((ItemHoe) stack.getItem()).getMaterialName().toLowerCase();
                switch (name) {
                    default:
                    case "wood":
                        curLevel = 1;
                        break;
                    case "stone":
                        curLevel = 2;
                        break;
                    case "iron":
                        curLevel = 3;
                        break;
                    case "gold":
                        curLevel = 4;
                        break;
                    case "diamond":
                        curLevel = 5;
                        break;
                }

                if (curLevel > level) {
                    level = curLevel;
                    arm = i;
                }
            }
        }

        if (arm == -1 && !fired.isEmpty()) {
            fired.clear();
            return getBestArm();
        }
        fired.add(arm);
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

    private @NotNull Vec3 doPrediction(@NotNull Vec3 pos, Vec3 motion) {
        Vec3 result = new Vec3(pos.toVec3());
        for (int i = 0; i < predTicks; i++) {
            result = result.add(
                    MoveUtil.predictedMotionXZ(motion.x(), i),
                    mc.thePlayer.onGround ? 0 : MoveUtil.predictedMotion(motion.y(), i),
                    MoveUtil.predictedMotionXZ(motion.z(), i)
            );
        }
        return result;
    }

    @Override
    public void onEnable() {
        clickMode.enable();
        targeted = false;
        click = false;
        predTicks = 0;
        fired.clear();
        pos = null;
    }

    @Override
    public void onDisable() {
        clickMode.disable();
    }

    @Override
    public boolean click() {
        click = true;
        return targeted;
    }
}
