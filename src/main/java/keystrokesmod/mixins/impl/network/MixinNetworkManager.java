package keystrokesmod.mixins.impl.network;

import com.mojang.realmsclient.gui.ChatFormatting;
import io.netty.channel.ChannelHandlerContext;
import keystrokesmod.Raven;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.client.Notifications;
import keystrokesmod.module.impl.exploit.ExploitFixer;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.ThreadQuickExitException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(value = NetworkManager.class, priority = 1001)
public abstract class MixinNetworkManager {
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @Shadow public abstract boolean isChannelOpen();

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    public void sendPacket(Packet<?> p_sendPacket_1_, CallbackInfo ci) {
        if (Raven.mc.thePlayer == null || Raven.mc.theWorld == null || !this.isChannelOpen()) return;
        if (PacketUtils.skipSendEvent.contains(p_sendPacket_1_)) {
            PacketUtils.skipSendEvent.remove(p_sendPacket_1_);
            return;
        }
        SendPacketEvent sendPacketEvent = new SendPacketEvent(p_sendPacket_1_);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(sendPacketEvent);

        if (sendPacketEvent.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    public void receivePacket(ChannelHandlerContext p_channelRead0_1_, Packet<?> p_channelRead0_2_, CallbackInfo ci) {
        try {
            if (Raven.mc.thePlayer == null || Raven.mc.theWorld == null || !this.isChannelOpen()) return;
            if (PacketUtils.skipReceiveEvent.contains(p_channelRead0_2_)) {
                PacketUtils.skipReceiveEvent.remove(p_channelRead0_2_);
                return;
            }
            ReceivePacketEvent receivePacketEvent = new ReceivePacketEvent(p_channelRead0_2_);
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(receivePacketEvent);

            if (receivePacketEvent.isCanceled()) {
                ci.cancel();
            }
        } catch (Exception ignored) {
        }
    }

    @Redirect(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Packet;processPacket(Lnet/minecraft/network/INetHandler;)V"))
    public void onProcessPacket(Packet<?> instance, INetHandler handler) {
        try {
            ((Packet<INetHandler>) instance).processPacket(handler);
        } catch (ThreadQuickExitException ignored) {  // 😅 Minecraft wtf
        } catch (Exception e) {
            try {
                if (ModuleManager.exploitFixer != null && ModuleManager.exploitFixer.isEnabled() && ExploitFixer.safePacketProcess != null && ExploitFixer.safePacketProcess.isToggled()) {
                    final StringBuilder stackTraces = new StringBuilder();

                    Arrays.stream(e.getStackTrace())
                            .limit(4)
                            .parallel()
                            .map(s -> "\n  " + ChatFormatting.RED + "at " + ChatFormatting.AQUA + s)
                            .forEach(stackTraces::append);

                    Utils.sendMessage(String.format(
                            "%sCatch %s on processing packet <%s>.%s",
                            ChatFormatting.RED, e.getClass(), instance.toString(), stackTraces
                    ));
                }
            } catch (Throwable ignored) {
            }
        }
    }
}
