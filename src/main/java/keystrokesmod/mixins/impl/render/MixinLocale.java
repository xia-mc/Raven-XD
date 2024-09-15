package keystrokesmod.mixins.impl.render;


import net.minecraft.client.resources.Locale;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Locale.class, priority = 999)
public abstract class MixinLocale {

    @Inject(method = "checkUnicode", at = @At(value = "FIELD", target = "Lnet/minecraft/client/resources/Locale;unicode:Z", shift = At.Shift.AFTER), cancellable = true)
    private void onCheckUnicode(@NotNull CallbackInfo ci) {
        ci.cancel();
    }
}
