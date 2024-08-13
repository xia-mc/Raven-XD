package keystrokesmod.utility;

import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.client.Settings;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

public enum Theme {
    Rainbow(null, null), // 0
    Cherry(new Color(255, 200, 200), new Color(243, 58, 106)), // 1
    Cotton_candy(new Color(99, 249, 255), new Color(255, 104, 204)), // 2
    Flare(new Color(231, 39, 24), new Color(245, 173, 49)), // 3
    Flower(new Color(215, 166, 231), new Color(211, 90, 232)), // 4
    Gold(new Color(255, 215, 0), new Color(240, 159, 0)), // from croat, 5
    Grayscale(new Color(240, 240, 240), new Color(110, 110, 110)), // 6
    Royal(new Color(125, 204, 241), new Color(30, 71, 170)), // 7
    Sky(new Color(160, 230, 225), new Color(15, 190, 220)), // 8
    Vine(new Color(17, 192, 45), new Color(201, 234, 198)), // 9
    Descriptor(new Color(95, 235, 255), new Color(68, 102, 250)), // 10
    HiddenBind(new Color(245, 33, 33), new Color(229, 21, 98)), // 11
    Astolfo(new Color(255,74,255), new Color(74,255,255), new Color(255,255,255)), // 12 (credit @biPas)
    Custom();
    private static final Map<Theme, BufferedImage> imageCache = new HashMap<>(values().length);

    private final List<Color> gradients;

    Theme(Color @NotNull ... gradients) {
        this.gradients = new ArrayList<>(gradients.length);
        Collections.addAll(this.gradients, gradients);
    }

    public static int getGradient(int index, double delay) {
        if (index > 0) {
            return convert(
                    (Math.sin(System.currentTimeMillis() / 1.0E8 * Settings.timeMultiplier.getInput() * 400000.0 + delay * Settings.offset.getInput()) + 1.0) * 0.5,
                    values()[index]
            ).getRGB();
        } else if (index == 0) {
            return Utils.getChroma(2, (long) delay);
        }
        return -1;
    }

    @Contract("_, _, _ -> new")
    public static @NotNull Color convert(@NotNull Color color, @NotNull Color color2, double n) {
        double n2 = 1.0 - n;
        return new Color(
                (int) (color.getRed() * n + color2.getRed() * n2),
                (int) (color.getGreen() * n + color2.getGreen() * n2),
                (int) (color.getBlue() * n + color2.getBlue() * n2)
        );
    }

    private static @NotNull Color convert(@Range(from = 0, to = 1) double position, @NotNull Theme theme) {
        List<Color> colors;
        if (theme == Custom) {
            colors = ModuleManager.clientTheme.getColors();
        } else {
            colors = theme.gradients;
        }

        if (colors == null) {
            throw new IllegalArgumentException("At least two colors are required for a gradient.");
        }
        if (colors.size() < 2) {
            return colors.get(0);
        }
        if (position < 0 || position > 1) {
            throw new IllegalArgumentException("Position must be between 0 and 1.");
        }

        int numColors = colors.size();
        double scaledPosition = position * (numColors - 1);
        int startIndex = (int) Math.floor(scaledPosition);
        int endIndex = Math.min(startIndex + 1, numColors - 1);
        float localPosition = (float) (scaledPosition - startIndex);

        Color startColor = colors.get(startIndex);
        Color endColor = colors.get(endIndex);

        int red = (int) (startColor.getRed() * (1 - localPosition) + endColor.getRed() * localPosition);
        int green = (int) (startColor.getGreen() * (1 - localPosition) + endColor.getGreen() * localPosition);
        int blue = (int) (startColor.getBlue() * (1 - localPosition) + endColor.getBlue() * localPosition);

        return new Color(red, green, blue);
    }

    @Contract("_ -> new")
    public static int @NotNull [] getGradients(int index) {
        Theme[] values = values();
        if (index >= 0 && index < values.length && values[index] != null) {
            Color firstGradient = values[index].gradients.get(0);
            Color secondGradient = values[index].gradients.get(1);
            if (firstGradient != null && secondGradient != null) {
                return new int[]{firstGradient.getRGB(), secondGradient.getRGB()};
            } else {
                return new int[]{Utils.getChroma(2, 0), Utils.getChroma(2, 0)};
            }
        }
        return new int[]{0, 0};
    }

    public static final String[] themes = Arrays.stream(values()).map(Theme::name).toArray(String[]::new);
}
