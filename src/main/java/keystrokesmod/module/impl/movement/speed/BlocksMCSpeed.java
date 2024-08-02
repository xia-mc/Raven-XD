package keystrokesmod.module.impl.movement.speed;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.PrePlayerInputEvent;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.impl.movement.Speed;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class BlocksMCSpeed extends SubMode<Speed> {
    private boolean reset;
    private double speed;

    public BlocksMCSpeed(String name, @NotNull Speed parent) {
        super(name, parent);
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (parent.noAction()) return;
        if (!MoveUtil.isMoving()) {
            event.setPosX(event.getPosX() + (Math.random() - 0.5) / 3);
            event.setPosZ(event.getPosZ() + (Math.random() - 0.5) / 3);
        }
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            speed = 0;
        }
    }

    @SubscribeEvent
    public void onPrePlayerInput(PrePlayerInputEvent event) {
        if (parent.noAction()) return;
        final double base = MoveUtil.getAllowedHorizontalDistance();
        final boolean potionActive = mc.thePlayer.isPotionActive(Potion.moveSpeed);

        if (MoveUtil.isMoving()) {
            switch (parent.offGroundTicks) {
                case 0:
                    mc.thePlayer.motionY = MoveUtil.jumpBoostMotion(0.42f);
                    speed = base * (potionActive ? 1.4 : 2.15);
                    break;

                case 1:
                    speed -= 0.8 * (speed - base);
                    break;

                default:
                    speed -= speed / MoveUtil.BUNNY_FRICTION;
                    break;
            }

            reset = false;
        } else if (!reset) {
            speed = 0;

            reset = true;
            speed = MoveUtil.getAllowedHorizontalDistance();
        }

        if (mc.thePlayer.isCollidedHorizontally) {
            speed = MoveUtil.getAllowedHorizontalDistance();
        }

        event.setSpeed(Math.max(speed, base), Math.random() / 2000);
    }

    @Override
    public void onDisable() {
        speed = 0;
    }
}
