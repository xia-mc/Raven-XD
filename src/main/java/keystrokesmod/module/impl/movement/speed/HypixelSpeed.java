package keystrokesmod.module.impl.movement.speed;

import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.module.impl.movement.Speed;
import keystrokesmod.module.impl.movement.TargetStrafe;
import keystrokesmod.module.impl.movement.speed.hypixel.*;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeValue;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.movement.Move;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class HypixelSpeed extends SubMode<Speed> {
    private final ModeValue mode;
    private final ButtonSetting fastTest;
    private final ButtonSetting strafe;
    private final SliderSetting minAngle;
    private final ButtonSetting fullStrafe;

    private double lastAngle;

    public HypixelSpeed(String name, @NotNull Speed parent) {
        super(name, parent);
        this.registerSetting(mode = new ModeValue("Hypixel mode", this)
                .add(new GroundStrafeSpeed("GroundStrafe", this))
                .add(new HypixelGroundSpeed("Ground", this))
                .add(new HypixelLowHopSpeed("LowHop", this))
        );
        this.registerSetting(fastTest = new ButtonSetting("Fast test", false));
        this.registerSetting(strafe = new ButtonSetting("Strafe", false));
        this.registerSetting(minAngle = new SliderSetting("Min angle", 30, 15, 90, 15, strafe::isToggled));
        this.registerSetting(fullStrafe = new ButtonSetting("Full strafe", false, strafe::isToggled));
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        if (strafe.isToggled() && canStrafe()) {
            if (parent.offGroundTicks == 9) {
                MoveUtil.strafe(0.2);
                mc.thePlayer.motionY += 0.1;
            } else {
                MoveUtil.strafe(0.11);
            }
        }

        if (fastTest.isToggled() && mc.thePlayer.onGround) {
            mc.thePlayer.motionX *= 1.114 - MoveUtil.getSpeedEffect() * .01 - Math.random() * 1E-4;
            mc.thePlayer.motionZ *= 1.114 - MoveUtil.getSpeedEffect() * .01 - Math.random() * 1E-4;
        }
    }

    private boolean canStrafe() {
        if (mc.thePlayer.onGround)
            return false;
        final double curAngle = Move.fromMovement(mc.thePlayer.moveForward, mc.thePlayer.moveStrafing).getDeltaYaw()
                + TargetStrafe.getMovementYaw();

        if (Math.abs(curAngle - lastAngle) < minAngle.getInput() || (mc.thePlayer.hurtTime < 7 && mc.thePlayer.hurtTime > 0))
            return false;
        lastAngle = curAngle;

        if (fullStrafe.isToggled())
            return parent.offGroundTicks >= 4 && parent.offGroundTicks <= 9;
        return parent.offGroundTicks == 4 || parent.offGroundTicks == 9;
    }

    @Override
    public void onEnable() {
        mode.enable();
        lastAngle = MoveUtil.direction() * (180 / Math.PI);
    }

    @Override
    public void onDisable() {
        mode.disable();

        if (fastTest.isToggled()) {
            mc.thePlayer.motionX *= .8;
            mc.thePlayer.motionZ *= .8;
        }
    }
}
