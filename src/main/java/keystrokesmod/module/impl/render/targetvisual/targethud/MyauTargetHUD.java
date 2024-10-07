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
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;
import java.awt.*;
import static keystrokesmod.module.impl.render.TargetHUD.*;

public class MyauTargetHUD extends SubMode<TargetHUD> implements ITargetVisual {
    private final ModeSetting theme;
    private final ModeSetting font;
    private final ButtonSetting showStatus;
    private final ButtonSetting healthColor;
    private final ButtonSetting animation;
    private final ButtonSetting hurtRender;
    private final Animation healthBarAnimation = new Animation(Easing.LINEAR, 250);
    private EntityLivingBase lastTarget;

    public MyauTargetHUD(String name, @NotNull TargetHUD parent) {
        super(name, parent);
        this.registerSetting(theme = new ModeSetting("Theme", Theme.themes, 0));
        this.registerSetting(font = new ModeSetting("Font", new String[]{"Minecraft", "ProductSans", "Regular"}, 0));
        this.registerSetting(animation = new ButtonSetting("Animation", true));
        this.registerSetting(hurtRender = new ButtonSetting("HurtRender", true));
        this.registerSetting(showStatus = new ButtonSetting("Show win or loss", true));
        this.registerSetting(healthColor = new ButtonSetting("Traditional health color", true));
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
        String TargetName = target.getDisplayName().getFormattedText();
        float health = Utils.limit(target.getHealth() / target.getMaxHealth(), 0, 1);
        String TargetHealth = String.format("%.1f", target.getHealth()) + "§c❤ ";

        if (showStatus.isToggled() && mc.thePlayer != null) {
            String status = (health <= Utils.getCompleteHealth(mc.thePlayer) / mc.thePlayer.getMaxHealth())? " §aW" : " §cL";
            TargetName = TargetName + status;
        }

        final ScaledResolution scaledResolution = new ScaledResolution(mc);
        final int n2 = 8;
        final int n3 = mc.fontRendererObj.getStringWidth(TargetName) + n2 + 20;
        final int n4 = scaledResolution.getScaledWidth() / 2 - n3 / 2 + posX;
        final int n5 = scaledResolution.getScaledHeight() / 2 + 15 + posY;
        current$minX = n4 - n2;
        current$minY = n5 - n2;
        current$maxX = n4 + n3;
        current$maxY = n5 + (mc.fontRendererObj.FONT_HEIGHT + 5) - 6 + n2;

        final int n10 = 255;
        final int n11 = Math.min(n10, 110);
        final int n12 = Math.min(n10, 210);

        RenderUtils.drawRect(current$minX, current$minY, current$maxX, current$maxY + 7, Utils.merge(Color.black.getRGB(), Math.min(n10, 60)));

        final int n13 = current$minX + 6 + 27;
        final int n14 = current$maxX - 2;
        final int n15 = (int) (current$maxY + 0.45);

        RenderUtils.drawRect(n13, n15, n14, n15 + 4, Utils.merge(Color.black.getRGB(), n11));

        float healthBar = (float) (int) (n14 + (n13 - n14) * (1.0 - ((health < 0.01)? 0 : health)));
        if (healthBar - n13 < 1) {
            healthBar = n13;
        }

        if (target != lastTarget) {
            healthBarAnimation.setValue(healthBar);
            lastTarget = target;
        }

        float displayHealthBar;
        if (animation.isToggled()) {
            healthBarAnimation.run(healthBar);
            displayHealthBar = (float) healthBarAnimation.getValue();
        } else {
            displayHealthBar = healthBar;
        }

        RenderUtils.drawRect(n13, n15, displayHealthBar, n15 + 4,
                Utils.merge(Theme.getGradients((int) theme.getInput())[0], n12));
        if (healthColor.isToggled()) {
            int healthTextColor = Utils.getColorForHealth(health);
            RenderUtils.drawRect(n13, n15, displayHealthBar, n15 + 4, healthTextColor);
        }

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        getFont().drawString(TargetName, (float) (n4 + 25), (float) n5 - 4, (new Color(220, 220, 220, 255).getRGB() & 0xFFFFFF) | Utils.clamp(n10 + 15) << 24, true);
        getFont().drawString(TargetHealth, (float) (n4 + 25), (float) n5 + 6, (new Color(220, 220, 220, 255).getRGB() & 0xFFFFFF) | Utils.clamp(n10 + 15) << 24, true);
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();

        if (target instanceof AbstractClientPlayer) {
            AbstractClientPlayer player = (AbstractClientPlayer) target;
            double targetX = current$minX + 4;
            double targetY = current$minY + 3;
            GlStateManager.color(1, 1, 1, 1);
            RenderUtils.renderPlayer2D((float) targetX, (float) targetY, 25, 25, player);
            if (hurtRender.isToggled()) {
            Color dynamicColor = new Color(255, 255 - (player.hurtTime * 10), 255 - (player.hurtTime * 10));
            GlStateManager.color(dynamicColor.getRed() / 255F, dynamicColor.getGreen() / 255F, dynamicColor.getBlue() / 255F, dynamicColor.getAlpha() / 255F);
            RenderUtils.renderPlayer2D((float) targetX, (float) targetY, 25, 25, player);
            GlStateManager.color(1, 1, 1, 1);
            }
        }
    }
}