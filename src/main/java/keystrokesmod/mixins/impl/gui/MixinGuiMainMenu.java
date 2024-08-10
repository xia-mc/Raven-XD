package keystrokesmod.mixins.impl.gui;

import keystrokesmod.Raven;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(GuiMainMenu.class)
public abstract class MixinGuiMainMenu extends GuiScreen {
    @Redirect(method = "drawScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiMainMenu;drawString(Lnet/minecraft/client/gui/FontRenderer;Ljava/lang/String;III)V"))
    public void onDrawScreen(GuiMainMenu instance, FontRenderer fontRenderer, @NotNull String text, int x, int y, int color) {
        if (text.equals("Copyright Mojang AB. Do not distribute!")) {
            text = Raven.moduleCounter + " modules and " + Raven.settingCounter + " settings loaded!";
        }

        this.drawString(fontRenderer, text, x, y, color);
    }
}
