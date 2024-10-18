package keystrokesmod.utility.render;

import keystrokesmod.Raven;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class BackgroundUtils {
    private static final int BLOOM_COLOR = new Color(255, 255, 255, 50).getRGB();
    private static int huoCheX = -99999;

    public static void renderBackground(@NotNull GuiScreen gui) {
        final int width = gui.width;
        final int height = gui.height;

        if (huoCheX == -99999)
            huoCheX = -width;

        RenderUtils.drawImage(new ResourceLocation("keystrokesmod:textures/backgrounds/bg.png"), 0, 0, width, height);
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

    @Getter
    private static final ResourceLocation logoPng = new ResourceLocation("keystrokesmod:textures/backgrounds/bluearacive.png");
}
