package keystrokesmod.module.impl.movement.noslow;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.module.impl.movement.NoSlow;
import keystrokesmod.utility.PacketUtils;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class OldGrimNoSlow extends INoSlow {
    public OldGrimNoSlow(String name, @NotNull NoSlow parent) {
        super(name, parent);
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer.isUsingItem()) {
            PacketUtils.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
            PacketUtils.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 7 + 2));
            PacketUtils.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
        }
    }

    @Override
    public float getSlowdown() {
        return 1;
    }
}
