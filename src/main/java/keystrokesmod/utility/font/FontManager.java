package keystrokesmod.utility.font;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIntImmutablePair;
import keystrokesmod.utility.font.impl.FontRenderer;
import keystrokesmod.utility.font.impl.FontUtil;
import keystrokesmod.utility.font.impl.MinecraftFontRenderer;
import lombok.Getter;
import net.minecraft.client.gui.ScaledResolution;
import org.jetbrains.annotations.NotNull;

import static keystrokesmod.Raven.mc;

import java.util.Map;

public class FontManager {
    public static FontRenderer
            regular16, regular22,
            icon20,
            productSans16, productSans20, productSansLight, productSansMedium,
            tenacity16, tenacity20, tenacity80;

    private static int prevScale;
    private static final Map<ObjectIntImmutablePair<Fonts>, FontRenderer> fontsMap = new Object2ObjectOpenHashMap<>(10);

    static {

        ScaledResolution sr = new ScaledResolution(mc);

        int scale = sr.getScaleFactor();

        if (scale != prevScale) {
            prevScale = scale;

            regular16 = getFont(Fonts.REGULAR, 16);
            regular22 = getFont(Fonts.REGULAR, 22);
            icon20 = getFont(Fonts.ICON, 20);
            productSans16 = getFont(Fonts.PRODUCT_SANS_REGULAR, 16);
            productSans20 = getFont(Fonts.PRODUCT_SANS_REGULAR, 20);
            productSansLight = getFont(Fonts.PRODUCT_SANS_LIGHT, 22);
            productSansMedium = getFont(Fonts.PRODUCT_SANS_MEDIUM, 22);
            tenacity16 = getFont(Fonts.PRODUCT_SANS_REGULAR, 16);
            tenacity20 = getFont(Fonts.TENACITY, 20);
            tenacity80 = getFont(Fonts.TENACITY, 80);
        }
    }

    public static FontRenderer getFont(Fonts font, int size) {
        final ObjectIntImmutablePair<Fonts> data = ObjectIntImmutablePair.of(font, size);
        if (fontsMap.containsKey(data)) {
            return fontsMap.get(data);
        } else {
            FontRenderer renderer = new FontRenderer(FontUtil.getResource(font.toString(), size));
            fontsMap.put(data, renderer);
            return renderer;
        }
    }

    public static MinecraftFontRenderer getMinecraft() {
        return MinecraftFontRenderer.INSTANCE;
    }

    public enum Fonts {
        REGULAR("Regular", "regular.ttf"),
        ICON("Icon", "icon.ttf"),
        PRODUCT_SANS_REGULAR("ProductSans regular", "product_sans_regular.ttf"),
        PRODUCT_SANS_LIGHT("ProductSans light", "product_sans_light.ttf"),
        PRODUCT_SANS_MEDIUM("ProductSans medium", "product_sans_medium.ttf"),
        TENACITY("Tenacity", "tenacity.ttf"),
        MAPLESTORY("Maplestory", "MAPLESTORY_OTF_BOLD.OTF");

        @Getter
        private final String prettyName;
        private final String filename;

        Fonts(String prettyName, String filename) {
            this.prettyName = prettyName;
            this.filename = filename;
        }

        @Override
        public @NotNull String toString() {
            return filename;
        }
    }
}