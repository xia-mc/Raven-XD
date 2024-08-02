package keystrokesmod.module.impl.movement.speed;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.module.impl.movement.Speed;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class HypixelASpeed extends SubMode<Speed> {

    public HypixelASpeed(String name, @NotNull Speed parent) {
        super(name, parent);
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (parent.noAction()) return;

        if (mc.thePlayer.onGround && MoveUtil.isMoving()) {
            if (!Utils.jumpDown()) {
                mc.thePlayer.motionY = 0.42;
                MoveUtil.strafe(0.415);
            }
        }
    }
}
