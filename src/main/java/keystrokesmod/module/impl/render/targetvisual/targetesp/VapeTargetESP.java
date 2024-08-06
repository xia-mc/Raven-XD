package keystrokesmod.module.impl.render.targetvisual.targetesp;

import keystrokesmod.module.impl.render.TargetESP;
import keystrokesmod.module.impl.render.targetvisual.ITargetVisual;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

public class VapeTargetESP extends SubMode<TargetESP> implements ITargetVisual {
    private final SliderSetting normalRed;
    private final SliderSetting normalGreen;
    private final SliderSetting normalBlue;
    private final SliderSetting normalAlpha;
    private final SliderSetting hitRed;
    private final SliderSetting hitGreen;
    private final SliderSetting hitBlue;
    private final SliderSetting hitAlpha;

    public VapeTargetESP(String name, @NotNull TargetESP parent) {
        super(name, parent);
        this.registerSetting(new DescriptionSetting("Normal color"));
        this.registerSetting(normalRed = new SliderSetting("Normal red", 100, 0, 255, 1));
        this.registerSetting(normalGreen = new SliderSetting("Normal green", 100, 0, 255, 1));
        this.registerSetting(normalBlue = new SliderSetting("Normal blue", 190, 0, 255, 1));
        this.registerSetting(normalAlpha = new SliderSetting("Normal alpha", 100, 0, 255, 1));
        this.registerSetting(new DescriptionSetting("Hit color"));
        this.registerSetting(hitRed = new SliderSetting("Hit red", 255, 0, 255, 1));
        this.registerSetting(hitGreen = new SliderSetting("Hit green", 0, 0, 255, 1));
        this.registerSetting(hitBlue = new SliderSetting("Hit blue", 0, 0, 255, 1));
        this.registerSetting(hitAlpha = new SliderSetting("Hit alpha", 100, 0, 255, 1));
    }

    @Override
    public void render(@NotNull EntityLivingBase target) {
        mc.entityRenderer.disableLightmap();
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND);

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        mc.entityRenderer.disableLightmap();

        double radius = target.width;
        double x = target.lastTickPosX + (target.posX - target.lastTickPosX) * (double) Utils.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosX;
        double y = target.lastTickPosY + (target.posY - target.lastTickPosY) * (double) Utils.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosY;
        double z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * (double) Utils.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosZ;
        double eased = target.height - 0.2;

        for (int segments = 0; segments < 360; segments += 5) {

            double x1 = x - Math.sin(segments * Math.PI / 180F) * radius;
            double z1 = z + Math.cos(segments * Math.PI / 180F) * radius;
            double x2 = x - Math.sin((segments - 5) * Math.PI / 180F) * radius;
            double z2 = z + Math.cos((segments - 5) * Math.PI / 180F) * radius;

            GL11.glBegin(GL11.GL_QUADS);
            if (target.hurtTime > 0) {
                GL11.glColor4f((float) (hitRed.getInput() / 255f), (float) (hitGreen.getInput() / 255f), (float) (hitBlue.getInput() / 255f), (float) (hitAlpha.getInput() / 255f));
            } else {
                GL11.glColor4f((float) (normalRed.getInput() / 255f), (float) (normalGreen.getInput() / 255f), (float) (normalBlue.getInput() / 255f), (float) (normalAlpha.getInput() / 255f));
            }
            GL11.glVertex3d(x1, y, z1);
            GL11.glVertex3d(x2, y, z2);
            if (target.hurtTime > 0) {
                GL11.glColor4f((float) (hitRed.getInput() / 255f), (float) (hitGreen.getInput() / 255f), (float) (hitBlue.getInput() / 255f), 0);
            } else {
                GL11.glColor4f((float) (normalRed.getInput() / 255f), (float) (normalGreen.getInput() / 255f), (float) (normalBlue.getInput() / 255f), 0);
            }
            GL11.glVertex3d(x2, y + eased, z2);
            GL11.glVertex3d(x1, y + eased, z1);
            GL11.glEnd();
            GL11.glBegin(GL11.GL_LINE_LOOP);
            GL11.glVertex3d(x2, y + eased, z2);
            GL11.glVertex3d(x1, y + eased, z1);
            GL11.glEnd();
        }

        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glColor4f(1f, 1f, 1f, 1f);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }
}
