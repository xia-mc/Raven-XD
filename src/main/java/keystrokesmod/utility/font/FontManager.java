package keystrokesmod.utility.font;

import keystrokesmod.utility.font.impl.FontRenderer;
import keystrokesmod.utility.font.impl.FontUtil;
import keystrokesmod.utility.font.impl.MinecraftFontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import static keystrokesmod.Raven.mc;

import java.util.HashMap;
import java.util.Map;

public class FontManager {
    public static FontRenderer
            regular16, regular22,
            icon20,
            productSans16, productSans20, productSansLight, productSansMedium,
            tenacity16, tenacity20, tenacity80;

    private static int prevScale;

    static {
        Map<String, java.awt.Font> locationMap = new HashMap<>();

        ScaledResolution sr = new ScaledResolution(mc);

        int scale = sr.getScaleFactor();

        if (scale != prevScale) {
            prevScale = scale;

            regular16 = new FontRenderer(FontUtil.getResource(locationMap, "regular.ttf", 16));
            regular22 = new FontRenderer(FontUtil.getResource(locationMap, "regular.ttf", 22));
            icon20 = new FontRenderer(FontUtil.getResource(locationMap, "icon.ttf", 20));
            productSans16 = new FontRenderer(FontUtil.getResource(locationMap, "product_sans_regular.ttf", 16));
            productSans20 = new FontRenderer(FontUtil.getResource(locationMap, "product_sans_regular.ttf", 20));
            productSansLight = new FontRenderer(FontUtil.getResource(locationMap, "product_sans_light.ttf", 22));
            productSansMedium = new FontRenderer(FontUtil.getResource(locationMap, "product_sans_medium.ttf", 22));
            tenacity16 = new FontRenderer(FontUtil.getResource(locationMap, "tenacity.ttf", 16));
            tenacity20 = new FontRenderer(FontUtil.getResource(locationMap, "tenacity.ttf", 20));
            tenacity80 = new FontRenderer(FontUtil.getResource(locationMap, "tenacity.ttf", 80));
        }
    }
    public static MinecraftFontRenderer getMinecraft() {
        return MinecraftFontRenderer.INSTANCE;
    }

}