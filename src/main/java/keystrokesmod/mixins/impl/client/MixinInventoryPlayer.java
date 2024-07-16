package keystrokesmod.mixins.impl.client;


import keystrokesmod.Raven;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryPlayer.class)
public abstract class MixinInventoryPlayer {

    @Shadow public EntityPlayer player;

    @Shadow public EntityPlayer player;

    @Inject(method = "getCurrentItem", at = @At("HEAD"), cancellable = true)
    public void getCurrentItem(@NotNull CallbackInfoReturnable<ItemStack> cir) {
        if (Utils.nullCheck() && this.player == Raven.mc.thePlayer) {
            cir.setReturnValue(SlotHandler.getHeldItem());
        }
    }
}
