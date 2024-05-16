package keystrokesmod.module.impl.other;

import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.event.HoverEvent;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.util.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class ViewPackets extends Module {
    private final ButtonSetting includeCancelled;
    private final ButtonSetting singlePlayer;
    private final ButtonSetting sent;
    private final ButtonSetting ignoreC00;
    private final ButtonSetting ignoreC03;
    private final ButtonSetting compactC03;
    private final ButtonSetting ignoreC0F;
    private final ButtonSetting received;
    private Packet packet;
    private long tick;

    public ViewPackets() {
        super("View Packets", category.other);
        this.registerSetting(includeCancelled = new ButtonSetting("Include cancelled", true));
        this.registerSetting(singlePlayer = new ButtonSetting("Singleplayer", false));
        this.registerSetting(sent = new ButtonSetting("Sent", false));
        this.registerSetting(ignoreC00 = new ButtonSetting("Ignore C00", false));
        this.registerSetting(ignoreC03 = new ButtonSetting("Ignore C03", false));
        this.registerSetting(compactC03 = new ButtonSetting("Compact C03", false));
        this.registerSetting(ignoreC0F = new ButtonSetting("Ignore C0F", false));
        this.registerSetting(received = new ButtonSetting("Received", false));
    }

    public void onDisable() {
        packet = null;
        tick = 0;
    }

    private static String formatBoolean(final boolean b) {
        return b ? "&atrue" : "&cfalse";
    }

    private void sendMessage(final Packet packet, final boolean b) {
        if (!Utils.nullCheck()) {
            return;
        }
        final String s = b ? ("&a" + packet.getClass().getSimpleName()) : applyInfo(packet);
        final String string = ((compactC03.isToggled() && packet instanceof C03PacketPlayer) ? "&6" : "&d") + packet.getClass().getSimpleName();
        final ChatComponentText chatComponentText = new ChatComponentText(Utils.formatColor("&7[&dR&7]&r &7" + (b ? "Received" : "Sent") + " packet (t:&b" + tick + "&7): "));
        final ChatStyle chatStyle = new ChatStyle();
        chatStyle.setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(Utils.formatColor(s))));
        ((IChatComponent)chatComponentText).appendSibling(new ChatComponentText(Utils.formatColor(string)).setChatStyle(chatStyle));
        mc.thePlayer.addChatMessage(chatComponentText);
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent e) {
        if (!sent.isToggled()) {
            return;
        }
        if (singlePlayer.isToggled() && mc.isSingleplayer() && e.getPacket().getClass().getSimpleName().charAt(0) == 'S') {
            return;
        }
        if (e.isCanceled() && !includeCancelled.isToggled()) {
            return;
        }
        if (ignoreC00.isToggled() && e.getPacket() instanceof C00PacketKeepAlive) {
            return;
        }
        if (ignoreC0F.isToggled() && e.getPacket() instanceof C0FPacketConfirmTransaction) {
            return;
        }
        if (e.getPacket() instanceof C03PacketPlayer && (ignoreC03.isToggled() || (compactC03.isToggled() && (packet == null || packet instanceof C03PacketPlayer)))) {
            return;
        }
        sendMessage(packet = e.getPacket(), false);
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent e) {
        if (!received.isToggled()) {
            return;
        }
        if (singlePlayer.isToggled() && mc.isSingleplayer() && e.getPacket().getClass().getSimpleName().charAt(0) == 'C') {
            return;
        }
        sendMessage(e.getPacket(), true);
    }

    private String applyInfo(final Packet packet) {
        String s = "&a" + packet.getClass().getSimpleName();
        if (packet instanceof C07PacketPlayerDigging) {
            final C07PacketPlayerDigging c07PacketPlayerDigging = (C07PacketPlayerDigging)packet;
            final String string = s + "\n&7Status: &b" + c07PacketPlayerDigging.getStatus().name() + "\n&7Facing: &b" + c07PacketPlayerDigging.getFacing().name();
            final BlockPos getPosition = c07PacketPlayerDigging.getPosition();
            s = string + "\n&7Position: &b" + getPosition.getX() + "&7, &b" + getPosition.getY() + "&7, &b" + getPosition.getZ();
        }
        else if (packet instanceof C09PacketHeldItemChange) {
            s = s + "\n&7Swap to slot: &b" + ((C09PacketHeldItemChange)packet).getSlotId();
        }
        else if (packet instanceof C0BPacketEntityAction) {
            s = s + "\n&7Action: &b" + ((C0BPacketEntityAction)packet).getAction().name() + "\n&7Aux data: &b" + ((C0BPacketEntityAction)packet).getAuxData();
        }
        else if (packet instanceof C08PacketPlayerBlockPlacement) {
            final C08PacketPlayerBlockPlacement c08PacketPlayerBlockPlacement = (C08PacketPlayerBlockPlacement)packet;
            final String string2 = s + "\n&7Item: &b" + ((c08PacketPlayerBlockPlacement.getStack() == null) ? "null" : c08PacketPlayerBlockPlacement.getStack().getItem().getRegistryName().replace("minecraft:", "")) + "\n&7Direction: &b" + c08PacketPlayerBlockPlacement.getPlacedBlockDirection();
            final BlockPos getPosition = c08PacketPlayerBlockPlacement.getPosition();
            s = string2 + "\n&7Position: &b" + getPosition.getX() + "&7, &b" + getPosition.getY() + "&7, &b" + getPosition.getZ() + "\n&7Offset: &b" + round(c08PacketPlayerBlockPlacement.getPlacedBlockOffsetX()) + "&7, &b" + round(c08PacketPlayerBlockPlacement.getPlacedBlockOffsetY()) + "&7, &b" + round(c08PacketPlayerBlockPlacement.getPlacedBlockOffsetZ());
        }
        else if (packet instanceof C02PacketUseEntity) {
            final C02PacketUseEntity c02PacketUseEntity = (C02PacketUseEntity)packet;
            final String string3 = s + "\n&7Action: &b" + c02PacketUseEntity.getAction().name();
            final Entity getEntityFromWorld = c02PacketUseEntity.getEntityFromWorld(mc.theWorld);
            final String string4 = string3 + "\n&7Target: &b" + ((getEntityFromWorld == null) ? "null" : getEntityFromWorld.getName());
            final Vec3 getHitVec = c02PacketUseEntity.getHitVec();
            if (getHitVec == null) {
                s = string4 + "\n&7Hit vec: &bnull";
            }
            else {
                s = string4 + "\n&7Hit vec: &b" + round(getHitVec.xCoord) + "&7, &b" + round(getHitVec.yCoord) + "&7, &b" + round(getHitVec.zCoord);
            }
        }
        else if (packet instanceof C01PacketChatMessage) {
            s = s + "\n&7Length: &b" + ((C01PacketChatMessage)packet).getMessage().length();
        }
        else if (packet instanceof C17PacketCustomPayload) {
            s = s + "\n&7Channel: &b" + ((C17PacketCustomPayload)packet).getChannelName();
        }
        else if (packet instanceof C15PacketClientSettings) {
            s = s + "\n&7Language: &b" + ((C15PacketClientSettings)packet).getLang() + "\n&7Chat visibility: &b" + ((C15PacketClientSettings)packet).getChatVisibility().name();
        }
        else if (packet instanceof C00PacketKeepAlive) {
            s = s + "\n&7Key: &b" + ((C00PacketKeepAlive)packet).getKey();
        }
        else if (packet instanceof C16PacketClientStatus) {
            s = s + "\n&7Status: &b" + ((C16PacketClientStatus)packet).getStatus().name();
        }
        else if (packet instanceof C10PacketCreativeInventoryAction) {
            s = s + "\n&7Slot: &b" + ((C10PacketCreativeInventoryAction)packet).getSlotId() + "\n&7Item: &b" + ((((C10PacketCreativeInventoryAction)packet).getStack() == null) ? "null" : ((C10PacketCreativeInventoryAction)packet).getStack().getItem().getRegistryName().replace("minecraft:", ""));
        }
        else if (packet instanceof C0EPacketClickWindow) {
            final C0EPacketClickWindow c0EPacketClickWindow = (C0EPacketClickWindow)packet;
            s = s + "\n&7Window: &b" + c0EPacketClickWindow.getWindowId() + "\n&7Slot: &b" + c0EPacketClickWindow.getSlotId() + "\n&7Button: &b" + c0EPacketClickWindow.getUsedButton() + "\n&7Action: &b" + c0EPacketClickWindow.getActionNumber() + "\n&7Mode: &b" + c0EPacketClickWindow.getMode() + "\n&7Item: &b" + ((c0EPacketClickWindow.getClickedItem() == null) ? "null" : c0EPacketClickWindow.getClickedItem().getItem().getRegistryName().replace("minecraft:", ""));
        }
        else if (packet instanceof C0FPacketConfirmTransaction) {
            s = s + "\n&7Window: &b" + ((C0FPacketConfirmTransaction)packet).getWindowId() + "\n&7Uid: &b" + ((C0FPacketConfirmTransaction)packet).getUid();
        }
        else if (packet instanceof C03PacketPlayer) {
            final C03PacketPlayer c03PacketPlayer = (C03PacketPlayer)packet;
            s = s + "\n&7Position: &b" + round(c03PacketPlayer.getPositionX()) + "&7, &b" + round(c03PacketPlayer.getPositionY()) + "&7, &b" + round(c03PacketPlayer.getPositionZ()) + "\n&7Rotations: &b" + round(c03PacketPlayer.getYaw()) + "&7, &b" + round(c03PacketPlayer.getPitch()) + "\n&7Ground: " + formatBoolean(c03PacketPlayer.isOnGround()) + "\n&7Moving: " + formatBoolean(c03PacketPlayer.isMoving()) + "\n&7Rotating: " + formatBoolean(c03PacketPlayer.getRotating());
        }
        return s + "\n&7Client tick: &e" + tick;
    }

    private static double round(final double n) {
        return Utils.rnd(n, 3);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.START) {
            ++tick;
        }
    }
}
