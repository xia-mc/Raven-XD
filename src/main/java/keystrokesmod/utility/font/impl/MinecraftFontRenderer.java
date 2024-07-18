package keystrokesmod.utility.font.impl;

import keystrokesmod.utility.font.Font;
import keystrokesmod.utility.render.ColorUtils;

import java.util.ArrayList;
import java.util.List;

import static keystrokesmod.Raven.mc;

public class MinecraftFontRenderer implements Font {
    public static MinecraftFontRenderer INSTANCE = new MinecraftFontRenderer();
    public void drawString(String text, double x, double y, CenterMode centerMode, boolean shadow, int color) {
        switch (centerMode) {
            case X:
                if (shadow) {
                    this.drawString(text, x - (double) this.getStringWidth(text) / 2 + 0.5, y + 0.5, color, true);
                }
                this.drawString(text, x - (double) this.getStringWidth(text) / 2, y, color, false);
                return;
            case Y:
                if (shadow) {
                    this.drawString(text, x + 0.5, y - this.height() / 2 + 0.5, color, true);
                }
                this.drawString(text, x, y - this.height() / 2, color, false);
                return;
            case XY:
                if (shadow) {
                    this.drawString(text, x - (double) this.getStringWidth(text) / 2 + 0.5, y - this.height() / 2 + 0.5, color, true);
                }
                this.drawString(text, x - (double) this.getStringWidth(text) / 2, y - this.height() / 2, color, false);
                return;
            case NONE:
                if (shadow) {
                    this.drawString(text, x + 0.5, y + 0.5, color, true);
                }
                this.drawString(text, x, y, color, false);
        }
    }
    public int drawString(String text, double x, double y, int color, boolean dropShadow) {
        return mc.fontRendererObj.drawString(text, (float) x, (float) y, color, dropShadow);
    }

    public void wrapText(String text, double x, double y, CenterMode centerMode, boolean shadow, int color, double width) {
        List<String> lines = new ArrayList<>();
        String[] words = text.trim().split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            double totalWidth = getStringWidth(line + " " + word);

            if (x + totalWidth >= x + width) {
                lines.add(line.toString());
                line = new StringBuilder(word).append(" ");
                continue;
            }

            line.append(word).append(" ");
        }
        lines.add(line.toString());

        double newY = y - (centerMode == CenterMode.XY || centerMode == CenterMode.Y ? ((lines.size() - 1) * (height() + 5)) / 2 : 0);
        // add x centermode support never !!!!
        for (String s : lines) {
            ColorUtils.resetColor();
            drawString(s, x, newY, centerMode, shadow, color);
            newY += height() + 5;
        }
    }

    @Override
    public int drawString(String text, double x, double y, int color) {
        return drawString(text, x, y, color, false);
    }

    @Override
    public int drawStringWithShadow(String text, double x, double y, int color) {
        return drawString(text, x, y, color, true);
    }

    @Override
    public int width(String text) {
        return mc.fontRendererObj.getStringWidth(text);
    }

    @Override
    public int drawCenteredString(String text, double x, double y, int color) {
        return drawString(text, x - (width(text) >> 1), y, color, false);
    }

    @Override
    public int drawRightString(String text, double x, double y, int color) {
        return drawString(text, x - (width(text)), y, color, false);
    }

    @Override
    public float height() {
        return mc.fontRendererObj.FONT_HEIGHT;
    }
    public enum CenterMode {
        X,
        Y,
        XY,
        NONE
    }
}
