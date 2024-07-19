package keystrokesmod.module.impl.render;

import keystrokesmod.Raven;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.utility.font.Font;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class Watermark extends Module {
    public static final String VERSION = "1.17.2";
    public static final HashMap<String, ResourceLocation> WATERMARK = new HashMap<>();

    public static String customName = "CustomClient";

    public static int posX = 5;
    public static int posY = 5;
    public static int current$minX = 0;
    public static int current$maxX = 0;
    public static int current$minY = 0;
    public static int current$maxY = 0;
    private final ModeSetting mode;
    private final ModeSetting watermarkText;
    private final ModeSetting watermarkPhoto;
    private final ButtonSetting showVersion;
    private final ButtonSetting lowercase;
    private final ButtonSetting shadow;

    public Watermark() {
        super("Watermark", category.render);
        this.registerSetting(mode = new ModeSetting("Mode", new String[]{"Text", "Photo"}, 0));
        final ModeOnly textMode = new ModeOnly(mode, 0);
        final ModeOnly photoMode = new ModeOnly(mode, 1);
        this.registerSetting(watermarkText = new ModeSetting("Watermark", new String[]{"Default", "Augustus", "Custom"}, 0, textMode));
        this.registerSetting(watermarkPhoto = new ModeSetting("Watermark", new String[]{"Enders", "Augustus"}, 0, photoMode));
        this.registerSetting(showVersion = new ButtonSetting("Show version", true, textMode));
        this.registerSetting(lowercase = new ButtonSetting("Lowercase", false, textMode));
        this.registerSetting(shadow = new ButtonSetting("Shadow", true, textMode));

        for (String s : Arrays.asList("enders", "augustus")) {
            try (InputStream stream = Objects.requireNonNull(Raven.class.getResourceAsStream("/assets/keystrokesmod/textures/watermarks/" + s + ".png"))) {
                BufferedImage image = ImageIO.read(stream);
                WATERMARK.put(s, Minecraft.getMinecraft().renderEngine.getDynamicTextureLocation(s, new DynamicTexture(image)));
            } catch (NullPointerException | IOException ignored) {
            }
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        render();
    }

    public void render() {
        switch ((int) mode.getInput()) {
            case 0:
                String text = "";
                switch ((int) watermarkText.getInput()) {
                    case 0:
                        text = "§r§f§lRaven §bX§9D §7";
                        break;
                    case 1:
                        text = "§f§lAugustus ";
                        break;
                    case 2:
                        text = customName;
                        break;
                }

                if (!text.isEmpty()) {
                    if (showVersion.isToggled())
                        text += VERSION;
                    if (lowercase.isToggled())
                        text = text.toLowerCase();

                    Font font = FontManager.getMinecraft();
                    font.drawString(text, posX, posY, 0, shadow.isToggled());

                    current$minX = posX;
                    current$maxX = current$minX + font.getStringWidth(text);
                    current$minY = posY;
                    current$maxY = current$minY + Math.round(font.height());
                }
                break;
            case 1:
                switch ((int) watermarkPhoto.getInput()) {
                    case 0:
                        RenderUtils.drawImage(WATERMARK.get("enders"), posX, posY, 150, 45, new Color(255, 255, 255));
                        break;
                    case 1:
                        RenderUtils.drawImage(WATERMARK.get("augustus"), posX, posY, 50, 50, new Color(255, 255, 255));
                        break;
                }
            break;
        }
    }
}
