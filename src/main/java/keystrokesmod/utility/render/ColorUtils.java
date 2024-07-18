package keystrokesmod.utility.render;

import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;
import java.util.regex.Pattern;

public class ColorUtils {
    public static final List<AnimationUtils> animation = new ArrayList<>(Collections.nCopies(18, new AnimationUtils(0.0F)));
    // fixing this never >:(
    private static final Pattern COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");

    public static Color getBackgroundColor(int id) {
        Color color = new Color(255, 0, 255);
        if (id == 1) {
            color = new Color(16, 15, 69);
        } else if (id == 2) {
            color = new Color(19, 19, 128);
        } else if (id == 3) {
            color = new Color(255, 255, 255);
        } else if (id == 4) {
            color = new Color(0, 0, 0);
        }
        return color;
    }

    public static Color getFontColor(int id, int alpha) {
        Color rawColor = getRawFontColor(id);
        int speed = 12;

        if (id == 1) {
            animation[12].setAnimation(rawColor.getRed(), speed);
            animation[13].setAnimation(rawColor.getGreen(), speed);
            animation[14].setAnimation(rawColor.getBlue(), speed);

            return new Color((int) animation[12].getValue(), (int) animation[13].getValue(), (int) animation[14].getValue(), alpha);
        }

        if (id == 2) {
            animation[15].setAnimation(rawColor.getRed(), speed);
            animation[16].setAnimation(rawColor.getGreen(), speed);
            animation[17].setAnimation(rawColor.getBlue(), speed);

            return new Color((int) animation[15].getValue(), (int) animation[16].getValue(), (int) animation[17].getValue(), alpha);
        }

        return rawColor;
    }

    private static Color getRawFontColor(int id) {
        Color color = new Color(255, 0, 0);

        if (id == 1) {
            color = new Color(255, 255, 255);
        } else if (id == 2) {
            color = new Color(173, 173, 173);
        }

        return color;
    }

    public static Color getFontColor(int id) {
        return getFontColor(id, 255);
    }

    public static void setColor(int color, double alpha) {
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        GlStateManager.color(r, g, b, (float) alpha);
    }

    public static void setColor(int color) {
        setColor(color, (color >> 24 & 255) / 255.0F);
    }

    public static void resetColor() {
        GlStateManager.color(1, 1, 1, 1);
    }

    public static String stripColor(String input) {
        return COLOR_PATTERN.matcher(input).replaceAll("");
    }
}