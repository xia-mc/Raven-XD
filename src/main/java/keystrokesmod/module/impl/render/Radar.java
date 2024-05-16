package keystrokesmod.module.impl.render;

import keystrokesmod.clickgui.ClickGui;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.utility.RenderUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class Radar extends Module {
    private final ButtonSetting showGUI;
    private final ButtonSetting tracerLines;
    private int scale = 2;
    private final int rectColor = new Color(0, 0, 0, 125).getRGB();
    public Radar() {
        super("Radar", category.render);
        this.registerSetting(showGUI = new ButtonSetting("Show in GUI", false));
        this.registerSetting(tracerLines = new ButtonSetting("Show tracer lines", false));
    }

    public void onUpdate() {
        this.scale = new ScaledResolution(mc).getScaleFactor();
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !Utils.nullCheck()) {
            return;
        }
        if (mc.currentScreen instanceof ClickGui) {
            return;
        }
        if (!showGUI.isToggled() && (mc.currentScreen != null || mc.gameSettings.showDebugInfo)) {
            return;
        }
        final int n = 5;
        final int n2 = 70;
        final int n3 = n + 100;
        final int n4 = n2 + 100;
        Gui.drawRect(n, n2, n3, n4, rectColor);
        Gui.drawRect(n - 1, n2 - 1, n3 + 1, n2, -1);
        Gui.drawRect(n - 1, n4, n3 + 1, n4 + 1, -1);
        Gui.drawRect(n - 1, n2, n, n4, -1);
        Gui.drawRect(n3, n2, n3 + 1, n4, -1);
        RenderUtils.drawPolygon(n3 / 2 + 3, n2 + 52, 5.0, 3, -1);
        GL11.glPushMatrix();
        GL11.glEnable(3089);
        GL11.glScissor(n * this.scale, mc.displayHeight - this.scale * 170, n3 * this.scale - this.scale * 5, this.scale * 100);
        for (final EntityPlayer entityPlayer : mc.theWorld.playerEntities) {
            if (entityPlayer != mc.thePlayer && entityPlayer.deathTime == 0) {
                if (AntiBot.isBot(entityPlayer)) {
                    continue;
                }
                final double getDistanceSqToEntity = entityPlayer.getDistanceSqToEntity(mc.thePlayer);
                if (getDistanceSqToEntity > 360.0) {
                    continue;
                }
                final double n5 = (mc.thePlayer.rotationYaw + Math.atan2(entityPlayer.posX - mc.thePlayer.posX, entityPlayer.posZ - mc.thePlayer.posZ) * 57.295780181884766) % 360.0;
                final double n6 = getDistanceSqToEntity / 5.0;
                final double n7 = n6 * Math.sin(Math.toRadians(n5));
                final double n8 = n6 * Math.cos(Math.toRadians(n5));
                if (tracerLines.isToggled()) {
                    GL11.glPushMatrix();
                    GL11.glEnable(3042);
                    GL11.glEnable(2848);
                    GL11.glDisable(2929);
                    GL11.glDisable(3553);
                    GL11.glBlendFunc(770, 771);
                    GL11.glEnable(3042);
                    GL11.glLineWidth(0.5f);
                    GL11.glColor3d(1.0, 1.0, 1.0);
                    GL11.glBegin(2);
                    GL11.glVertex2d(n3 / 2 + 3, n2 + 52);
                    GL11.glVertex2d((double)(n3 / 2 + 3) - n7, (double)(n2 + 52) - n8);
                    GL11.glEnd();
                    GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
                    GL11.glDisable(3042);
                    GL11.glEnable(3553);
                    GL11.glEnable(2929);
                    GL11.glDisable(2848);
                    GL11.glDisable(3042);
                    GL11.glPopMatrix();
                }
                RenderUtils.drawPolygon((double)(n3 / 2 + 3) - n7, (double)(n2 + 52) - n8, 3.0, 4, Color.red.getRGB());
            }
        }
        GL11.glDisable(3089);
        GL11.glPopMatrix();
    }
}
