package keystrokesmod.module.impl.client;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;

public class MiddleClick extends Module {
    int prevSlot;
    public static ModeSetting middleClick;
    private boolean hasClicked;
    private int pearlEvent;

    public MiddleClick() {
        super("MiddleClick", category.client, 0);
        this.registerSetting(middleClick = new ModeSetting("Middle Click", new String[]{"Toggle Friend", "Throw Pearl"}, 0));
    }

    public void onEnable() {
        pearlEvent = 4;
        hasClicked = false;
    }

    @SubscribeEvent
    public void onTick(TickEvent.RenderTickEvent e) {
        if (!Utils.nullCheck())
            return;
        if (pearlEvent < 4) {
            if (pearlEvent == 3) {
                mc.thePlayer.inventory.currentItem = prevSlot;
            }
            pearlEvent++;
        }
        if (Mouse.isButtonDown(2) && !hasClicked) {
            switch ((int) middleClick.getInput()) {
                case 0: {
                    EntityLivingBase g = Utils.raytrace(30);
                    if (g != null && !AntiBot.isBot(g) && !Utils.addFriend(g.getName())) {
                        Utils.removeFriend(g.getName());
                    }
                    break;
                }
                case 1: {
                    for (int slot = 0; slot <= 8; slot++) {
                        ItemStack itemInSlot = mc.thePlayer.inventory.getStackInSlot(slot);
                        if (itemInSlot != null && itemInSlot.getItem() instanceof ItemEnderPearl) {
                            prevSlot = mc.thePlayer.inventory.currentItem;
                            mc.thePlayer.inventory.currentItem = slot;
                            mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, itemInSlot);
                            pearlEvent = 0;
                        }
                    }
                    break;
                }
            }
            hasClicked = true;
        } else if (!Mouse.isButtonDown(2) && hasClicked) {
            hasClicked = false;
        }
    }
}
