package keystrokesmod.utility.render;

import net.minecraft.client.renderer.GlStateManager;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ColorUtils {
    public static final List<AnimationUtils> animation = new ArrayList<>();

    static {
        for (int i = 0; i < 18; i++) {
            animation.add(new AnimationUtils(0.0F));
        }
    }
    // fixing this never >:(
    private static final Pattern COLOR_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");

    public static Color getBackgroundColor(int id) {
        switch (id) {
            case 1:
                return new Color(16, 15, 69);
            case 2:
                return new Color(19, 19, 128);
            case 3:
                return new Color(255, 255, 255);
            case 4:
                return new Color(0, 0, 0);
            default:
                return new Color(255, 0, 255);
        }
    }

    public static Color getFontColor(int id, int alpha) {
        Color rawColor = getRawFontColor(id);
        int speed = 12;

        if (id == 1) {
            animation.get(12).setAnimation(rawColor.getRed(), speed);
            animation.get(13).setAnimation(rawColor.getGreen(), speed);
            animation.get(14).setAnimation(rawColor.getBlue(), speed);

            return new Color((int) animation.get(12).getValue(), (int) animation.get(13).getValue(), (int) animation.get(14).getValue(), alpha);
        }

        if (id == 2) {
            animation.get(15).setAnimation(rawColor.getRed(), speed);
            animation.get(16).setAnimation(rawColor.getGreen(), speed);
            animation.get(17).setAnimation(rawColor.getBlue(), speed);

            return new Color((int) animation.get(15).getValue(), (int) animation.get(16).getValue(), (int) animation.get(17).getValue(), alpha);
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