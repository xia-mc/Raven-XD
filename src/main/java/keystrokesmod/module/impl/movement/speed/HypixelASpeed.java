package keystrokesmod.module.impl.movement.speed;

import keystrokesmod.event.PrePlayerInputEvent;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.impl.movement.Speed;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class HypixelASpeed extends SubMode<Speed> {
    private double lastAngle = 999;
    public static int ticksSinceVelocity = 0;

    public HypixelASpeed(String name, @NotNull Speed parent) {
        super(name, parent);
    }

    @Override
    public void onEnable() {
        ticksSinceVelocity = 0;
    }

    @SubscribeEvent
    public void onPrePlayerInput(PrePlayerInputEvent event) {
        if (parent.noAction()) return;

        if (!Utils.jumpDown() && Utils.isMoving() && mc.currentScreen == null) {
            mc.thePlayer.setSprinting(true);
            if (mc.thePlayer.onGround) {
                MoveUtil.strafe(MoveUtil.getAllowedHorizontalDistance() - Math.random() / 100f);
                mc.thePlayer.jump();

                double angle = Math.atan(mc.thePlayer.motionX / mc.thePlayer.motionZ) * (180 / Math.PI);

                if (lastAngle != 999 && Math.abs(lastAngle - angle) > 20 && ticksSinceVelocity > 20) {
                    int speed = mc.thePlayer.isPotionActive(Potion.moveSpeed) ? mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1 : 0;

                    switch (speed) {
                        case 0:
                            MoveUtil.moveFlying(-0.005);
                            break;

                        case 1:
                            MoveUtil.moveFlying(-0.035);
                            break;

                        default:
                            MoveUtil.moveFlying(-0.04);
                            break;
                    }
                }
                lastAngle = angle;
            }
        }
    }

    @Override
    public void onUpdate() {
        if (ticksSinceVelocity < Integer.MAX_VALUE)
            ticksSinceVelocity++;
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S12PacketEntityVelocity) {
            ticksSinceVelocity = 0;
        }
    }
}
