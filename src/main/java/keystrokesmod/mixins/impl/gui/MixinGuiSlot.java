package keystrokesmod.mixins.impl.gui;


import keystrokesmod.module.ModuleManager;
import keystrokesmod.utility.render.BackgroundUtils;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.Tessellator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiSlot.class)
public class MixinGuiSlot {

    @Inject(method = "drawContainerBackground", at = @At("HEAD"), cancellable = true, remap = false)
    public void onDrawContainerBackground(Tessellator p_drawContainerBackground_1_, CallbackInfo ci) {
        if (!ModuleManager.clientTheme.isEnabled() || !ModuleManager.clientTheme.background.isToggled())
            return;

        BackgroundUtils.renderBackground((GuiSlot) (Object) this);

        ci.cancel();
    }
}
