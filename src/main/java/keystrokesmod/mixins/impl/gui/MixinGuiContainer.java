package keystrokesmod.mixins.impl.gui;

import keystrokesmod.event.RenderContainerEvent;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiContainer.class)
public abstract class MixinGuiContainer {

    @Inject(method = "drawScreen", at = @At("HEAD"), cancellable = true)
    public void onDrawScreen(int p_drawScreen_1_, int p_drawScreen_2_, float p_drawScreen_3_, CallbackInfo ci) {
        RenderContainerEvent event = new RenderContainerEvent();
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled())
            ci.cancel();
    }
}
