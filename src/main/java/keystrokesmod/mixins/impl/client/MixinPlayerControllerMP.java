package keystrokesmod.mixins.impl.client;


import keystrokesmod.module.impl.other.SlotHandler;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {

    @Shadow private int currentPlayerItem;

    @Shadow @Final private NetHandlerPlayClient netClientHandler;

    /**
     * @author xia__mc
     * @reason for SlotHandler (silent switch)
     */
    @Inject(method = "syncCurrentPlayItem", at = @At("HEAD"), cancellable = true)
    private void syncCurrentPlayItem(CallbackInfo ci) {
        int i = SlotHandler.getCurrentSlot();
        if (i != this.currentPlayerItem) {
            this.currentPlayerItem = i;
            this.netClientHandler.addToSendQueue(new C09PacketHeldItemChange(this.currentPlayerItem));
        }

        ci.cancel();
    }
}
