package keystrokesmod.module.impl.movement.longjump;

import keystrokesmod.event.PostPlayerInputEvent;
import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.PreVelocityEvent;
import keystrokesmod.module.impl.movement.LongJump;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import static keystrokesmod.module.ModuleManager.blink;

public class HypixelLongJump extends SubMode<LongJump> {
    private boolean selfDamaging = false;
    private int jumps = 0;
    private int offGroundTicks = 0;
    private int ticksSinceVelocity = 99999;

    public HypixelLongJump(String name, @NotNull LongJump parent) {
        super(name, parent);
    }

    @Override
    public void onEnable() {
        selfDamaging = true;
        jumps = 0;
        offGroundTicks = 0;
        ticksSinceVelocity = 99999;
    }

    @Override
    public void onDisable() {
        Utils.resetTimer();
        blink.disable();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPreVelocity(@NotNull PreVelocityEvent event) {
        event.setCanceled(true);
        mc.thePlayer.motionY = event.getMotionY() / 8000.0D;

        ticksSinceVelocity = 0;
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer.onGround)
            offGroundTicks = 0;
        else
            offGroundTicks++;
        ticksSinceVelocity++;

        if (selfDamaging) {
            MoveUtil.stop();
            if (jumps < 4) {
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.motionY = 0.42;
                    jumps++;
                }

                event.setOnGround(false);
            } else if (offGroundTicks >= 11) {
                selfDamaging = false;
                jumps = 0;
            }
        } else {
            if (mc.thePlayer.onGround) {
                MoveUtil.strafe(MoveUtil.getAllowedHorizontalDistance() - Math.random() / 100);
                mc.thePlayer.jump();
            }

            event.setOnGround(false);

            if (offGroundTicks == 1) {
                Utils.getTimer().timerSpeed = 0.2f;
                event.setOnGround(true);
            }

            if (offGroundTicks <= 5 && offGroundTicks > 1) {
                blink.enable();
            }

            if (ticksSinceVelocity <= 20 && ticksSinceVelocity > 1) {
                mc.thePlayer.motionY += 0.0239;

                MoveUtil.moveFlying(0.0039);
            }
        }
    }

    @SubscribeEvent
    public void onPostStrafe(PostPlayerInputEvent event) {
        if (selfDamaging)
            MoveUtil.stop();
    }
}
