package keystrokesmod.module.impl.movement.speed.hypixel.lowhop;

import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.module.impl.movement.speed.hypixel.HypixelLowHopSpeed;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class HypixelLowHopMotionSpeed extends SubMode<HypixelLowHopSpeed> {

    public HypixelLowHopMotionSpeed(String name, @NotNull HypixelLowHopSpeed parent) {
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
        } else if (parent.noLowHop() || MoveUtil.getJumpEffect() != 0) {
            return;
        }

        switch (parent.parent.parent.offGroundTicks) {
            case 1:
                mc.thePlayer.motionY = 0.39;
                break;
            case 3:
                mc.thePlayer.motionY -= 0.13;
                break;
            case 4:
                mc.thePlayer.motionY -= 0.2;
                break;
        }
    }
}
