package keystrokesmod.utility.packet;

import lombok.Getter;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum OutgoingPackets {
    C00PacketKeepAlive(net.minecraft.network.play.client.C00PacketKeepAlive.class),
    C01PacketChatMessage(net.minecraft.network.play.client.C01PacketChatMessage.class),
    C02PacketUseEntity(net.minecraft.network.play.client.C02PacketUseEntity.class),
    C03PacketPlayer(net.minecraft.network.play.client.C03PacketPlayer.class),
    C04PacketPlayerPosition(net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition.class),
    C05PacketPlayerLook(net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook.class),
    C06PacketPlayerPosLook(net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook.class),
    C07PacketPlayerDigging(net.minecraft.network.play.client.C07PacketPlayerDigging.class),
    C08PacketPlayerBlockPlacement(net.minecraft.network.play.client.C08PacketPlayerBlockPlacement.class),
    C09PacketHeldItemChange(net.minecraft.network.play.client.C09PacketHeldItemChange.class),
    C0APacketAnimation(net.minecraft.network.play.client.C0APacketAnimation.class),
    C0BPacketEntityAction(net.minecraft.network.play.client.C0BPacketEntityAction.class),
    C0CPacketInput(net.minecraft.network.play.client.C0CPacketInput.class),
    C0DPacketCloseWindow(net.minecraft.network.play.client.C0DPacketCloseWindow.class),
    C0EPacketClickWindow(net.minecraft.network.play.client.C0EPacketClickWindow.class),
    C0FPacketConfirmTransaction(net.minecraft.network.play.client.C0FPacketConfirmTransaction.class),
    C10PacketCreativeInventoryAction(net.minecraft.network.play.client.C10PacketCreativeInventoryAction.class),
    C11PacketEnchantItem(net.minecraft.network.play.client.C11PacketEnchantItem.class),
    C12PacketUpdateSign(net.minecraft.network.play.client.C12PacketUpdateSign.class),
    C13PacketPlayerAbilities(net.minecraft.network.play.client.C13PacketPlayerAbilities.class),
    C14PacketTabComplete(net.minecraft.network.play.client.C14PacketTabComplete.class),
    C15PacketClientSettings(net.minecraft.network.play.client.C15PacketClientSettings.class),
    C16PacketClientStatus(net.minecraft.network.play.client.C16PacketClientStatus.class),
    C17PacketCustomPayload(net.minecraft.network.play.client.C17PacketCustomPayload.class),
    C18PacketSpectate(net.minecraft.network.play.client.C18PacketSpectate.class),
    C19PacketResourcePacketStatus(C19PacketResourcePackStatus.class);

    @Getter
    private final Class<? extends Packet<INetHandlerPlayServer>> packetClass;

    OutgoingPackets(Class<? extends Packet<INetHandlerPlayServer>> packetClass) {
        this.packetClass = packetClass;
    }

    @Getter
    private static final List<Class<? extends Packet<INetHandlerPlayServer>>> outgoingPackets =
            Arrays.stream(values()).map(OutgoingPackets::getPacketClass).collect(Collectors.toList());

}
