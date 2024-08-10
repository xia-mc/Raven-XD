package keystrokesmod.mixins.impl.gui;


import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiDownloadTerrain.class)
public class MixinGuiDownloadTerrain extends GuiScreen {

    @Inject(method = "keyTyped", at = @At("HEAD"))
    public void onKeyTyped(char typedChar, int keyCode, CallbackInfo ci) {
        if (keyCode == Keyboard.KEY_RETURN) {
            mc.displayGuiScreen(null);

            if (mc.currentScreen == null) {
                mc.setIngameFocus();
            }
        }
    }

    @Inject(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiScreen;drawScreen(IIF)V"))
    public void onDrawScreen(int p_drawScreen_1_, int p_drawScreen_2_, float p_drawScreen_3_, CallbackInfo ci) {
        this.drawCenteredString(this.fontRendererObj, "Press RETURN to force yourself into the server.", this.width / 2, this.height / 2 - 25, 16777215);
    }
}
