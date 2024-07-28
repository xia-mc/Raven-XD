package keystrokesmod.utility.font.impl;

import static keystrokesmod.Raven.mc;

public class MinecraftFontRenderer {
    public static MinecraftFontRenderer INSTANCE = new MinecraftFontRenderer();
    public int drawString(String text, double x, double y, int color, boolean dropShadow) {
        return mc.fontRendererObj.drawString(text, (float) x, (float) y, color, dropShadow);
    }

    public int drawString(String text, double x, double y, int color) {
        return drawString(text, x, y, color, false);
    }

    public int drawStringWithShadow(String text, double x, double y, int color) {
        return drawString(text, x, y, color, true);
    }

    public int width(String text) {
        return mc.fontRendererObj.getStringWidth(text);
    }

    public int drawCenteredString(String text, double x, double y, int color) {
        return drawString(text, x - (width(text) >> 1), y, color, false);
    }

    public int drawRightString(String text, double x, double y, int color) {
        return drawString(text, x - (width(text)), y, color, false);
    }

    public float height() {
        return mc.fontRendererObj.FONT_HEIGHT;
    }
}
