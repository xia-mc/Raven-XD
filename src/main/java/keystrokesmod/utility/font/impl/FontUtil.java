package keystrokesmod.utility.font.impl;

import keystrokesmod.Raven;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;

public class FontUtil {

    private static final IResourceManager RESOURCE_MANAGER = Raven.mc.getResourceManager();

    /**
     * Enhanced method, which gets a font by a resource name with better error handling.
     *
     * @param resource resource name
     * @param size     font size
     * @return font by resource or null if an error occurs
     */
    public static @Nullable Font getResource(final String resource, final int size) {
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, RESOURCE_MANAGER.getResource(new ResourceLocation(resource)).getInputStream()).deriveFont((float) size);
            if (font != null) {
                System.out.println("Font loaded successfully: " + resource);
                return font;
            } else {
                System.out.println("Font loaded but is null: " + resource);
            }
        } catch (FontFormatException e) {
            System.out.println("Font format is not supported: " + resource);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Error accessing the font file: " + resource);
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Unexpected error loading font: " + resource);
            e.printStackTrace();
        }
        return null;
    }
}