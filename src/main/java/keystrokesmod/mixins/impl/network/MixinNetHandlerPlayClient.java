package keystrokesmod.mixins.impl.network;


import keystrokesmod.Raven;
import keystrokesmod.event.PostVelocityEvent;
import keystrokesmod.event.PreVelocityEvent;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.utility.Reflection;
import keystrokesmod.utility.Utils;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public abstract class MixinNetHandlerPlayClient {

    @Inject(method = "handleEntityVelocity", at = @At("HEAD"), cancellable = true)
    public void onPreHandleEntityVelocity(S12PacketEntityVelocity packet, CallbackInfo ci) {
        if (!Utils.nullCheck()) return;

        if (packet.getEntityID() == Raven.mc.thePlayer.getEntityId()) {
            if (ModuleManager.fly.isEnabled() || ModuleManager.longJump.isEnabled()) return;

            PreVelocityEvent event = new PreVelocityEvent(packet.getMotionX(), packet.getMotionY(), packet.getMotionZ());
            MinecraftForge.EVENT_BUS.post(event);
            if (event.isCanceled()) ci.cancel();

            try {
                Reflection.S12PacketEntityVelocityXMotion.set(packet, event.getMotionX());
                Reflection.S12PacketEntityVelocityYMotion.set(packet, event.getMotionY());
                Reflection.S12PacketEntityVelocityZMotion.set(packet, event.getMotionZ());
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    @Inject(method = "handleEntityVelocity", at = @At("RETURN"))
    public void onPostHandleEntityVelocity(S12PacketEntityVelocity packet, CallbackInfo ci) {
        if (!Utils.nullCheck()) return;

        if (packet.getEntityID() == Raven.mc.thePlayer.getEntityId()) {
            MinecraftForge.EVENT_BUS.post(new PostVelocityEvent());
        }
    }
}
