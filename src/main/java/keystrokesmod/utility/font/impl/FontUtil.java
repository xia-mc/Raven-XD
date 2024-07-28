package keystrokesmod.utility.font.impl;

import keystrokesmod.Raven;
import static keystrokesmod.Raven.mc;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class FontUtil {

    private static final IResourceManager RESOURCE_MANAGER = Raven.mc.getResourceManager();

    public static Font getResource(Map<String, Font> locationMap, String location, int size) {
        Font font;

        ScaledResolution sr = new ScaledResolution(mc);

        size = (int) (size * ((double) sr.getScaleFactor() / 2));

        try {
            if (locationMap.containsKey(location)) {
                font = locationMap.get(location).deriveFont(Font.PLAIN, size);
            } else {
                InputStream is = mc.getResourceManager().getResource(new ResourceLocation("keystrokesmod:fonts/" + location)).getInputStream();
                locationMap.put(location, font = Font.createFont(0, is));
                font = font.deriveFont(Font.PLAIN, size);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            font = new Font("default", Font.PLAIN, size);
        }
        return font;
    }
}