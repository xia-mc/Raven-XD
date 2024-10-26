package keystrokesmod.utility.font.impl;

import keystrokesmod.Raven;
import static keystrokesmod.Raven.mc;

import keystrokesmod.utility.Utils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;

public class FontUtil {

    private static final IResourceManager RESOURCE_MANAGER = Raven.mc.getResourceManager();

    public static Font getResource(String location, int size) {
        Font font;

        ScaledResolution sr = new ScaledResolution(mc);

        size = (int) (size * ((double) sr.getScaleFactor() / 2));

        try {
            InputStream is = mc.getResourceManager().getResource(new ResourceLocation("keystrokesmod:fonts/" + location)).getInputStream();
            font = Font.createFont(0, is).deriveFont(Font.PLAIN, size);
        } catch (Exception exception) {
            Utils.sendMessage(Arrays.toString(exception.getStackTrace()));
            font = new Font("default", Font.PLAIN, size);
        }
        return font;
    }
}