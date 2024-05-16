package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.player.Freecam;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class Nametags extends Module { // skidded from raven source code as well
    private final SliderSetting yOffset;
    private final SliderSetting scale;
    private final ButtonSetting autoScale;
    private final ButtonSetting drawRect;
    private final ButtonSetting showHealth;
    private final ButtonSetting showInvis;
    private final ButtonSetting removeTags;
    private final ButtonSetting renderFriends;
    private final ButtonSetting renderEnemies;

    public Nametags() {
        super("Nametags", Module.category.render, 0);
        this.registerSetting(yOffset = new SliderSetting("Y-Offset", 0.0D, -40.0D, 40.0D, 1.0D));
        this.registerSetting(scale = new SliderSetting("Scale", 1.0, 0.5, 5.0, 0.1));
        this.registerSetting(autoScale = new ButtonSetting("Auto-scale", true));
        this.registerSetting(drawRect = new ButtonSetting("Draw rect", true));
        this.registerSetting(showHealth = new ButtonSetting("Show health", true));
        this.registerSetting(showInvis = new ButtonSetting("Show invis", true));
        this.registerSetting(removeTags = new ButtonSetting("Remove tags", false));
        this.registerSetting(renderFriends = new ButtonSetting("Render friends (green)", false));
        this.registerSetting(renderEnemies = new ButtonSetting("Render enemies (red)", false));
    }

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Specials.Pre e) {
        if (removeTags.isToggled()) {
            e.setCanceled(true);
            return;
        }
        if (e.entity instanceof EntityPlayer && e.entity != mc.thePlayer && e.entity.deathTime == 0) {
            final EntityPlayer entityPlayer = (EntityPlayer) e.entity;
            if (!showInvis.isToggled() && entityPlayer.isInvisible()) {
                return;
            }
            if (entityPlayer.getDisplayNameString().isEmpty() || AntiBot.isBot(entityPlayer)) {
                return;
            }
            e.setCanceled(true);
            String s = entityPlayer.getDisplayName().getFormattedText();
            if (showHealth.isToggled()) {
                s = s + " " + Utils.getHealthStr(entityPlayer);
            }
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) e.x + 0.0f, (float) e.y + entityPlayer.height + 0.5f, (float) e.z);
            GL11.glNormal3f(0.0f, 1.0f, 0.0f);
            GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
            GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0f, 0.0f, 0.0f);
            final float n = 0.02666667f;
            if (autoScale.isToggled()) {
                final float renderPartialTicks = Utils.getTimer().renderPartialTicks;
                final EntityPlayer o = (Freecam.freeEntity == null) ? mc.thePlayer : Freecam.freeEntity;
                final double n2 = o.lastTickPosX + (o.posX - o.lastTickPosX) * renderPartialTicks - (entityPlayer.lastTickPosX + (entityPlayer.posX - entityPlayer.lastTickPosX) * renderPartialTicks);
                final double n3 = o.lastTickPosY + (o.posY - o.lastTickPosY) * renderPartialTicks - (entityPlayer.lastTickPosY + (entityPlayer.posY - entityPlayer.lastTickPosY) * renderPartialTicks);
                final double n4 = o.lastTickPosZ + (o.posZ - o.lastTickPosZ) * renderPartialTicks - (entityPlayer.lastTickPosZ + (entityPlayer.posZ - entityPlayer.lastTickPosZ) * renderPartialTicks);
                final double n5 = MathHelper.sqrt_double(n2 * n2 + n3 * n3 + n4 * n4);
                final float n6 = (float) Math.max(n, 0.003 * n5 + 0.011);
                final float n7 = (float) (-(Math.max(0.07, -0.03866143897175789 + 0.018833419308066368 * n5 - 5.270970286801457E-4 * Math.pow(n5, 2.0) + 5.4459292186948005E-6 * Math.pow(n5, 3.0) - 1.9360259173595296E-8 * Math.pow(n5, 4.0)) * n5));
                GlStateManager.scale(-n6, -n6, n6);
                GlStateManager.translate(0.0f, n7, 0.0f);
            } else {
                final float n8 = (float) (n * scale.getInput());
                GlStateManager.scale(-n8, -n8, n8);
            }
            if (entityPlayer.isSneaking() && scale.getInput() == 1.0 && !autoScale.isToggled()) {
                GlStateManager.translate(0.0f, 9.374999f, 0.0f);
            }
            GlStateManager.disableLighting();
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            final int n9 = (int) (-yOffset.getInput());
            final int n10 = mc.fontRendererObj.getStringWidth(s) / 2;
            GlStateManager.disableTexture2D();
            int d = renderFriends.isToggled() ? 1 : 0;
            int d2 = renderEnemies.isToggled() ? 1 : 0;
            if (d != 0) {
                if (Utils.isFriended(entityPlayer)) {
                    d2 = 0;
                } else {
                    d = 0;
                }
            }
            if (d == 0 && d2 != 0 && !Utils.isEnemy(entityPlayer)) {
                d2 = 0;
            }
            if (drawRect.isToggled() || d != 0 || d2 != 0) {
                float n11 = 0.0f;
                float n12 = 0.0f;
                if (d != 0) {
                    n12 = 1.0f;
                } else if (d2 != 0) {
                    n11 = 1.0f;
                }
                final Tessellator getInstance = Tessellator.getInstance();
                final WorldRenderer getWorldRenderer = getInstance.getWorldRenderer();
                getWorldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                getWorldRenderer.pos(-n10 - 1, -1 + n9, 0.0).color(n11, n12, 0.0f, 0.25f).endVertex();
                getWorldRenderer.pos(-n10 - 1, 8 + n9, 0.0).color(n11, n12, 0.0f, 0.25f).endVertex();
                getWorldRenderer.pos(n10 + 1, 8 + n9, 0.0).color(n11, n12, 0.0f, 0.25f).endVertex();
                getWorldRenderer.pos(n10 + 1, -1 + n9, 0.0).color(n11, n12, 0.0f, 0.25f).endVertex();
                getInstance.draw();
            }
            GlStateManager.enableTexture2D();
            mc.fontRendererObj.drawString(s, -n10, n9, -1);
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.popMatrix();
        }
    }
}
