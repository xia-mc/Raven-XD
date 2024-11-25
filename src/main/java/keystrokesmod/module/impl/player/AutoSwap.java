package keystrokesmod.module.impl.player;

import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoSwap extends Module {
    private final ButtonSetting sameType;
    private final ButtonSetting swapToGreaterStack;

    public AutoSwap() {
        super("AutoSwap", category.player);
        this.registerSetting(new DescriptionSetting("Automatically swaps blocks."));
        this.registerSetting(sameType = new ButtonSetting("Only same type", true));
        this.registerSetting(swapToGreaterStack = new ButtonSetting("Swap to greater stack", true));
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent e) {
        ItemStack heldItem = mc.thePlayer.getHeldItem();
        int biggestStack = getSlot(heldItem);
        if (biggestStack == -1) {
            return;
        }
        mc.thePlayer.inventory.currentItem = biggestStack;
    }

    private int getSlot(final ItemStack stack2) {
        int slot = -1;
        int highestStack = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack itemStack = mc.thePlayer.inventory.mainInventory[i];
            if (itemStack != null && itemStack.getItem() instanceof ItemBlock && itemStack.stackSize > 0) {
                if (stack2 != null && sameType.isToggled() && !(itemStack.getItem().getClass().equals(stack2.getItem().getClass()))) {
                    continue;
                }
                if (mc.thePlayer.inventory.mainInventory[i].stackSize > highestStack || !swapToGreaterStack.isToggled()) {
                    highestStack = mc.thePlayer.inventory.mainInventory[i].stackSize;
                    slot = i;
                }
            }
        }
        return slot;
    }
}
