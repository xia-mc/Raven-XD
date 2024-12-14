package keystrokesmod.utility.render;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import keystrokesmod.utility.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

import static keystrokesmod.Raven.mc;

public class BackgroundUtils {
    public static final ResourceLocation RES_LOGO = new ResourceLocation("keystrokesmod:textures/backgrounds/ravenxd.png");
    private static final List<ResourceLocation> BACKGROUNDS = new ObjectArrayList<>();
    private static final int MAX_INDEX;

    private static long lastRenderTime = -1;
    private static ResourceLocation lastBackground;
    private static int shadow = 0;

    static {
        BACKGROUNDS.add(new ResourceLocation("keystrokesmod:textures/backgrounds/1.png"));
        BACKGROUNDS.add(new ResourceLocation("keystrokesmod:textures/backgrounds/2.png"));
        BACKGROUNDS.add(new ResourceLocation("keystrokesmod:textures/backgrounds/3.png"));
        BACKGROUNDS.add(new ResourceLocation("keystrokesmod:textures/backgrounds/4.png"));
        BACKGROUNDS.add(new ResourceLocation("keystrokesmod:textures/backgrounds/5.png"));
        MAX_INDEX = BACKGROUNDS.size() - 1;

        lastBackground = BACKGROUNDS.get(Utils.randomizeInt(0, MAX_INDEX));
    }

    public static void renderBackground(@NotNull GuiScreen screen) {
        updateShadow(0);
        renderBackground(screen.width, screen.height);
    }

    public static void renderBackground(@NotNull GuiSlot slot) {
        updateShadow(200);
        renderBackground(slot.width, slot.height);
    }

    private static void renderBackground(final int width, final int height) {
        final long time = System.currentTimeMillis();
        if (time - lastRenderTime > 30000) {
            lastBackground = BACKGROUNDS.get(Utils.randomizeInt(0, MAX_INDEX));
        }
        lastRenderTime = time;

        if (!Utils.nullCheck())
            RenderUtils.drawImage(lastBackground, 0, 0, width, height);

        if (shadow != 0) {
            ScaledResolution resolution = new ScaledResolution(mc);
            RenderUtils.drawBloomShadow(-16, -16, resolution.getScaledWidth() + 16, resolution.getScaledHeight() + 16, 4,
                    new Color(0, 0, 0, shadow), false
            );
        }
    }

    private static void updateShadow(final int shadowTarget) {
        if (shadowTarget > shadow) {
            shadow = (int) Math.min(shadow + 4.0 * 300 / Minecraft.getDebugFPS(), shadowTarget);
        } else if (shadowTarget < shadow) {
            shadow = (int) Math.max(shadow - 4.0 * 300 / Minecraft.getDebugFPS(), shadowTarget);
        }
    }

    public static ResourceLocation getLogoPng() {
        return RES_LOGO;
    }
}
