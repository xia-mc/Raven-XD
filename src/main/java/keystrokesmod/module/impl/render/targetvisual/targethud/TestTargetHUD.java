package keystrokesmod.module.impl.render.targetvisual.targethud;

import keystrokesmod.module.impl.render.TargetHUD;
import keystrokesmod.module.impl.render.targetvisual.ITargetVisual;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.Theme;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.font.IFont;
import keystrokesmod.utility.render.Animation;
import keystrokesmod.utility.render.Easing;
import keystrokesmod.utility.render.RenderUtils;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;
import java.awt.*;

import static keystrokesmod.module.impl.render.TargetHUD.*;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

public class TestTargetHUD extends SubMode<TargetHUD> implements ITargetVisual {
    private final ModeSetting theme;
    private final ModeSetting font;
    private final ButtonSetting showStatus;
    private final ButtonSetting healthColor;
    private final Animation healthBarAnimation = new Animation(Easing.EASE_OUT_CIRC, 150);
    private final Animation backgroundWidthAnimation = new Animation(Easing.EASE_OUT_CIRC, 75);

    public TestTargetHUD(String name, @NotNull TargetHUD parent) {
        super(name, parent);
        this.registerSetting(theme = new ModeSetting("Theme", Theme.themes, 0));
        this.registerSetting(font = new ModeSetting("Font", new String[]{"Minecraft", "ProductSans", "Regular"}, 0));
        this.registerSetting(showStatus = new ButtonSetting("Show win or loss", true));
        this.registerSetting(healthColor = new ButtonSetting("Traditional health color", false));
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
        String string = target.getDisplayName().getFormattedText();
        float health = Utils.limit(target.getHealth() / target.getMaxHealth(), 0, 1);
        string = string + " §a" + Math.round(target.getHealth()) + " §c❤ ";
        if (showStatus.isToggled() && mc.thePlayer != null && mc.currentScreen != null) {
            String status = (health <= Utils.getCompleteHealth(mc.thePlayer) / mc.thePlayer.getMaxHealth()) ? "§aW" : "§cL";
            string = string + status;
        }

        final ScaledResolution scaledResolution = new ScaledResolution(mc);
        final int n2 = 8;
        final int n3 = mc.fontRendererObj.getStringWidth(string) + n2 + 30;
        final int n4 = scaledResolution.getScaledWidth() / 2 - n3 / 2 + posX;
        final int n5 = scaledResolution.getScaledHeight() / 2 + 15 + posY;

        current$minX = n4 - n2;
        current$minY = n5 - n2;
        current$maxX = n4 + n3;
        current$maxY = n5 + (mc.fontRendererObj.FONT_HEIGHT + 5) - 6 + n2;

        final int n10 = 255;
        final int n11 = Math.min(n10, 110);
        final int n12 = Math.min(n10, 210);
        final int[] array = Theme.getGradients((int) theme.getInput());

        backgroundWidthAnimation.run(current$maxX - current$minX);
        float animatedWidth = (float) backgroundWidthAnimation.getValue();
        float halfAnimatedWidth = animatedWidth / 2;

        float animatedMinX = (float) (current$minX + current$maxX) / 2 - halfAnimatedWidth;
        float animatedMaxX = (float) (current$minX + current$maxX) / 2 + halfAnimatedWidth;

        RenderUtils.drawRoundedRectangle(animatedMinX, (float) current$minY, animatedMaxX, (float) (current$maxY + 13), 10.0f, Utils.merge(Color.black.getRGB(), n11));

        final int n13 = current$minX + 6 + 30;
        final int n14 = current$maxX - 6;
        final int n15 = current$maxY;

        RenderUtils.drawRoundedRectangle((float) n13, (float) n15, (float) n14, (float) (n15 + 5), 4.0f, Utils.merge(Color.black.getRGB(), n11));

        int k = Utils.merge(array[0], n12);
        int n16 = Utils.merge(array[1], n12);

        float healthBar = (float) (int) (n14 + (n13 - n14) * (1.0 - ((health < 0.05) ? 0.05 : health)));
        if (healthBar - n13 < 3) {
            healthBar = n13 + 3;
        }

        healthBarAnimation.run(healthBar);
        float lastHealthBar = (float) healthBarAnimation.getValue();

        if (healthColor.isToggled()) {
            k = n16 = Utils.merge(Utils.getColorForHealth(health), n12);
        }

        RenderUtils.drawRoundedGradientRect((float) n13, (float) n15, lastHealthBar, (float) (n15 + 5), 4.0f, k, k, k, n16);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        getFont().drawString(string, (float) (n4 + 30), (float) n5, (new Color(220, 220, 220, 255).getRGB() & 0xFFFFFF) | Utils.clamp(n10 + 15) << 24, true);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        if (target instanceof AbstractClientPlayer) {
            renderPlayer2D(current$minX + 5, current$minY + 4, 25, 25, (AbstractClientPlayer) target);
            GlStateManager.disableBlend();
        }
    }
    public static void renderPlayer2D(float x, float y, float width, float height, AbstractClientPlayer player) {
        GlStateManager.pushAttrib();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(player.getLocationSkin());
        Gui.drawScaledCustomSizeModalRect((int) x, (int) y, 8.0F, 8.0F, 8, 8, (int) width, (int) height, 64.0F, 64.0F);
        GlStateManager.disableBlend();
        GlStateManager.popAttrib();
    }
}