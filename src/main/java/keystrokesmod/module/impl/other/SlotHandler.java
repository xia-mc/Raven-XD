package keystrokesmod.module.impl.other;

import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.mixins.impl.client.PlayerControllerMPAccessor;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.utility.CoolDown;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

public final class SlotHandler extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", new String[]{"Default", "Silent"}, 0);
    private final SliderSetting switchBackDelay = new SliderSetting("Switch back delay", 100, 0, 1000, 10, "ms", new ModeOnly(mode, 1));

    private static final AtomicInteger currentSlot = new AtomicInteger(-1);
    private static final CoolDown coolDown = new CoolDown(-1);

    public SlotHandler() {
        super("SlotHandler", category.other);
        this.registerSetting(mode, switchBackDelay);
        this.canBeEnabled = false;
    }

    public static int getCurrentSlot() {
        if (isSilentSlot())
            return currentSlot.get();
        return mc.thePlayer.inventory.currentItem;
    }

    public static @Nullable ItemStack getHeldItem() {
        final InventoryPlayer inventory = mc.thePlayer.inventory;
        if (isSilentSlot())
            return inventory.mainInventory[currentSlot.get()];
        return getRenderHeldItem();
    }

    public static @Nullable ItemStack getRenderHeldItem() {
        final InventoryPlayer inventory = mc.thePlayer.inventory;
        return inventory.currentItem < 9 && inventory.currentItem >= 0 ? inventory.mainInventory[inventory.currentItem] : null;
    }

    public static void setCurrentSlot(int slot) {
        if (slot >= 0 && slot < 9) {
            currentSlot.set(slot);
            coolDown.start();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPreUpdate(PreUpdateEvent event) {
        switch ((int) mode.getInput()) {
            case 0:
                mc.thePlayer.inventory.currentItem = getCurrentSlot();
                resetSlot();
                break;
            case 1:
                coolDown.setCooldown((long) switchBackDelay.getInput());
                if (isSilentSlot()
                        && !((PlayerControllerMPAccessor) mc.playerController).isHittingBlock()
                        && coolDown.hasFinished())
                    resetSlot();
                break;
        }
    }

    public static boolean isSilentSlot() {
        return currentSlot.get() != -1;
    }

    public static void resetSlot() {
        currentSlot.set(-1);
    }
}
