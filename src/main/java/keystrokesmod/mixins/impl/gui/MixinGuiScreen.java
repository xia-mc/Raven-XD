package keystrokesmod.mixins.impl.gui;

import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.player.ChestStealer;
import keystrokesmod.module.impl.render.NoBackground;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.render.BackgroundUtils;
import net.minecraft.client.gui.GuiScreen;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiScreen.class)
public abstract class MixinGuiScreen {

    @Inject(method = "drawDefaultBackground", at = @At("HEAD"), cancellable = true)
    public void onDrawDefaultBackground(CallbackInfo ci) {
        if (Utils.nullCheck() && (NoBackground.noRender() || ChestStealer.noChestRender()))
            ci.cancel();
    }

    @Inject(method = "drawBackground", at = @At("HEAD"), cancellable = true)
    public void onDrawBackground(int p_drawWorldBackground_1_, @NotNull CallbackInfo ci) {
        if (!ModuleManager.clientTheme.isEnabled() || !ModuleManager.clientTheme.background.isToggled())
            return;

        BackgroundUtils.renderBackground((GuiScreen) (Object) this);
        ci.cancel();
    }
}
