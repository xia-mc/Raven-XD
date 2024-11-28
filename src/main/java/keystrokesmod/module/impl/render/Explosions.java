package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.render.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Sphere;

public class Explosions extends Module {
    private final ButtonSetting showBlastRing = new ButtonSetting("Blast ring (inner)", true);
    private final ButtonSetting showDamageRing = new ButtonSetting("Damage ring (outer)", true);

    public Explosions() {
        super("Explosions", category.render);
        this.registerSetting(showDamageRing, showBlastRing);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent e) {
        if (!Utils.nullCheck()) return;

        double playerX = mc.getRenderManager().viewerPosX;
        double playerY = mc.getRenderManager().viewerPosY;
        double playerZ = mc.getRenderManager().viewerPosZ;

        for (Object obj : mc.theWorld.loadedEntityList) {
            if (obj instanceof EntityTNTPrimed) {
                EntityTNTPrimed tnt = (EntityTNTPrimed) obj;
                GL11.glPushMatrix();
                GL11.glTranslated(tnt.posX - playerX, tnt.posY - playerY, tnt.posZ - playerZ);

                if (showBlastRing.isToggled()) {
                    renderBlastRing();
                }
                if (showDamageRing.isToggled()) {
                    renderExplosionSphere();
                }
                GL11.glPopMatrix();
            }
        }
    }

    private void renderBlastRing() {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableDepth();
        RenderUtils.glColor(255, 255, 0, 128);
        Sphere sphere = new Sphere();
        sphere.setDrawStyle(GLU.GLU_LINE);
        sphere.draw(4.0f, 24, 24);
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }

    private void renderExplosionSphere() {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableDepth();
        RenderUtils.glColor(255, 0, 0, 128);
        Sphere sphere = new Sphere();
        sphere.setDrawStyle(GLU.GLU_SILHOUETTE);
        sphere.draw(8.0f, 32, 32);
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
    }
}
