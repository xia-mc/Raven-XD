package keystrokesmod.utility;

import keystrokesmod.module.impl.exploit.ExploitFixer;
import keystrokesmod.script.classes.Vec3;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.C03PacketPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static keystrokesmod.Raven.mc;

public class PacketUtils {
    public static List<Packet<INetHandlerPlayServer>> skipSendEvent = new ArrayList<>();
    public static List<Packet<INetHandlerPlayClient>> skipReceiveEvent = new ArrayList<>();

    public static void sendPacketNoEvent(Packet<?> packet) {
        if (packet == null)
            return;
        try {
            Packet<INetHandlerPlayServer> casted = castPacket(packet);
            skipSendEvent.add(casted);
            mc.thePlayer.sendQueue.addToSendQueue(casted);
        } catch (ThreadQuickExitException | ClassCastException ignored) {
        }
    }

    public static void sendPacket(Packet<?> packet) {
        if (packet == null)
            return;
        try {
            Packet<INetHandlerPlayServer> casted = castPacket(packet);
            mc.thePlayer.sendQueue.addToSendQueue(casted);
        } catch (ThreadQuickExitException | ClassCastException ignored) {
        }
    }

    public static void receivePacketNoEvent(Packet<?> packet) {
        if (packet == null)
            return;
        try {
            Packet<INetHandlerPlayClient> casted = castPacket(packet);
            skipReceiveEvent.add(casted);
            casted.processPacket(mc.getNetHandler());
        } catch (ThreadQuickExitException ignored) {
        } catch (Exception e) {
            ExploitFixer.onBadPacket(packet, e);
        }
    }

    public static void receivePacket(Packet<?> packet) {
        if (packet == null)
            return;
        try {
            Packet<INetHandlerPlayClient> casted = castPacket(packet);
            casted.processPacket(mc.getNetHandler());
        } catch (ThreadQuickExitException ignored) {
        } catch (Exception e) {
            ExploitFixer.onBadPacket(packet, e);
        }
    }

    public static @NotNull Optional<Vec3> getPos(Packet<?> packet) {
        if (packet instanceof C03PacketPlayer.C06PacketPlayerPosLook) {
            final C03PacketPlayer.C06PacketPlayerPosLook p = (C03PacketPlayer.C06PacketPlayerPosLook) packet;
            return Optional.of(new Vec3(p.getPositionX(), p.getPositionY(), p.getPositionZ()));
        }
        if (packet instanceof C03PacketPlayer.C04PacketPlayerPosition) {
            final C03PacketPlayer.C04PacketPlayerPosition p = (C03PacketPlayer.C04PacketPlayerPosition) packet;
            return Optional.of(new Vec3(p.getPositionX(), p.getPositionY(), p.getPositionZ()));
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public static <H extends INetHandler> Packet<H> castPacket(Packet<?> packet) throws ClassCastException {
        return (Packet<H>) packet;
    }

}
