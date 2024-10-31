package keystrokesmod.module.impl.world.scaffold.sprint;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.impl.other.anticheats.utils.world.PlayerMove;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.impl.world.scaffold.IScaffoldSprint;
import keystrokesmod.script.classes.Vec3;
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
            packets.forEach(PacketUtils::sendPacketNoEvent);
            packets.clear();
        }

        mc.thePlayer.motionX *= .8;
        mc.thePlayer.motionZ *= .8;
    }

    @SubscribeEvent
    public void onPreUpdate(PreMotionEvent event) {
        if (getLastDistance() > .22 && mc.thePlayer.ticksExisted % 2 == 0 && mc.thePlayer.onGround) {
            final double xDist = mc.thePlayer.posX - mc.thePlayer.prevPosX;
            final double zDist = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;
            final double multiplier = .5 - getSpeedEffect() * .05;
            final double random = Math.random() * .007;
            event.setPosX(event.getPosX() - xDist * (multiplier + random));
            event.setPosZ(event.getPosZ() - zDist * (multiplier + random));
            event.setPosY(event.getPosY() + .00625 + Math.random() * 1E-3);
        }

        if (mc.thePlayer.onGround) {
            mc.thePlayer.motionX *= 1.114 - getSpeedEffect() * .01 - Math.random() * 1E-4;
            mc.thePlayer.motionZ *= 1.114 - getSpeedEffect() * .01 - Math.random() * 1E-4;
        }

        if (mc.thePlayer.ticksExisted % 2 != 0 && !packets.isEmpty()) {
            packets.forEach(PacketUtils::sendPacketNoEvent);
            packets.clear();
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

    private static double getLastDistance() {
        return PlayerMove.getXzTickSpeed(new Vec3(mc.thePlayer.lastTickPosX, mc.thePlayer.lastTickPosY, mc.thePlayer.lastTickPosZ), new Vec3(mc.thePlayer));
    }

    @Override
    public boolean isSprint() {
        return true;
    }
}
