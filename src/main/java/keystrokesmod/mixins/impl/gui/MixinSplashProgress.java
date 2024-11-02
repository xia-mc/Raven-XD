package keystrokesmod.mixins.impl.gui;


import net.minecraftforge.fml.client.SplashProgress;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SplashProgress.class, remap = false)
public abstract class MixinSplashProgress {

    @Inject(method = "getString", at = @At("HEAD"), cancellable = true)
    private static void onGetString(@NotNull String name, String def, CallbackInfoReturnable<String> cir) {
        if (name.equals("logoTexture") && def.equals("textures/gui/title/mojang.png")) {
            cir.setReturnValue("keystrokesmod:textures/backgrounds/ravenxd.png");
        }
    }
}