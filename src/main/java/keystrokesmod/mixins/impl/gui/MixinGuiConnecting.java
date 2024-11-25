package keystrokesmod.mixins.impl.gui;

import keystrokesmod.event.PreConnectEvent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.NetworkManager;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiConnecting.class)
public abstract class MixinGuiConnecting extends GuiScreen {
    @Unique
    public PreConnectEvent raven_bS$preConnectEvent = null;
    @Shadow
    private NetworkManager networkManager;

    @Inject(method = "connect", at = @At("HEAD"), cancellable = true)
    public void onConnect(String p_connect_1_, int p_connect_2_, CallbackInfo ci) {
        raven_bS$preConnectEvent = new PreConnectEvent((GuiConnecting) (Object) this, p_connect_1_, p_connect_2_);
        MinecraftForge.EVENT_BUS.post(raven_bS$preConnectEvent);
        if (raven_bS$preConnectEvent.isCanceled()) {
            ci.cancel();
        }
    }

    /**
     * @author xia__mc
     * @reason for NyaProxy module
     */
    @Overwrite
    public void drawScreen(int p_drawScreen_1_, int p_drawScreen_2_, float p_drawScreen_3_) {
        String msg = this.networkManager == null ? I18n.format("connect.connecting") : I18n.format("connect.authorizing");
        if (raven_bS$preConnectEvent != null) {
            msg = raven_bS$preConnectEvent.getExtraMessage().toString(msg);
        }

        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, msg, this.width / 2, this.height / 2 - 50, 16777215);

        super.drawScreen(p_drawScreen_1_, p_drawScreen_2_, p_drawScreen_3_);
    }
}
