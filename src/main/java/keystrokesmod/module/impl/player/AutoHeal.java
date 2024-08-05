package keystrokesmod.module.impl.player;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.utility.ContainerUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemAppleGold
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AutoHeal extends Module {
    private final ModeSetting item;
    private final ButtonSetting autoThrow;
    private final SliderSetting minHealth;
    private final SliderSetting healDelay;
    private final SliderSetting startDelay;
    private long lastHeal = -1;
    private long lastSwitchTo = -1;
    private long lastDoneUse = -1;
    private int originalSlot = -1;
    public AutoHeal() {
        super("AutoHeal", category.player);
        this.registerSetting(new DescriptionSetting("help you win by auto use healing item."));
        this.registerSetting(item = new ModeSetting("Item", new String[]{"Golden head", "Soup", "Golden apple"}, 0));
        this.registerSetting(autoThrow = new ButtonSetting("Auto throw", false, new ModeOnly(item, 1)));
        this.registerSetting(minHealth = new SliderSetting("Min health", 10, 0, 20, 1));
        this.registerSetting(healDelay = new SliderSetting("Heal delay", 500, 0, 1500, 1));
        this.registerSetting(startDelay = new SliderSetting("Start delay", 0, 0, 300, 1));
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event) {
        if (!Utils.nullCheck() || mc.thePlayer.isDead) return;
        if (System.currentTimeMillis() - lastHeal < healDelay.getInput()) return;

        if (mc.thePlayer.getHealth() <= minHealth.getInput()) {
            if (lastSwitchTo == -1) {
                int toSlot;
                switch ((int) item.getInput()) {
                    default:
                    case 0:
                        toSlot = ContainerUtils.getSlot(ItemSkull.class);
                        break;
                    case 1:
                        toSlot = ContainerUtils.getSlot(ItemSoup.class);
                        break;
                    case 2:
                        toSlot = ContainerUtils.getSlot(ItemAppleGold.class);
                }

                if (toSlot == -1) return;

                originalSlot = mc.thePlayer.inventory.currentItem;
                SlotHandler.setCurrentSlot(toSlot);
                lastSwitchTo = System.currentTimeMillis();
            }
        }

        if (lastSwitchTo != -1) {
            if (lastDoneUse == -1) {
                if (System.currentTimeMillis() - lastSwitchTo < startDelay.getInput()) return;
                mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, SlotHandler.getHeldItem());
                lastDoneUse = System.currentTimeMillis();
            } else {
                if (item.getInput() == 1 && autoThrow.isToggled()) {
                    mc.playerController.sendPacketDropItem(SlotHandler.getHeldItem());
                }

                if (originalSlot != -1) {
                    SlotHandler.setCurrentSlot(originalSlot);
                    originalSlot = -1;
                }

                lastSwitchTo = -1;
                lastDoneUse = -1;
                lastHeal = System.currentTimeMillis();
            }
        }
    }
}
