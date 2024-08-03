package keystrokesmod.utility.font.impl;

import keystrokesmod.utility.font.IFont;

import static keystrokesmod.Raven.mc;

public class MinecraftFontRenderer implements IFont {
    public static MinecraftFontRenderer INSTANCE = new MinecraftFontRenderer();
    public double drawString(String text, double x, double y, int color, boolean dropShadow) {
        return mc.fontRendererObj.drawString(text, (float) x, (float) y, color, dropShadow);
    }

    public double drawString(String text, double x, double y, int color) {
        return drawString(text, x, y, color, false);
    }

    public double width(String text) {
        return mc.fontRendererObj.getStringWidth(text);
    }

    public double drawCenteredString(String text, double x, double y, int color) {
        return drawString(text, x - ((int) width(text) >> 1), y, color, false);
    }

    public double height() {
        return mc.fontRendererObj.FONT_HEIGHT;
    }
}
