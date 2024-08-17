package keystrokesmod.module.impl.movement.speed.hypixel;

import keystrokesmod.event.PrePlayerInputEvent;
import keystrokesmod.module.impl.exploit.disabler.hypixel.HypixelMotionDisabler;
import keystrokesmod.module.impl.movement.speed.HypixelSpeed;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Vulcan speed lol
 */
public class HypixelLowHopSpeed extends SubMode<HypixelSpeed> {
    private final ButtonSetting strafe;

    private float angle = 0;

    public HypixelLowHopSpeed(String name, @NotNull HypixelSpeed parent) {
        super(name, parent);
        this.registerSetting(strafe = new ButtonSetting("Strafe", false));
    }

    @Override
    public void onEnable() {
        angle = (float) Math.toDegrees(MoveUtil.direction());
    }

    @SubscribeEvent
    public void onPrePlayerInput(PrePlayerInputEvent event) {
        if (!MoveUtil.isMoving() || !HypixelMotionDisabler.isDisabled() || parent.parent.noAction()) return;
        switch (parent.parent.offGroundTicks) {
            case 0:
                if (!Utils.jumpDown()) {
                    mc.thePlayer.jump();
                    MoveUtil.strafe(0.485);
                }
                break;
            case 5:
                if (strafe.isToggled())
                    MoveUtil.strafe(0.315);
                mc.thePlayer.motionY = MoveUtil.predictedMotion(mc.thePlayer.motionY, 2);
                angle = (float) Math.toDegrees(MoveUtil.direction());
                break;
            case 6:
                if (strafe.isToggled())
                    MoveUtil.strafe();
                break;
        }
    }

}
