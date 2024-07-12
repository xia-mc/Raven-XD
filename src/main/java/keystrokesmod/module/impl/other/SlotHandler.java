package keystrokesmod.module.impl.other;

import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.utils.ModeOnly;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

public final class SlotHandler extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", new String[]{"Default", "Silent"}, 0);
    private final SliderSetting switchBackDelay = new SliderSetting("Switch back delay", 100, 0, 1000, 10, "ms", new ModeOnly(mode, 1));

    private static @Nullable Integer currentSlot = null;
    private static long lastSetCurrentSlotTime = -1;

    public SlotHandler() {
        super("SlotHandler", category.other);
        this.registerSetting(mode, switchBackDelay);
        this.canBeEnabled = false;
    }

    public static int getCurrentSlot() {
        if (currentSlot != null)
            return currentSlot;
        return mc.thePlayer.inventory.currentItem;
    }

    public static @Nullable ItemStack getHeldItem() {
        InventoryPlayer inventory = mc.thePlayer.inventory;
        if (currentSlot != null)
            return currentSlot < 9 && currentSlot >= 0 ? inventory.mainInventory[currentSlot] : null;
        return inventory.currentItem < 9 && inventory.currentItem >= 0 ? inventory.mainInventory[inventory.currentItem] : null;
    }

    public static void setCurrentSlot(int slot) {
        if (slot != -1) {
            currentSlot = slot;
            lastSetCurrentSlotTime = System.currentTimeMillis();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPreUpdate(PreUpdateEvent event) {
        switch ((int) mode.getInput()) {
            case 0:
                mc.thePlayer.inventory.currentItem = getCurrentSlot();
                currentSlot = null;
                break;
            case 1:
                if (currentSlot != null && System.currentTimeMillis() - lastSetCurrentSlotTime > switchBackDelay.getInput())
                    currentSlot = null;
                break;
        }
    }
}
