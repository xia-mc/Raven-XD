package keystrokesmod.module.impl.combat;

import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.event.RotationEvent;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.combat.autoclicker.IAutoClicker;
import keystrokesmod.module.impl.combat.autoclicker.LowCPSAutoClicker;
import keystrokesmod.module.impl.combat.autoclicker.NormalAutoClicker;
import keystrokesmod.module.impl.combat.autoclicker.RecordAutoClicker;
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
import keystrokesmod.utility.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class AutoRod extends IAutoClicker {
    private final ModeValue clickMode;
    private final ButtonSetting onlyWhileKillAura;
    private final SliderSetting range;
    private final SliderSetting aimSpeed;
    private final ModeSetting moveFix;
    private final ButtonSetting prediction;
    private final ButtonSetting smart;
    private final SliderSetting predictionTicks;
    private final ButtonSetting drawPos;
    private final ButtonSetting ignoreTeammates;

    private int fromSlot = -1;
    private EntityLivingBase target = null;
    private int predTicks = 0;
    private net.minecraft.util.Vec3 pos = null;
    private Float lastYaw = null, lastPitch = null;

    public AutoRod() {
        super("AutoRod", category.combat);
        this.registerSetting(clickMode = new ModeValue("Click mode", this)
                .add(new LowCPSAutoClicker("Normal", this, false, true))
                .add(new NormalAutoClicker("NormalFast", this, false, true))
                .add(new RecordAutoClicker("Record", this, false, true))
        );
        this.registerSetting(onlyWhileKillAura = new ButtonSetting("Only while killAura", false));
        this.registerSetting(range = new SliderSetting("Range", 10, 5, 15, 0.1, () -> !onlyWhileKillAura.isToggled()));
        this.registerSetting(aimSpeed = new SliderSetting("Aim speed", 10, 5, 20, 0.1, () -> !onlyWhileKillAura.isToggled()));
        this.registerSetting(moveFix = new ModeSetting("MoveFix", RotationHandler.MoveFix.MODES, 0));
        this.registerSetting(prediction = new ButtonSetting("Prediction", false));
        this.registerSetting(smart = new ButtonSetting("Smart", true, prediction::isToggled));
        this.registerSetting(predictionTicks = new SliderSetting("Prediction ticks", 2, 0, 10, 1, "ticks", () -> prediction.isToggled() && !smart.isToggled()));
        this.registerSetting(drawPos = new ButtonSetting("Draw pos", false, prediction::isToggled));
        this.registerSetting(ignoreTeammates = new ButtonSetting("Ignore teammates", true));
    }

    @Override
    public void onEnable() {
        clickMode.enable();

        fromSlot = -1;
        pos = null;
        lastYaw = lastPitch = null;
    }

    @Override
    public void onDisable() {
        clickMode.disable();
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        target = null;
        if (KillAura.target != null && !ModuleManager.killAura.isAttack()) {
            target = KillAura.target;
        } else {
            mc.theWorld.playerEntities.parallelStream()
                    .filter(p -> p != mc.thePlayer)
                    .filter(p -> !AntiBot.isBot(p))
                    .filter(p -> !ignoreTeammates.isToggled() || !Utils.isTeamMate(p))
                    .filter(p -> !Utils.isFriended(p))
                    .filter(this::notBehindWall)
                    .min(Comparator.comparingDouble(p -> mc.thePlayer.getDistanceToEntity(p)))
                    .filter(p -> mc.thePlayer.getDistanceToEntity(p) <= range.getInput())
                    .ifPresent(p -> target = p);
        }

        if (target != null) {
            int slot = getRod();
            if (slot == -1) return;
            if (fromSlot == -1)
                fromSlot = SlotHandler.getCurrentSlot();
            SlotHandler.setCurrentSlot(slot);
        }
    }

    private boolean notBehindWall(EntityLivingBase entity) {
        Vec3 target = Utils.getEyePos(entity);
        Vec3 from = Utils.getEyePos();

        return RotationUtils.rayCast(target.distanceTo(from), PlayerRotation.getYaw(target), PlayerRotation.getPitch(target)) == null;
    }

    private int getRod() {
        int a = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack getStackInSlot = mc.thePlayer.inventory.getStackInSlot(i);
            if (getStackInSlot != null && getStackInSlot.getItem() == Items.fishing_rod) {
                a = i;
                break;
            }
        }
        return a;
    }

    @SubscribeEvent
    public void onRotation(RotationEvent event) {
        if (target == null) {
            lastYaw = lastPitch = null;
            pos = null;
        } else {
            if (lastYaw == null || lastPitch == null) {
                lastYaw = event.getYaw();
                lastPitch = event.getPitch();
            }

            Vec3 hitPos = getHitPos(target, target.hurtTime > 0
                    ? new Vec3(target.motionX, target.motionY, target.motionZ)
                    : new Vec3(target.posX - target.lastTickPosX, target.posY - target.lastTickPosY, target.posZ - target.lastTickPosZ));
            event.setYaw(lastYaw = AimSimulator.rotMove(PlayerRotation.getYaw(hitPos), lastYaw, (float) aimSpeed.getInput()));
            event.setPitch(lastPitch = AimSimulator.rotMove(PlayerRotation.getPitch(hitPos), lastPitch, (float) aimSpeed.getInput()));
            event.setMoveFix(RotationHandler.MoveFix.values()[(int) moveFix.getInput()]);
            pos = new net.minecraft.util.Vec3(hitPos.x, hitPos.y - target.getEyeHeight(), hitPos.z);
        }
    }

    private @NotNull Vec3 getHitPos(@NotNull EntityLivingBase entity, Vec3 motion) {
        Vec3 result = Utils.getEyePos(entity);

        return MoveUtil.predictedPos(entity, motion, result, predTicks);
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (drawPos.isToggled() && prediction.isToggled() && pos != null) {
            Blink.drawBox(pos);
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
    public boolean click() {
        ItemStack item = SlotHandler.getHeldItem();
        if (item != null && item.getItem() instanceof ItemFishingRod && target != null) {
            if (item.getItemUseAction() == EnumAction.BOW) {  // threw
                if (target.hurtTime == 0)
                    return false;
                target = null;
            }

            Utils.sendClick(1, true);
            Utils.sendClick(1, false);
            return true;
        } else {
            if (fromSlot != -1) {
                SlotHandler.setCurrentSlot(fromSlot);
                fromSlot = -1;
            }
            return false;
        }
    }
}
