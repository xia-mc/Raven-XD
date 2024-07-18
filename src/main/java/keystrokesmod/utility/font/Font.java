package keystrokesmod.utility.font;

import keystrokesmod.utility.font.impl.MinecraftFontRenderer;

public interface Font {
    int drawString(String text, double x, double y, int color, boolean dropShadow);
    void drawString(String text, double x, double y, MinecraftFontRenderer.CenterMode centerMode, boolean shadow, int color);
    int drawString(final String text, final double x, final double y, final int color);

    int drawStringWithShadow(final String text, final double x, final double y, final int color);

    int width(String text);

    int drawCenteredString(final String text, final double x, final double y, final int color);

    int drawRightString(final String text, final double x, final double y, final int color);
    void wrapText(String text, double x, double y, MinecraftFontRenderer.CenterMode centerMode, boolean shadow, int color, double width);
    float height();

    default int getStringWidth(String text) {
        return width(text);
    }

    default int getCharWidth(char c) {
        return width(String.valueOf(c));
    }
}
