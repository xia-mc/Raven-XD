package keystrokesmod.module.impl.player;

import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.render.RenderUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Blink extends Module {
    private final ButtonSetting pulse;
    private final SliderSetting pulseDelay;
    private final ButtonSetting initialPosition;
    private final ButtonSetting overlay;

    public final List<Packet<?>> blinkedPackets = new ArrayList<>();
    private long startTime = -1;
    private Vec3 pos;
    public static final int color = new Color(72, 125, 227).getRGB();
    public Blink() {
        super("Blink", category.player);
        this.registerSetting(pulse = new ButtonSetting("Pulse", false));
        this.registerSetting(pulseDelay = new SliderSetting("Pulse delay", 1000, 0, 10000, 100, pulse::isToggled));
        this.registerSetting(initialPosition = new ButtonSetting("Show initial position", true));
        this.registerSetting(overlay = new ButtonSetting("Overlay", false));
    }

    @Override
    public void onEnable() {
        start();
    }

    private void start() {
        blinkedPackets.clear();
        pos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        startTime = System.currentTimeMillis();
    }

    public void onDisable() {
        reset();
    }

    private void reset() {
        synchronized (blinkedPackets) {
            for (Packet<?> packet : blinkedPackets) {
                PacketUtils.sendPacketNoEvent(packet);
            }
        }
        blinkedPackets.clear();
        pos = null;
    }

    @Override
    public String getInfo() {
        return String.valueOf(blinkedPackets.size());
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event) {
        if (!overlay.isToggled() || event.phase != TickEvent.Phase.END || !Utils.nullCheck()) {
            return;
        }

        RenderUtils.drawText("blinking: " + blinkedPackets.size());
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent e) {
        if (!Utils.nullCheck()) {
            this.disable();
            return;
        }
        Packet<?> packet = e.getPacket();
        if (packet.getClass().getSimpleName().startsWith("S")) {
            return;
        }
        if (packet instanceof C00Handshake
                || packet instanceof C00PacketLoginStart
                || packet instanceof C00PacketServerQuery
                || packet instanceof C01PacketEncryptionResponse
                || packet instanceof C01PacketChatMessage) {
            return;
        }
        blinkedPackets.add(packet);
        e.setCanceled(true);

        if (pulse.isToggled()) {
            if (System.currentTimeMillis() - startTime >= pulseDelay.getInput()) {
                reset();
                start();
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent e) {
        if (!Utils.nullCheck() || pos == null || !initialPosition.isToggled()) {
            return;
        }
        drawBox(pos);
    }

    public static void drawBox(@NotNull Vec3 pos) {
        GlStateManager.pushMatrix();
        double x = pos.xCoord - mc.getRenderManager().viewerPosX;
        double y = pos.yCoord - mc.getRenderManager().viewerPosY;
        double z = pos.zCoord - mc.getRenderManager().viewerPosZ;
        AxisAlignedBB bbox = mc.thePlayer.getEntityBoundingBox().expand(0.1D, 0.1, 0.1);
        AxisAlignedBB axis = new AxisAlignedBB(bbox.minX - mc.thePlayer.posX + x, bbox.minY - mc.thePlayer.posY + y, bbox.minZ - mc.thePlayer.posZ + z, bbox.maxX - mc.thePlayer.posX + x, bbox.maxY - mc.thePlayer.posY + y, bbox.maxZ - mc.thePlayer.posZ + z);
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glLineWidth(2.0F);
        GL11.glColor4f(r, g, b, a);
        RenderUtils.drawBoundingBox(axis, r, g, b);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GlStateManager.popMatrix();
    }
}

