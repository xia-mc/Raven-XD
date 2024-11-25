package keystrokesmod.module.impl.player;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.ContainerUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AutoHeal extends Module {
    private final ButtonSetting autoDrop;
    private final SliderSetting minHealth;
    private final SliderSetting healDelay;
    private final SliderSetting startDelay;
    private final ButtonSetting goldenHead;
    private final ButtonSetting soup;
    private final ButtonSetting goldenHeadName;
    private long lastHeal = -1;
    private long lastSwitchTo = -1;
    private long lastDoneUse = -1;
    private int originalSlot = -1;

    public AutoHeal() {
        super("AutoHeal", category.player);
        this.registerSetting(new DescriptionSetting("Automatically uses healing items."));
        this.registerSetting(goldenHead = new ButtonSetting("Golden head", true));
        this.registerSetting(goldenHeadName = new ButtonSetting("Check golden Head name", true, goldenHead::isToggled));
        this.registerSetting(soup = new ButtonSetting("Soup", false));
        this.registerSetting(autoDrop = new ButtonSetting("Auto drop", false, soup::isToggled));
        this.registerSetting(minHealth = new SliderSetting("Min health", 10, 0, 20, 1));
        this.registerSetting(healDelay = new SliderSetting("Heal delay", 500, 0, 8500, 1));
        this.registerSetting(startDelay = new SliderSetting("Start delay", 0, 0, 300, 1));
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event) {
        if (!Utils.nullCheck() || mc.thePlayer.isDead || mc.playerController == null) return;
        if (System.currentTimeMillis() - lastHeal < healDelay.getInput()) return;

        if (mc.thePlayer.getHealth() <= minHealth.getInput()) {
            if (lastSwitchTo == -1) {
                int toSlot = -1;

                if (goldenHead.isToggled()) {
                    for (int slot = 0; slot <= 8; slot++) {
                        ItemStack itemInSlot = mc.thePlayer.inventory.getStackInSlot(slot);
                        if (itemInSlot != null && itemInSlot.getItem() instanceof ItemSkull) {

                            if (goldenHeadName.isToggled()) {
                                String displayName = itemInSlot.getDisplayName().toLowerCase();
                                if ((displayName.contains("golden") && displayName.contains("head"))) {
                                    toSlot = slot;
                                    break;
                                }
                            } else {
                                toSlot = slot;
                                break;
                            }
                        }
                    }
                }


                if (toSlot == -1 && soup.isToggled()) {
                    toSlot = ContainerUtils.getSlot(ItemSoup.class);
                }

                if (toSlot == -1) return;

                originalSlot = mc.thePlayer.inventory.currentItem;
                SlotHandler.setCurrentSlot(toSlot);
                lastSwitchTo = System.currentTimeMillis();
            }
        }


        if (lastSwitchTo != -1) {
            ItemStack stack = SlotHandler.getHeldItem();
            if (stack == null) return;

            if (lastDoneUse == -1) {
                if (System.currentTimeMillis() - lastSwitchTo < startDelay.getInput()) return;
                mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, stack);
                lastDoneUse = System.currentTimeMillis();
            } else {
                if (autoDrop.isToggled() && stack.getItem() instanceof ItemSoup) {
                    mc.thePlayer.dropOneItem(true);
                }

                if (originalSlot != -1) {
                    SlotHandler.setCurrentSlot(originalSlot);
                    originalSlot = -1;
                }

                lastSwitchTo = -1;
                lastDoneUse = -1;
                lastHeal = System.currentTimeMillis();

                if (mc.thePlayer.getHealth() <= minHealth.getInput()) {
                    lastSwitchTo = -1;
                }
            }
        }
    }
}
