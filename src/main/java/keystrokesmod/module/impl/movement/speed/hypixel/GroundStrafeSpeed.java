package keystrokesmod.module.impl.movement.speed.hypixel;

import keystrokesmod.event.PrePlayerInputEvent;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.impl.movement.speed.HypixelSpeed;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class GroundStrafeSpeed extends SubMode<HypixelSpeed> {
    private double lastAngle = 999;
    private int ticksSinceVelocity = 0;

    public GroundStrafeSpeed(String name, @NotNull HypixelSpeed parent) {
        super(name, parent);
    }

    @Override
    public void onEnable() {
        ticksSinceVelocity = 0;
    }

    @SubscribeEvent
    public void onPrePlayerInput(PrePlayerInputEvent event) {
        if (parent.parent.noAction()) return;

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
