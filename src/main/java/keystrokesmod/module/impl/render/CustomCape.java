package keystrokesmod.module.impl.render;

import keystrokesmod.Raven;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;

import static com.mojang.realmsclient.gui.ChatFormatting.*;

public final class CustomCape extends Module {
    private static File directory;
    public static String[] CAPES_NAME = new String[]{
            "RavenAnime", "RavenAqua", "RavenGreen", "RavenPurple", "RavenRed", "RavenWhite", "RavenYellow",
            "Cherry", "Die",
            "Astolfo"
    };
    public static final List<ResourceLocation> LOADED_CAPES = new ArrayList<>();
    public static final ModeSetting cape = new ModeSetting("Cape", CAPES_NAME, 0);

    public CustomCape() {
        super("CustomCape", category.render);
        this.registerSetting(new ButtonSetting("Load capes", CustomCape::loadCapes));
        this.registerSetting(cape);

        directory = new File(mc.mcDataDir + File.separator + "keystrokes", "customCapes");
        if (!directory.exists()) {
            boolean success = directory.mkdirs();
            if (!success) {
                System.out.println("There was an issue creating customCapes directory.");
            }
        }

        this.registerSetting(new ButtonSetting("Open folder", () -> {
            try {
                Desktop.getDesktop().open(directory);
            }
            catch (IOException ex) {
                Raven.profileManager.directory.mkdirs();
                Utils.sendMessage("&cError locating folder, recreated.");
            }
        }));

        loadCapes();
    }

    public static void loadCapes() {
        final File[] files;
        try {
            files = Objects.requireNonNull(directory.listFiles());
        } catch (NullPointerException e) {
            Utils.sendMessage(RED + "Fail to load.");
            return;
        }

        final String[] builtinCapes = new String[]{
                "RavenAnime", "RavenAqua", "RavenGreen", "RavenPurple", "RavenRed", "RavenWhite", "RavenYellow",
                "Cherry", "Die",
                "Astolfo"
        };
        CAPES_NAME = new String[files.length + builtinCapes.length];
        LOADED_CAPES.clear();
        System.arraycopy(builtinCapes, 0, CAPES_NAME, 0, builtinCapes.length);

        for (String s : builtinCapes) {
            String name = s.toLowerCase();
            try {
                InputStream stream = Raven.class.getResourceAsStream("/assets/keystrokesmod/textures/capes/" + name + ".png");
                if (stream == null)
                    stream = Raven.class.getResourceAsStream("/assets/keystrokesmod/textures/capes/" + s + ".png");
                if (stream == null)
                    continue;
                BufferedImage bufferedImage = ImageIO.read(stream);
                LOADED_CAPES.add(Minecraft.getMinecraft().renderEngine.getDynamicTextureLocation(name, new DynamicTexture(bufferedImage)));
                stream.close();
            } catch (Exception e) {
                Utils.sendMessage(RED + "Failed to load cape '" + RESET + s + RED + "'");
            }
        }

        for (int i = 0, filesLength = files.length; i < filesLength; i++) {
            File file = files[i];
            if (!file.exists() || !file.isFile()) continue;
            if (!file.getName().endsWith(".png")) continue;
            String fileName = file.getName().substring(0, file.getName().length() - 4);

            CAPES_NAME[builtinCapes.length + i] = fileName;

            try {
                BufferedImage bufferedImage = ImageIO.read(file);
                LOADED_CAPES.add(Minecraft.getMinecraft().renderEngine.getDynamicTextureLocation(fileName, new DynamicTexture(bufferedImage)));
            } catch (IOException e) {
                Utils.sendMessage(RED + "Failed to load cape '" + RESET + fileName + RED + "'");
            }
        }

        cape.setOptions(CAPES_NAME);
        Utils.sendMessage(GREEN + "Loaded " + RESET + cape.getOptions().length + GREEN + " capes.");
    }
}
