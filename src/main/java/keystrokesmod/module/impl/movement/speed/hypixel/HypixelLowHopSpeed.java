package keystrokesmod.module.impl.movement.speed.hypixel;

import keystrokesmod.event.PrePlayerInputEvent;
import keystrokesmod.module.impl.movement.speed.HypixelSpeed;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import net.minecraft.potion.Potion;
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
        this.registerSetting(strafe = new ButtonSetting("Strafe", true));
    }

    @Override
    public void onEnable() {
        angle = mc.thePlayer.rotationYaw;
    }

    @SubscribeEvent
    public void onPrePlayerInput(PrePlayerInputEvent event) {
        if (!MoveUtil.isMoving()) return;
        switch (parent.parent.offGroundTicks) {
            case 0:
                mc.thePlayer.jump();

                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    MoveUtil.strafe(0.6);
                } else {
                    MoveUtil.strafe(0.485);
                }
                break;
            case 5:
                mc.thePlayer.motionY = MoveUtil.predictedMotion(mc.thePlayer.motionY, 2);
                break;
            default:
                if (strafe.isToggled() && parent.parent.offGroundTicks > 5)
                    angle = MoveUtil.simulationStrafeAngle(angle, 10.0F);
                break;
        }
    }

}
