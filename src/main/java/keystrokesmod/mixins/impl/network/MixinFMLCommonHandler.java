package keystrokesmod.mixins.impl.network;

import keystrokesmod.event.ClientBrandEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FMLCommonHandler.class)
public abstract class MixinFMLCommonHandler {

    @Inject(method = "getModName", at = @At("RETURN"), remap = false, cancellable = true)
    private void getModName(@NotNull CallbackInfoReturnable<String> cir) {
        ClientBrandEvent event = new ClientBrandEvent(cir.getReturnValue());
        MinecraftForge.EVENT_BUS.post(event);
        cir.setReturnValue(event.getBrand());
    }
}
