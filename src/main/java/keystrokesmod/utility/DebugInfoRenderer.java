package keystrokesmod.utility;

import keystrokesmod.Raven;
import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.impl.other.anticheats.utils.world.PlayerMove;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.font.CenterMode;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.render.RenderUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static keystrokesmod.utility.Utils.mc;

public class DebugInfoRenderer extends net.minecraft.client.gui.Gui {
    private static final Queue<Double> speedFromJump = new ConcurrentLinkedQueue<>();
    private static double avgSpeedFromJump = -1;
    private static Vec3 lastServerPos = Vec3.ZERO;
    private static Vec3 curServerPos = Vec3.ZERO;

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (!Raven.debugger || !Utils.nullCheck()) {
            speedFromJump.clear();
            avgSpeedFromJump = -1;
            return;
        }

        if (mc.thePlayer.onGround) {
            if (!speedFromJump.isEmpty()) {
                avgSpeedFromJump = 0;
                for (double speed : speedFromJump) {
                    avgSpeedFromJump += speed;
                }
                avgSpeedFromJump /= speedFromJump.size();
            }
            speedFromJump.clear();
        }
        speedFromJump.add(PlayerMove.getXzSecSpeed(
                new Vec3(mc.thePlayer.lastTickPosX, mc.thePlayer.lastTickPosY, mc.thePlayer.lastTickPosZ),
                new Vec3(event.getPosX(), event.getPosY(), event.getPosZ()))
        );
    }

    @SubscribeEvent
    public void onRenderTick(RenderTickEvent ev) {
        if (!Raven.debugger || !Utils.nullCheck()) {
            return;
        }

        if (mc.currentScreen == null) {
            RenderUtils.renderBPS(String.format("Server speed: %.2fbps  Client speed: ", PlayerMove.getXzSecSpeed(lastServerPos, curServerPos)), true, true);
            if (avgSpeedFromJump != -1) {
                ScaledResolution scaledResolution = new ScaledResolution(Raven.mc);

                FontManager.getMinecraft().drawString(
                        String.format("Speed from jump: %.2f", avgSpeedFromJump),
                        (float)(scaledResolution.getScaledWidth() / 2),
                        (float)(scaledResolution.getScaledHeight() / 2 + 30),
                        CenterMode.X,
                        false,
                        new Color(255, 255, 255).getRGB()
                );
            }
        }
    }

    @SubscribeEvent
    public void onSendPacket(@NotNull SendPacketEvent event) {
        if (event.getPacket() instanceof C03PacketPlayer.C04PacketPlayerPosition) {
            C03PacketPlayer.C04PacketPlayerPosition packet = (C03PacketPlayer.C04PacketPlayerPosition) event.getPacket();
            lastServerPos = curServerPos;
            curServerPos = new Vec3(
                    packet.getPositionX(),
                    packet.getPositionY(),
                    packet.getPositionZ()
            );
        } else if (event.getPacket() instanceof C03PacketPlayer.C06PacketPlayerPosLook) {
            C03PacketPlayer.C06PacketPlayerPosLook packet = (C03PacketPlayer.C06PacketPlayerPosLook) event.getPacket();
            lastServerPos = curServerPos;
            curServerPos = new Vec3(
                    packet.getPositionX(),
                    packet.getPositionY(),
                    packet.getPositionZ()
            );
        }
    }
}
