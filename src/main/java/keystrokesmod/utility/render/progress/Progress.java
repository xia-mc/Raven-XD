package keystrokesmod.utility.render.progress;

import keystrokesmod.utility.font.CenterMode;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.render.Animation;
import keystrokesmod.utility.render.Easing;
import keystrokesmod.utility.render.RRectUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.ScaledResolution;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.awt.*;
import java.util.function.Supplier;

import static keystrokesmod.Raven.mc;

public class Progress {
    private final Animation posYAnimation = new Animation(Easing.EASE_OUT_EXPO, 300);
    @Getter
    @Setter
    private int posY;
    private @Range(from = 0, to = 1) double progress = 0;
    @Setter
    private String text;

    public Progress(int posY, @NotNull String text) {
        this.posY = posY;
        this.text = text;
        posYAnimation.setValue(this.posY);
    }

    public Progress(@NotNull String text) {
        this(0, text);
    }

    public void setProgress(@Range(from = 0, to = 1) double progress) {
        this.progress = progress;
    }

    public void setProgress(@NotNull Supplier<@NotNull @Range(from = 0, to = 1) Double> progress) {
        setProgress(progress.get());
    }

    public void render() {
        posYAnimation.run(posY);

        if (mc.currentScreen != null) return;

        final ScaledResolution sr = new ScaledResolution(mc);
        final double width = sr.getScaledWidth_double() / 5;
        final double height = width / 13;
        final double renderY = sr.getScaledHeight_double() * 0.8 - posYAnimation.getValue() * height * 1.5;

        // background
        RRectUtils.drawRound(
                sr.getScaledWidth_double() / 2.0 - width / 2.0,
                renderY - height / 2.0,
                width, height, height / 2.0, new Color(255, 255, 255, 30)
        );

        // progress
        RRectUtils.drawRound(
                sr.getScaledWidth_double() / 2.0 - width / 2.0,
                renderY - height / 2.0,
                width * progress, height, height / 2.0,
                new Color(6, 112, 190, 200)
        );

        // text
        FontManager.tenacity16.drawString(
                text,
                sr.getScaledWidth_double() / 2.0,
                renderY,
                CenterMode.XY, false, new Color(240, 240, 240).getRGB()
        );
    }
}
