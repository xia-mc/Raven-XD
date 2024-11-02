package keystrokesmod.module.impl.world.scaffold.sprint;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.impl.world.scaffold.IScaffoldSprint;
import keystrokesmod.utility.PacketUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author StylesFoundation
 */
public class HypixelSprint extends IScaffoldSprint {
    private final List<Packet<?>> packets = new ArrayList<>();

    public HypixelSprint(String name, @NotNull Scaffold parent) {
        super(name, parent);
    }

    public void onDisable() {
        if (!packets.isEmpty()) {
            packets.forEach(PacketUtils::sendPacket);
            packets.clear();
        }

        mc.thePlayer.motionX *= .8;
        mc.thePlayer.motionZ *= .8;
    }

    @SubscribeEvent
    public void onPreUpdate(PreMotionEvent event) {
        if (mc.thePlayer.onGround) {
            mc.thePlayer.motionX *= 1.114 - getSpeedEffect() * .01 - Math.random() * 1E-4;
            mc.thePlayer.motionZ *= 1.114 - getSpeedEffect() * .01 - Math.random() * 1E-4;
        }

        if (mc.thePlayer.ticksExisted % 2 != 0) {
            synchronized (packets) {
                if (!packets.isEmpty()) {
                    packets.forEach(PacketUtils::sendPacket);
                    packets.clear();
                }
            }
        }
    }

    @SubscribeEvent
    public void onSendPacket(@NotNull SendPacketEvent event) {
        if (mc.thePlayer.onGround && mc.thePlayer.ticksExisted % 2 == 0
                && (event.getPacket() instanceof C08PacketPlayerBlockPlacement
                || event.getPacket() instanceof C0APacketAnimation
                || event.getPacket() instanceof C09PacketHeldItemChange)) {
            packets.add(event.getPacket());
            event.setCanceled(true);
        }
    }

    private int getSpeedEffect() {
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed))
            return mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1;
        return 0;
    }

    @Override
    public boolean isSprint() {
        return true;
    }
}
