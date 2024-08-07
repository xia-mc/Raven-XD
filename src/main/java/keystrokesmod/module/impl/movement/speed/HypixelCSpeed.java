package keystrokesmod.module.impl.movement.speed;

import keystrokesmod.event.PrePlayerInputEvent;
import keystrokesmod.module.impl.movement.Speed;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class HypixelCSpeed extends SubMode<Speed> {
    private boolean timed = false;

    public HypixelCSpeed(String name, @NotNull Speed parent) {
        super(name, parent);
    }

    @SubscribeEvent
    public void onPrePlayerInput(PrePlayerInputEvent event) {
        if (!Utils.nullCheck()) return;
        if (mc.thePlayer.onGround && MoveUtil.isMoving()) {
            Utils.getTimer().timerSpeed = (float) ((mc.thePlayer.ticksExisted % 2 == 0 ? 0.9095f : 1.11f) + (Math.random() - 0.5) / 100.0);
            timed = true;

            event.setSpeed(MoveUtil.getAllowedHorizontalDistance());
        } else if (timed) {
            Utils.resetTimer();
        }
    }

    @Override
    public void onDisable() {
        if (timed)
            Utils.resetTimer();
    }
}
