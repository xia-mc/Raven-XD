package keystrokesmod.module.impl.render.targetvisual.targethud;

import keystrokesmod.module.impl.render.TargetHUD;
import keystrokesmod.module.impl.render.targetvisual.ITargetVisual;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.font.IFont;
import keystrokesmod.utility.render.RenderUtils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ExhibitionTargetHUD extends SubMode<TargetHUD> implements ITargetVisual {
    private final ModeSetting font;
    
    public ExhibitionTargetHUD(String name, @NotNull TargetHUD parent) {
        super(name, parent);
        this.registerSetting(font = new ModeSetting("Font", new String[]{"Minecraft", "ProductSans", "Regular"}, 0));
    }
    
    private IFont getFont() {
        switch ((int) font.getInput()) {
            default:
            case 0:
                return FontManager.getMinecraft();
            case 1:
                return FontManager.productSansMedium;
            case 2:
                return FontManager.regular22;
        }
    }

    @Override
    public void render(@NotNull EntityLivingBase target) {
        String name = target.getDisplayName().getFormattedText();
        double x = TargetHUD.current$minX;
        double y = TargetHUD.current$minY;

        ///////////////////////////////////////////////////

        RenderUtils.drawRect(x, y, x + 140, y + 50, new Color(0, 0, 0).getRGB()); // rect 1
        RenderUtils.drawRect(x + 0.5, y + 0.5, x + 0.5 + 139, y + 0.5 + 49, new Color(60, 60, 60).getRGB());// rect 2
        RenderUtils.drawRect(x + 1.5, y + 1.5, x + 1.5 + 137, y + 1.5 + 47, new Color(0, 0, 0).getRGB()); // rect 3
        RenderUtils.drawRect(x + 2, y + 2, x + 2 + 136, y + 2 + 46, new Color(25, 25, 24).getRGB()); // rect 4

        ///////////////////////////////////////////////////

        getFont().drawString(name, (int) (x + 40), (int) (y + 6), Color.WHITE.getRGB()); //drawing name obviously

        ///////////////////////////////////////////////////

        if (mc.thePlayer != null) {
            GlStateManager.pushMatrix(); // gay shit
            GlStateManager.scale(0.7, 0.7, 0.7); //scaling text
            getFont().drawString("HP: " + Math.round(target.getHealth()) + " | Dist: " + Math.round(mc.thePlayer.getDistanceToEntity(target)), (int) ((x + 40) * (1 / 0.7)), (int) ((y + 17) * (1 / 0.7)), Color.WHITE.getRGB()); //drawing said scaled text
            GlStateManager.popMatrix(); // more gay shit
        }

        ///////////////////////////////////////////////////

        double health = Math.min(Math.round(target.getHealth()), target.getMaxHealth()); //health calculations

        ///////////////////////////////////////////////////

        Color healthColor = getColor(target); //getting le color

        ///////////////////////////////////////////////////

        double x2 = x + 40; // new "x" variable for loop
        RenderUtils.drawRect(x2, y + 25, x + 100 - 9, y + 25 + 5, new Color(healthColor.getRed(), healthColor.getGreen(), healthColor.getBlue(), 50).getRGB()); //static healthbar rendered below healthbar
        RenderUtils.drawRect(x2, y + 25, x + (100 - 9) * (health / target.getMaxHealth()), y + 25 + 6, healthColor.getRGB()); //actual functioning healthbar
        RenderUtils.drawRect(x2, y + 25, x + 91, y + 25 + 1, Color.BLACK.getRGB()); // top healthbar outline
        RenderUtils.drawRect(x2, y + 30, x + 91, y + 30 + 1, Color.BLACK.getRGB()); // bottom healthbar outline

        ///////////////////////////////////////////////////

        for (int i = 0; i < 10; i++) {
            RenderUtils.drawRect(x2 + 10 * i, y + 25, x2 + 10 * i + 1, y + 25 + 6, Color.BLACK.getRGB()); //so i don't need to render 10 rectangles (messy code)
        }

        ///////////////////////////////////////////////////

        RenderUtils.renderItemIcon(x2, y + 31, target.getHeldItem()); //rendering targets held item
        RenderUtils.renderItemIcon(x2 + 15, y + 31, target.getEquipmentInSlot(4)); //rendering targets helmet
        RenderUtils.renderItemIcon(x2 + 30, y + 31, target.getEquipmentInSlot(3)); //rendering targets chestplate
        RenderUtils.renderItemIcon(x2 + 45, y + 31, target.getEquipmentInSlot(2)); //rendering targets leggings
        RenderUtils.renderItemIcon(x2 + 60, y + 31, target.getEquipmentInSlot(1)); //rendering targets boots

        ///////////////////////////////////////////////////

        if (mc.thePlayer != null) {
            GlStateManager.pushMatrix(); //gay shit
            GlStateManager.scale(0.4, 0.4, 0.4); //scaling the gay shit
            GlStateManager.translate((x + 20) * (1 / 0.4), (y + 44) * (1 / 0.4), 40f * (1 / 0.4)); //translating
            drawModel(target.rotationYaw, target.rotationPitch, target); //drawing model
            GlStateManager.popMatrix(); //more gay shit
        }
    }

    private static Color getColor(@NotNull EntityLivingBase target) { // a VERY retarded way to do health colors :smile:
        Color healthColor = new Color(0, 165, 0); //green
        if (target.getHealth() < target.getMaxHealth() / 1.5)
            healthColor = new Color(200, 200, 0); //yellow
        if (target.getHealth() < target.getMaxHealth() / 2.5)
            healthColor = new Color(200, 155, 0); //orange
        if (target.getHealth() < target.getMaxHealth() / 4)
            healthColor = new Color(120, 0, 0); //red
        return healthColor;
    }

    public static void drawModel(final float yaw, final float pitch, final @NotNull EntityLivingBase entityLivingBase) { //method to draw model (skidded)
        GlStateManager.resetColor();
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0f, 0.0f, 50.0f);
        GlStateManager.scale(-50.0f, 50.0f, 50.0f);
        GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
        final float renderYawOffset = entityLivingBase.renderYawOffset;
        final float rotationYaw = entityLivingBase.rotationYaw;
        final float rotationPitch = entityLivingBase.rotationPitch;
        final float prevRotationYawHead = entityLivingBase.prevRotationYawHead;
        final float rotationYawHead = entityLivingBase.rotationYawHead;
        GlStateManager.rotate(135.0f, 0.0f, 1.0f, 0.0f);
        RenderHelper.enableStandardItemLighting();
        GlStateManager.rotate(-135.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate((float) (-Math.atan(pitch / 40.0f) * 20.0), 1.0f, 0.0f, 0.0f);
        entityLivingBase.renderYawOffset = yaw - 0.4f;
        entityLivingBase.rotationYaw = yaw - 0.2f;
        entityLivingBase.rotationPitch = pitch;
        entityLivingBase.rotationYawHead = entityLivingBase.rotationYaw;
        entityLivingBase.prevRotationYawHead = entityLivingBase.rotationYaw;
        GlStateManager.translate(0.0f, 0.0f, 0.0f);
        final RenderManager renderManager = mc.getRenderManager();
        renderManager.setPlayerViewY(180.0f);
        renderManager.setRenderShadow(false);
        renderManager.renderEntityWithPosYaw(entityLivingBase, 0.0, 0.0, 0.0, 0.0f, 1.0f);
        renderManager.setRenderShadow(true);
        entityLivingBase.renderYawOffset = renderYawOffset;
        entityLivingBase.rotationYaw = rotationYaw;
        entityLivingBase.rotationPitch = rotationPitch;
        entityLivingBase.prevRotationYawHead = prevRotationYawHead;
        entityLivingBase.rotationYawHead = rotationYawHead;
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
        GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        GlStateManager.disableTexture2D();
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.resetColor();
    }
}
