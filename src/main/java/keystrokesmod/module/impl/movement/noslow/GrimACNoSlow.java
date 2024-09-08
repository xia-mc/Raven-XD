package keystrokesmod.module.impl.movement.noslow;

import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.mixins.impl.client.KeyBindingAccessor;
import keystrokesmod.module.impl.movement.NoSlow;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.utility.PacketUtils;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class GrimACNoSlow extends INoSlow {
    public GrimACNoSlow(String name, @NotNull NoSlow parent) {
        super(name, parent);
    }

    @SubscribeEvent
    public void onSendPacket(@NotNull SendPacketEvent event) {
        if (canFoodNoSlow()) {
            if (event.getPacket() instanceof C08PacketPlayerBlockPlacement) {
                event.setCanceled(true);
                PacketUtils.sendPacketNoEvent(event.getPacket());
                PacketUtils.sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                ((KeyBindingAccessor) mc.gameSettings.keyBindUseItem).setPressed(false);
            } else if (event.getPacket() instanceof C07PacketPlayerDigging) {
                if (((C07PacketPlayerDigging) event.getPacket()).getStatus() == C07PacketPlayerDigging.Action.RELEASE_USE_ITEM)
                    event.setCanceled(true);
            }
        }
    }

    private boolean canFoodNoSlow() {
        final ItemStack item = SlotHandler.getHeldItem();
        return item != null && item.getItem() instanceof ItemFood && item.stackSize > 1;
    }

    @Override
    public float getSlowdown() {
        ItemStack item = SlotHandler.getHeldItem();
        return item != null && item.getItem() instanceof ItemFood ? .2f : 1;
    }
}
