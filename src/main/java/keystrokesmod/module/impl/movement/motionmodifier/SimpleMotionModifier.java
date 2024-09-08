package keystrokesmod.module.impl.movement.motionmodifier;

import keystrokesmod.mixins.impl.entity.EntityAccessor;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;

public class SimpleMotionModifier extends SubMode<Module> {
    private final ButtonSetting editTick;
    private final ButtonSetting notWhileKB;
    private final ButtonSetting onlyInWater;
    private final ButtonSetting onlyInWeb;
    private final ButtonSetting onlyAtLadder;
    private final ButtonSetting jump;
    private final ButtonSetting editStaticY;
    private final SliderSetting editStaticYAmount;
    private final ButtonSetting editStaticXZ;
    private final SliderSetting editStaticXZAmount;
    private final ButtonSetting editAddY;
    private final SliderSetting editAddYAmount;
    private final ButtonSetting editAddXZ;
    private final SliderSetting editAddXZAmount;
    private final ButtonSetting editMultiplyY;
    private final SliderSetting editMultiplyYAmount;
    private final ButtonSetting editMultiplyXZ;
    private final SliderSetting editMultiplyXZAmount;
    private final ButtonSetting editLimitY;
    private final SliderSetting editLimitYAmount;
    private final ButtonSetting editLimitXZ;
    private final SliderSetting editLimitXZAmount;
    private final ButtonSetting editTimerSpeed;
    private final SliderSetting timerSpeedAmount;
    private final ButtonSetting strafe;

    public SimpleMotionModifier(String name, Module module) {
        super(name, module);
        this.registerSetting(editTick = new ButtonSetting("Edit tick", false));
        this.registerSetting(notWhileKB = new ButtonSetting("Not while kb", false, this::isEdit));
        this.registerSetting(onlyInWater = new ButtonSetting("Only in water", false, this::isEdit));
        this.registerSetting(onlyInWeb = new ButtonSetting("Only in web", false, this::isEdit));
        this.registerSetting(onlyAtLadder = new ButtonSetting("Only at ladder", false, this::isEdit));
        this.registerSetting(jump = new ButtonSetting("Jump", false, this::isEdit));
        this.registerSetting(editStaticY = new ButtonSetting("Edit static y", false, this::isEdit));
        this.registerSetting(editStaticYAmount = new SliderSetting("Edit static y amount", 0.42, -1, 1, 0.01, () -> editStaticY.isToggled() && isEdit()));
        this.registerSetting(editStaticXZ = new ButtonSetting("Edit static xz", false, this::isEdit));
        this.registerSetting(editStaticXZAmount = new SliderSetting("Edit static xz amount", 0.2, 0, 1, 0.01, () -> editStaticXZ.isToggled() && isEdit()));
        this.registerSetting(editAddY = new ButtonSetting("Edit add y", false, this::isEdit));
        this.registerSetting(editAddYAmount = new SliderSetting("Edit add y amount", 0.02, -1, 1, 0.01, () -> editAddY.isToggled() && isEdit()));
        this.registerSetting(editAddXZ = new ButtonSetting("Edit add xz", false, this::isEdit));
        this.registerSetting(editAddXZAmount = new SliderSetting("Edit add xz amount", 0.02, -1, 1, 0.01, () -> editAddXZ.isToggled() && isEdit()));
        this.registerSetting(editMultiplyY = new ButtonSetting("Edit multiply y", false, this::isEdit));
        this.registerSetting(editMultiplyYAmount = new SliderSetting("Edit multiply y amount", 0.02, 0, 1, 0.01, () -> editMultiplyY.isToggled() && isEdit()));
        this.registerSetting(editMultiplyXZ = new ButtonSetting("Edit multiply xz", false, this::isEdit));
        this.registerSetting(editMultiplyXZAmount = new SliderSetting("Edit multiply xz amount", 0.02, 0, 1, 0.01, () -> editMultiplyXZ.isToggled() && isEdit()));
        this.registerSetting(editLimitY = new ButtonSetting("Edit limit y", false, this::isEdit));
        this.registerSetting(editLimitYAmount = new SliderSetting("Edit limit xz amount", 0.02, 0, 1, 0.01, () -> editMultiplyY.isToggled() && isEdit()));
        this.registerSetting(editLimitXZ = new ButtonSetting("Edit limit xz", false, this::isEdit));
        this.registerSetting(editLimitXZAmount = new SliderSetting("Edit limit xz amount", 0.02, 0, 1, 0.01, () -> editMultiplyY.isToggled() && isEdit()));
        this.registerSetting(editTimerSpeed = new ButtonSetting("Edit timer speed", false, this::isEdit));
        this.registerSetting(timerSpeedAmount = new SliderSetting("Timer speed amount", 1, 0.1, 4, 0.0001, () -> editTimerSpeed.isToggled() && isEdit()));
        this.registerSetting(strafe = new ButtonSetting("Strafe", false, this::isEdit));
    }

    public void update() {
        if (noAction()) return;

        if (jump.isToggled() && mc.thePlayer.onGround)
            mc.thePlayer.jump();
        if (editStaticY.isToggled())
            mc.thePlayer.motionY = editStaticYAmount.getInput();
        if (editStaticXZ.isToggled())
            MoveUtil.strafe(editStaticXZAmount.getInput());
        if (editAddY.isToggled())
            mc.thePlayer.motionY += editAddYAmount.getInput();
        if (editAddXZ.isToggled())
            MoveUtil.moveFlying(editAddXZAmount.getInput());
        if (editMultiplyY.isToggled())
            mc.thePlayer.motionY *= editMultiplyYAmount.getInput();
        if (editMultiplyXZ.isToggled()) {
            mc.thePlayer.motionX *= editMultiplyXZAmount.getInput();
            mc.thePlayer.motionZ *= editMultiplyXZAmount.getInput();
        }
        if (editLimitY.isToggled()) {
            if (editLimitYAmount.getInput() > 0) {
                mc.thePlayer.motionY = Math.min(mc.thePlayer.motionY, editLimitYAmount.getInput());
            } else {
                mc.thePlayer.motionY = Math.max(mc.thePlayer.motionY, editLimitYAmount.getInput());
            }
        }
        if (editLimitXZ.isToggled()) {
            if (editLimitXZAmount.getInput() > 0) {
                mc.thePlayer.motionX = Math.min(mc.thePlayer.motionX, editLimitXZAmount.getInput());
                mc.thePlayer.motionZ = Math.min(mc.thePlayer.motionZ, editLimitXZAmount.getInput());
            } else {
                mc.thePlayer.motionX = Math.max(mc.thePlayer.motionX, editLimitXZAmount.getInput());
                mc.thePlayer.motionZ = Math.max(mc.thePlayer.motionZ, editLimitXZAmount.getInput());
            }
        }
        if (editTimerSpeed.isToggled())
            Utils.getTimer().timerSpeed = (float) timerSpeedAmount.getInput();
        if (strafe.isToggled())
            MoveUtil.strafe();
    }

    public boolean isEdit() {
        return editTick.isToggled();
    }

    private boolean noAction() {
        if (!editTick.isToggled()) return true;
        if (mc.thePlayer.hurtTime > 0 && notWhileKB.isToggled()) return true;
        if (!mc.thePlayer.isInWater() && onlyInWater.isToggled()) return true;
        if (!((EntityAccessor) mc.thePlayer).isInWeb() && onlyInWeb.isToggled()) return true;
        return !mc.thePlayer.isOnLadder() && onlyAtLadder.isToggled();
    }
}
