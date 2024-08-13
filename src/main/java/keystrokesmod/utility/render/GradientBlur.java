package keystrokesmod.utility.render;

import lombok.Getter;
import net.minecraft.client.renderer.texture.TextureUtil;
import org.jetbrains.annotations.Range;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.awt.*;
import java.nio.IntBuffer;

import static keystrokesmod.Raven.mc;

/**
 * @author TIMER_err
 * 好的视觉，灵感来自果冻
 */
@Getter
public class GradientBlur {
    private final Type type;

    private int targetR1, targetG1, targetB1;
    private int targetR2, targetG2, targetB2;

    private int r1, g1, b1;
    private int lastR1, lastG1, lastB1;
    private int r2, g2, b2;
    private int lastR2, lastG2, lastB2;

    private boolean inited = false;
    private int[] pixels;

    public GradientBlur(Type type) {
        this.type = type;
    }

    public void update(float x, float y, float width, float height) {
        update((int) x, (int) y, (int) width, (int) height);
    }

    public void update(int x, int y, int width, int height) {
        pixels = getPixels(x, y, width, height);
        int leftTop = pixels[0];
        int rightTop = pixels[width - 1];
        int leftBottom = pixels[(height - 1) * width - 1];
        int rightBottom = pixels[height * width - 1];
        Color color1, color2;
        switch (type) {
            case LR:
                color1 = ColorUtils.blend(leftTop, leftBottom);
                color2 = ColorUtils.blend(rightTop, rightBottom);
                break;
            default:
            case TB:
                color1 = ColorUtils.blend(leftTop, rightTop);
                color2 = ColorUtils.blend(leftBottom, rightBottom);
                break;
        }
        targetR1 = color1.getRed();
        targetG1 = color1.getGreen();
        targetB1 = color1.getBlue();
        targetR2 = color2.getRed();
        targetG2 = color2.getGreen();
        targetB2 = color2.getBlue();
        if (!inited) {
            r1 = targetR1;
            g1 = targetB1;
            b1 = targetG1;
            r2 = targetR2;
            g2 = targetB2;
            b2 = targetG2;
            inited = true;
        }
        lastR1 = r1;
        lastG1 = g1;
        lastB1 = b1;
        lastR2 = r2;
        lastG2 = g2;
        lastB2 = b2;

        r1 = animate(r1, targetR1);
        g1 = animate(g1, targetG1);
        b1 = animate(b1, targetB1);
        r2 = animate(r2, targetR2);
        g2 = animate(g2, targetG2);
        b2 = animate(b2, targetB2);

        r1 = Math.min(r1, 255);
        g1 = Math.min(g1, 255);
        b1 = Math.min(b1, 255);
        r1 = Math.max(r1, 0);
        g1 = Math.max(g1, 0);
        b1 = Math.max(b1, 0);
        r2 = Math.min(r2, 255);
        g2 = Math.min(g2, 255);
        b2 = Math.min(b2, 255);
        r2 = Math.max(r2, 0);
        g2 = Math.max(g2, 0);
        b2 = Math.max(b2, 0);
    }

    public void render(float x, float y, float width, float height, @Range(from = 0, to = 1) float partialTicks, @Range(from = 0, to = 1) float alpha) {
        Color color1 = new Color(smoothAnimation(r1, lastR1, partialTicks), smoothAnimation(g1, lastG1, partialTicks), smoothAnimation(b1, lastB1, partialTicks));
        Color color2 = new Color(smoothAnimation(r2, lastR2, partialTicks), smoothAnimation(g2, lastG2, partialTicks), smoothAnimation(b2, lastB2, partialTicks));
        color1 = ColorUtils.reAlpha(color1, alpha);
        color2 = ColorUtils.reAlpha(color2, alpha);
        switch (type) {
            case LR:
            case TB:
                RRectUtils.drawGradientRect(x, y, x + width, y + height, color1.getRGB(), color2.getRGB());
                break;
        }
    }

    private int animate(double current, double target) {
        return (int) (current + (target - current) / 10);
    }

    private int smoothAnimation(double current, double last, float partialTicks) {
        return (int) (current * partialTicks + last * (1.0f - partialTicks));
    }

    public enum Type {
        TB, LR
    }

    private int[] getPixels(int x, int y, int width, int height) {
        int[] pixelValues;
        int size = width * height;
        IntBuffer pixelBuffer = (IntBuffer) BufferUtils.createIntBuffer(size).clear();
        pixelValues = new int[size];
        GL11.glPixelStorei(GL11.GL_PACK_ALIGNMENT, 1);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
        int scaleFactor = 1;
        int k = mc.gameSettings.guiScale;
        if (k == 0) k = 1000;
        while (scaleFactor < k && mc.displayWidth / (scaleFactor + 1) >= 320
                && mc.displayHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor;
        }
        GL11.glReadPixels(x * scaleFactor, (mc.displayHeight - (y + 6) * scaleFactor), width, height, GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, pixelBuffer);
        pixelBuffer.get(pixelValues);
        TextureUtil.processPixelValues(pixelValues, width, height);
        return pixelValues;
    }
}
