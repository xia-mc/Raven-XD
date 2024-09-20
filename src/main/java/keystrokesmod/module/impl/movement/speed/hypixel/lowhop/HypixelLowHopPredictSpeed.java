package keystrokesmod.module.impl.movement.speed.hypixel.lowhop;

import keystrokesmod.event.PrePlayerInputEvent;
import keystrokesmod.module.impl.movement.speed.hypixel.HypixelLowHopSpeed;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class HypixelLowHopPredictSpeed extends SubMode<HypixelLowHopSpeed> {
    private final ButtonSetting fast;

    public HypixelLowHopPredictSpeed(String name, @NotNull HypixelLowHopSpeed parent) {
        super(name, parent);
        this.registerSetting(fast = new ButtonSetting("Fast", false));
    }

    @SubscribeEvent
    public void onPrePlayerInput(PrePlayerInputEvent event) {
        if (!MoveUtil.isMoving() || parent.parent.parent.noAction()) return;
        if (parent.parent.parent.offGroundTicks == 0) {
            if (!Utils.jumpDown()) {
                if (fast.isToggled() && !RotationHandler.isSet()) {
                    mc.thePlayer.jump();
                    if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                        MoveUtil.strafe(0.6);
                    } else {
                        MoveUtil.strafe(0.485);
                    }
                } else {
                    MoveUtil.strafe(MoveUtil.getAllowedHorizontalDistance() - Math.random() / 100f);
                    mc.thePlayer.jump();
                }
            }
        } else if (parent.parent.parent.offGroundTicks == 5 && !parent.noLowHop()) {
            mc.thePlayer.motionY = MoveUtil.predictedMotion(mc.thePlayer.motionY, 2);
        }
    }
}
