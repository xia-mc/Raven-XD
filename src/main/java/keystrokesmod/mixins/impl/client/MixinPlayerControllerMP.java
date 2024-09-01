package keystrokesmod.mixins.impl.client;


import keystrokesmod.event.BlockPlaceEvent;
import keystrokesmod.module.impl.other.SlotHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
public abstract class MixinPlayerControllerMP {

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

    @Inject(method = "onPlayerRightClick", at = @At("RETURN"))
    private void onPlayerRightClick(EntityPlayerSP thePlayer, WorldClient theWorld,
                                    ItemStack itemStack, BlockPos blockPos, EnumFacing enumFacing,
                                    Vec3 hitPos, @NotNull CallbackInfoReturnable<Boolean> cir
    ) {
        if (cir.getReturnValue())
            MinecraftForge.EVENT_BUS.post(new BlockPlaceEvent());
    }
}
