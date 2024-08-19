package keystrokesmod.utility.font.impl;

import keystrokesmod.utility.font.CenterMode;
import keystrokesmod.utility.font.IFont;
import org.jetbrains.annotations.NotNull;

import static keystrokesmod.Raven.mc;

public class MinecraftFontRenderer implements IFont {
    public static MinecraftFontRenderer INSTANCE = new MinecraftFontRenderer();
    public void drawString(String text, double x, double y, int color, boolean dropShadow) {
        mc.fontRendererObj.drawString(text, (float) x, (float) y, color, dropShadow);
    }

    public void drawString(String text, double x, double y, int color) {
        drawString(text, x, y, color, false);
    }

    public double width(String text) {
        return mc.fontRendererObj.getStringWidth(text);
    }

    public void drawCenteredString(String text, double x, double y, int color) {
        drawString(text, x - ((int) width(text) >> 1), y, color, false);
    }

    public double height() {
        return mc.fontRendererObj.FONT_HEIGHT;
    }

    @Override
    public void drawString(String text, double x, double y, @NotNull CenterMode centerMode, boolean dropShadow, int color) {
        switch (centerMode) {
            case X:
                drawString(text, x - ((int) width(text) >> 1), y, color);
                break;
            case XY:
                x -= (int) width(text) >> 1;
            case Y:
                y -= (int) height() >> 1;
            case NONE:
                drawString(text, x, y, color);
                break;
        }
    }
}
