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
    public static final ResourceLocation RES_BG = new ResourceLocation("keystrokesmod:textures/backgrounds/bg.png");
    public static final ResourceLocation RES_QI = new ResourceLocation("keystrokesmod:textures/backgrounds/qi.png");
    public static final ResourceLocation RES_DIANXIAN = new ResourceLocation("keystrokesmod:textures/backgrounds/DianXian.png");
    public static final ResourceLocation RES_HUOCHE = new ResourceLocation("keystrokesmod:textures/backgrounds/huoChe.png");
    public static final ResourceLocation RES_DIANXIAN2 = new ResourceLocation("keystrokesmod:textures/backgrounds/DianXian2.png");
    public static final ResourceLocation RES_CAO = new ResourceLocation("keystrokesmod:textures/backgrounds/cao.png");
    public static final ResourceLocation RES_REN = new ResourceLocation("keystrokesmod:textures/backgrounds/ren.png");
    public static final ResourceLocation RES_LOGO = new ResourceLocation("keystrokesmod:textures/backgrounds/ravenxd.png");

    private static int huoCheX = -99999;

    public static void renderBackground(@NotNull GuiScreen gui) {
        final int width = gui.width;
        final int height = gui.height;

        if (huoCheX == -99999)
            huoCheX = -width;

        RenderUtils.drawImage(RES_BG, 0, 0, width, height);
        RenderUtils.drawImage(RES_QI, 0, 0, width, height);
        RenderUtils.drawImage(RES_DIANXIAN, 0, 0, width, height);
        RenderUtils.drawImage(RES_HUOCHE, huoCheX, height / 3F, width * 2F, height / 3F);
        RenderUtils.drawImage(RES_DIANXIAN2, 0, 0, width, height);
        RenderUtils.drawImage(RES_CAO, 0, 0, width, height);
        RenderUtils.drawBloomShadow(0, 0, width, height, 12, 6, BLOOM_COLOR, true);
        RenderUtils.drawImage(RES_REN, 0, 0, width, height);
        if (huoCheX >= 0) {
            huoCheX = -width;
        }
        huoCheX++;
    }

    public static ResourceLocation getLogoPng() {
        return RES_LOGO;
    }
}