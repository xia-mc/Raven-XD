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
import keystrokesmod.utility.render.*;
import keystrokesmod.utility.render.blur.GaussianBlur;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;
import java.awt.*;
import static keystrokesmod.module.impl.render.TargetHUD.*;

public class RavenNewTargetHUD extends SubMode<TargetHUD> implements ITargetVisual {
    public static final int SHADOW_COLOR = new Color(0, 0, 0, 80).getRGB();
    public static final int RECT_COLOR = new Color(255, 255, 255, 30).getRGB();
    public static final int TEXT_DIST_TO_RECT = 6;
    public static final int RECT_SHADOW_DIST = 6;
    private final ModeSetting theme;
    private final ModeSetting font;
    private final ButtonSetting showStatus;
    private final ButtonSetting healthColor;
    private final Animation healthBarAnimation = new Animation(Easing.EASE_OUT_CIRC, 150);

    public RavenNewTargetHUD(String name, @NotNull TargetHUD parent) {
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
        String name = target.getDisplayName().getFormattedText();
        String healthText = " " + (int) target.getHealth();
        float health = Utils.limit(target.getHealth() / target.getMaxHealth(), 0, 1);
        if (Float.isInfinite(health) || Float.isNaN(health)) {
            health = 0;
        }

        if (showStatus.isToggled() && mc.thePlayer != null) {
            healthText = healthText + " " + ((health <= Utils.getCompleteHealth(mc.thePlayer) / mc.thePlayer.getMaxHealth()) ? "§aW" : "§cL");
        }

        final String renderText = name + " " + healthText;
        final ScaledResolution scaled = new ScaledResolution(mc);
        current$minX = scaled.getScaledWidth() / 2 + posX;
        current$minY = scaled.getScaledHeight() / 2 + 15 + posY;
        current$maxX = current$minX + (int) Math.round(getFont().width(renderText)) + 12;
        current$maxY = current$minY + 16 + 12;

        RenderUtils.drawBloomShadow(
                (float) (current$minX - RECT_SHADOW_DIST), (float) (current$minY - RECT_SHADOW_DIST),
                (float) (current$maxX - current$minX + RECT_SHADOW_DIST * 2.0), (float) (current$maxY - current$minY + RECT_SHADOW_DIST * 2.0),
                6, 8, SHADOW_COLOR, false);

        RenderUtils.drawBloomShadow(
                current$minX, current$minY,
                current$maxX - current$minX, current$maxY - current$minY,
                6, 8, RECT_COLOR, false);

        GaussianBlur.startBlur();
        RenderUtils.drawBloomShadow(
                current$minX, current$minY,
                current$maxX - current$minX, current$maxY - current$minY,
                6, 8, -1, false);
        GaussianBlur.endBlur(8, 2);

        getFont().drawString(renderText, current$minX + TEXT_DIST_TO_RECT, current$minY + TEXT_DIST_TO_RECT, -1);

        float healthBar = (float) (int) (current$maxX - 6 + (current$minX + 6 - current$maxX - 6) * (1.0 - ((health < 0.05) ? 0.05 : health)));
        if (healthBar - current$minX + 3 < 0) { // if goes below, the rounded health bar glitches out
            healthBar = current$minX + 3;
        }
        healthBarAnimation.run(healthBar);
        float lastHealthBar = (float) healthBarAnimation.getValue();

        RenderUtils.drawRoundedGradientRect((float) current$minX + 6, (float) current$maxY - 9, lastHealthBar, (float) (current$maxY - 4), 4.0f,
                Utils.merge(Theme.getGradients((int) theme.getInput())[0], Math.min(255, 210)), Utils.merge(Theme.getGradients((int) theme.getInput())[0], Math.min(255, 210)),
                Utils.merge(Theme.getGradients((int) theme.getInput())[1], Math.min(255, 210)), Utils.merge(Theme.getGradients((int) theme.getInput())[1], Math.min(255, 210)));

        if (healthColor.isToggled()) {
            int healthTextColor = Utils.getColorForHealth(health);
            RenderUtils.drawRoundedRectangle((float) current$minX + 6, (float) current$maxY - 9, lastHealthBar, (float) (current$maxY - 4), 4.0f, healthTextColor);
        }
    }
}