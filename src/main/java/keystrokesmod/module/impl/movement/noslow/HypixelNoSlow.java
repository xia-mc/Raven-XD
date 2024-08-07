package keystrokesmod.module.impl.movement.noslow;

import keystrokesmod.Raven;
import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.impl.movement.NoSlow;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.utility.ContainerUtils;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HypixelNoSlow extends INoSlow {
    private final ModeSetting swordMode;

    private int offGroundTicks = 0;
    private boolean send = false;

    public HypixelNoSlow(String name, @NotNull NoSlow parent) {
        super(name, parent);
        this.registerSetting(swordMode = new ModeSetting("Sword mode", new String[]{"Switch", "Test1", "Test2"}, 0));
    }

    @Override
    public void onUpdate() {
        if (!mc.thePlayer.isUsingItem() || SlotHandler.getHeldItem() == null) return;

        if (SlotHandler.getHeldItem().getItem() instanceof ItemSword) {
            switch ((int) swordMode.getInput()) {
                case 0:
                    PacketUtils.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                    PacketUtils.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                    break;
                case 1:
                    if (mc.thePlayer.ticksExisted % 2 == 0 && !Raven.badPacketsHandler.C07)
                        PacketUtils.sendPacket(new C08PacketPlayerBlockPlacement(null));
                    break;
                case 2:
                    PacketUtils.sendPacket(new C02PacketUseEntity());
                    break;
            }
        } else {
            if (mc.thePlayer.ticksExisted % 3 == 0 && !Raven.badPacketsHandler.C07) {
                PacketUtils.sendPacket(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 1, null, 0, 0, 0));
            }
        }
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer.onGround) {
            offGroundTicks = 0;
        } else {
            offGroundTicks++;
        }

        final @Nullable ItemStack item = SlotHandler.getHeldItem();
        if (offGroundTicks == 4 && send) {
            send = false;
            PacketUtils.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(
                    new BlockPos(-1, -1, -1),
                    255, item,
                    0, 0, 0
            ));

        } else if (item != null && mc.thePlayer.isUsingItem()) {
            event.setPosY(event.getPosY() + 1E-14);
        }
    }

    @SubscribeEvent
    public void onSendPacket(@NotNull SendPacketEvent event) {
        if (event.getPacket() instanceof C08PacketPlayerBlockPlacement && !mc.thePlayer.isUsingItem()) {
            C08PacketPlayerBlockPlacement blockPlacement = (C08PacketPlayerBlockPlacement) event.getPacket();
            if (SlotHandler.getHeldItem() != null && blockPlacement.getPlacedBlockDirection() == 255
                    && ContainerUtils.isRest(SlotHandler.getHeldItem().getItem()) && offGroundTicks < 2) {
                if (mc.thePlayer.onGround && !Utils.jumpDown()) {
                    mc.thePlayer.jump();
                }
                send = true;
                event.setCanceled(true);
            }
        }
    }

    @Override
    public float getSlowdown() {
        ItemStack item = SlotHandler.getHeldItem();
        if (item == null) return 1;
        if (item.getItem() instanceof ItemPotion) return .8f;
        return 1;
    }
}
