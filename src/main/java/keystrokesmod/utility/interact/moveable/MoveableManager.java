package keystrokesmod.utility.interact.moveable;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import keystrokesmod.utility.render.RenderUtils;
import net.minecraft.client.gui.GuiChat;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.Set;

import static keystrokesmod.Raven.mc;

public class MoveableManager {
    private static final int BACKGROUND_COLOR = new Color(0, 0, 0, 80).getRGB();

    private static final Set<Moveable> moveObjs = new ObjectOpenHashSet<>();
    private static int lastX;
    private static int lastY;
    private static boolean lastDragging = false;

    public static void register(Moveable object) {
        moveObjs.add(object);
    }

    public static void unregister(Moveable object) {
        moveObjs.remove(object);
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event) {
        if (!(mc.currentScreen instanceof GuiChat)) return;
        int x = Mouse.getEventX() * mc.currentScreen.width / mc.currentScreen.mc.displayWidth;
        int y = mc.currentScreen.height - Mouse.getEventY() * mc.currentScreen.height / mc.currentScreen.mc.displayHeight - 1;

        boolean hovered = false;
        for (Moveable obj : moveObjs) {
            if (obj.isDisabled()) continue;
            if (!hovered && isHover(obj, x, y)) {
                hovered = true;
                RenderUtils.drawBloomShadow(
                        (float) obj.getMinX() - 4, (float) obj.getMinY() - 4,
                        (float) (obj.getMaxX() - obj.getMinX()) + 8, (float) (obj.getMaxY() - obj.getMinY()) + 8,
                        1, 8, BACKGROUND_COLOR, false);
            }
            obj.render();
        }
    }

    public static void onMouseDrag(int x, int y) {
        if (!lastDragging) {
            lastDragging = true;
            lastX = x;
            lastY = y;
            return;
        }
        for (Moveable obj : moveObjs) {
            if (obj.isDisabled()) continue;
            if (isHover(obj, lastX, lastY)) {
                obj.moveX(x - lastX);
                obj.moveY(y - lastY);
                return;
            }
        }
        lastX = x;
        lastY = y;
    }

    public static void onMouseRelease() {
        lastDragging = false;
    }

    private static boolean isHover(final @NotNull Moveable obj, int x, int y) {
        return obj.getMinX() <= x && obj.getMaxX() >= x && obj.getMinY() <= y && obj.getMaxY() >= y;
    }
}
