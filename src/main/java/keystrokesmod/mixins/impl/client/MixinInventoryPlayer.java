package keystrokesmod.mixins.impl.client;


import keystrokesmod.module.impl.other.SlotHandler;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryPlayer.class)
public class MixinInventoryPlayer {

    @Inject(method = "getCurrentItem", at = @At("HEAD"), cancellable = true)
    public void getCurrentItem(@NotNull CallbackInfoReturnable<ItemStack> cir) {
        cir.setReturnValue(SlotHandler.getHeldItem());
    }
}
