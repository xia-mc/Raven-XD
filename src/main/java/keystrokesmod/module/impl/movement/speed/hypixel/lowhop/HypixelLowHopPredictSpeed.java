package keystrokesmod.module.impl.movement.speed.hypixel.lowhop;

import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.module.impl.movement.speed.hypixel.HypixelLowHopSpeed;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class HypixelLowHopPredictSpeed extends SubMode<HypixelLowHopSpeed> {
    public HypixelLowHopPredictSpeed(String name, @NotNull HypixelLowHopSpeed parent) {
        super(name, parent);
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        if (!MoveUtil.isMoving() || parent.parent.parent.noAction()) return;
        if (parent.parent.parent.offGroundTicks == 0) {
            if (!Utils.jumpDown()) {
                MoveUtil.strafe(MoveUtil.getAllowedHorizontalDistance() - Math.random() / 100f);
                mc.thePlayer.jump();
            }
        } else if (parent.parent.parent.offGroundTicks == 5 && !parent.noLowHop()) {
            mc.thePlayer.motionY = MoveUtil.predictedMotion(mc.thePlayer.motionY, 2);
        }
    }
}
