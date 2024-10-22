package keystrokesmod.mixins.impl.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.impl.exploit.ExploitFixer;
import keystrokesmod.utility.PacketUtils;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NetworkManager.class, priority = 1001)
public abstract class MixinNetworkManager extends SimpleChannelInboundHandler<Packet<?>> {

    @Shadow private INetHandler packetListener;

    @Inject(method = "sendPacket(Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    public void sendPacket(Packet<?> packet, CallbackInfo ci) {
        if (packet != null) {
            if (PacketUtils.skipSendEvent.contains(packet)) {
                PacketUtils.skipSendEvent.remove(packet);
                return;
            }
        }
        SendPacketEvent sendPacketEvent = new SendPacketEvent(packet);
        MinecraftForge.EVENT_BUS.post(sendPacketEvent);

        if (sendPacketEvent.isCanceled()) {
            ci.cancel();
        }
    }

    @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V", at = @At("HEAD"), cancellable = true)
    public void receivePacket(ChannelHandlerContext p_channelRead0_1_, Packet<?> packet, CallbackInfo ci) {
        if (packet != null) {
            if (PacketUtils.skipReceiveEvent.contains(packet)) {
                PacketUtils.skipReceiveEvent.remove(packet);
                return;
            }
        }
        ReceivePacketEvent receivePacketEvent = new ReceivePacketEvent(packet);
        MinecraftForge.EVENT_BUS.post(receivePacketEvent);

        if (receivePacketEvent.isCanceled()) {
            ci.cancel();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Redirect(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/Packet;processPacket(Lnet/minecraft/network/INetHandler;)V"))
    public void onProcessPacket(Packet instance, INetHandler handler) {
        try {
            instance.processPacket(this.packetListener);
        } catch (ThreadQuickExitException e) {
            throw e;
        } catch (Exception e) {
            ExploitFixer.onBadPacket(instance, e);
        }
    }

}
