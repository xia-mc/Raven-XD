package keystrokesmod.utility.render;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class BackgroundUtils {
    private static final int BLOOM_COLOR = new Color(255, 255, 255, 50).getRGB();
    private static int huoCheX = -99999;

    public static void renderBackground(@NotNull GuiScreen gui) {
        final int width = gui.width;
        final int height = gui.height;

        if (huoCheX == -99999)
            huoCheX = -width;

        RenderUtils.drawImage(new ResourceLocation("keystrokesmod:textures/backgrounds/bg.png"), 0, 0, width, height);
//        RenderUtils.drawBloomShadow(0, 0, width, height, 12, 6, BLOOM_COLOR, false);
        RenderUtils.drawImage(new ResourceLocation("keystrokesmod:textures/backgrounds/qi.png"), 0, 0, width, height);
        RenderUtils.drawImage(new ResourceLocation("keystrokesmod:textures/backgrounds/DianXian.png"), 0, 0, width, height);
        RenderUtils.drawImage(new ResourceLocation("keystrokesmod:textures/backgrounds/huoChe.png"), huoCheX, height / 3F, width * 2F, height / 3F);
        RenderUtils.drawImage(new ResourceLocation("keystrokesmod:textures/backgrounds/DianXian2.png"), 0, 0, width, height);
        RenderUtils.drawImage(new ResourceLocation("keystrokesmod:textures/backgrounds/cao.png"), 0, 0, width, height);
        RenderUtils.drawBloomShadow(0, 0, width, height, 12, 6, BLOOM_COLOR, true);
        RenderUtils.drawImage(new ResourceLocation("keystrokesmod:textures/backgrounds/ren.png"), 0, 0, width, height);
        if (huoCheX >= 0) {
            huoCheX = -width;
        }
        huoCheX++;
    }
}
