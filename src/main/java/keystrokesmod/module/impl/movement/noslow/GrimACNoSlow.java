package keystrokesmod.module.impl.movement.noslow;

import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.impl.movement.NoSlow;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class GrimACNoSlow extends INoSlow {
    private boolean toCancel = false;

    public GrimACNoSlow(String name, @NotNull NoSlow parent) {
        super(name, parent);
    }

    @SubscribeEvent
    public void onSendPacket(@NotNull SendPacketEvent event) {
        if (canFoodNoSlow()) {
            if (event.getPacket() instanceof C08PacketPlayerBlockPlacement) {
                event.setCanceled(true);
                PacketUtils.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(SlotHandler.getHeldItem()));
                PacketUtils.sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                Utils.sendClick(1, false);
                toCancel = true;
            } else if (toCancel && event.getPacket() instanceof C07PacketPlayerDigging) {
                if (((C07PacketPlayerDigging) event.getPacket()).getStatus() == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM) {
                    toCancel = false;
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        ItemStack itemStack = SlotHandler.getHeldItem();
        if (!mc.thePlayer.isUsingItem() || itemStack == null) {
            toCancel = false;
            return;
        }

        if (NoSlow.bow.isToggled() && itemStack.getItem() instanceof ItemBow) {
            PacketUtils.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
            PacketUtils.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 7 + 2));
            PacketUtils.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
        } else if (NoSlow.sword.isToggled() && itemStack.getItem() instanceof ItemSword) {
            PacketUtils.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
            PacketUtils.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 7 + 2));
            PacketUtils.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
        }
    }

    private boolean canFoodNoSlow() {
        final ItemStack item = SlotHandler.getHeldItem();
        return item != null && item.getItem() instanceof ItemFood && item.stackSize > 2;
    }

    @Override
    public float getSlowdown() {
        ItemStack item = SlotHandler.getHeldItem();
        return item != null && item.getItem() instanceof ItemFood ? .2f : 1;
    }

    @Override
    public void onDisable() throws Throwable {
        toCancel = false;
    }
}
