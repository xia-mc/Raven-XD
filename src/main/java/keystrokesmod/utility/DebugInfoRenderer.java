package keystrokesmod.utility;

import keystrokesmod.Raven;
import keystrokesmod.module.impl.other.anticheats.utils.world.PlayerMove;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.font.CenterMode;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.render.RenderUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;

import java.awt.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static keystrokesmod.utility.Utils.mc;

public class DebugInfoRenderer extends net.minecraft.client.gui.Gui {
    private static final Queue<Double> speedFromJump = new ConcurrentLinkedQueue<>();
    private static double avgSpeedFromJump = -1;

    @SubscribeEvent
    public void onRenderTick(RenderTickEvent ev) {
        if (!Raven.debugger || ev.phase != TickEvent.Phase.END || !Utils.nullCheck()) {
            speedFromJump.clear();
            avgSpeedFromJump = -1;
            return;
        }

        if (mc.thePlayer.onGround) {
            if (!speedFromJump.isEmpty()) {
                avgSpeedFromJump = 0;
                speedFromJump.forEach(speed -> avgSpeedFromJump += speed);
                avgSpeedFromJump /= speedFromJump.size();
            }
            speedFromJump.clear();
        }
        speedFromJump.add(PlayerMove.getXzSecSpeed(
                new Vec3(mc.thePlayer.lastTickPosX, mc.thePlayer.lastTickPosY, mc.thePlayer.lastTickPosZ),
                new Vec3(mc.thePlayer))
        );

        if (mc.currentScreen == null) {
            RenderUtils.renderBPS(true, true);
            if (avgSpeedFromJump != -1) {
                ScaledResolution scaledResolution = new ScaledResolution(Raven.mc);

                FontManager.getMinecraft().drawString(
                        String.format("Speed from jump: %.2f", avgSpeedFromJump),
                        (float)(scaledResolution.getScaledWidth() / 2),
                        (float)(scaledResolution.getScaledHeight() / 2 + 30),
                        CenterMode.X,
                        true,
                        new Color(255, 255, 255).getRGB()
                );
            }
        }
    }
}
