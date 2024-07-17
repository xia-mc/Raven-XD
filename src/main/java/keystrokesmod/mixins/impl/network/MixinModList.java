package keystrokesmod.mixins.impl.network;

import io.netty.buffer.ByteBuf;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.exploit.ModSpoofer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Map;

@Mixin(FMLHandshakeMessage.ModList.class)
public abstract class MixinModList {
    @Shadow(remap = false)
    private Map<String, String> modTags;

    @Inject(method = "toBytes", at = @At(value = "HEAD"), cancellable = true, remap = false)
    public void toBytes(ByteBuf buffer, CallbackInfo callbackInfo) {
        if (Minecraft.getMinecraft().isSingleplayer() || !ModuleManager.modSpoofer.isEnabled()) return;

        callbackInfo.cancel();

        if (!ModSpoofer.cancel.isToggled()) {
            ArrayList<Map.Entry<String, String>> shownTags = new ArrayList<>();
            for (Map.Entry<String, String> modTag : this.modTags.entrySet()) {
                if (!ModSpoofer.filteredMod.contains(modTag.getKey())) {
                    shownTags.add(modTag);
                }
            }

            ByteBufUtils.writeVarInt(buffer, shownTags.size(), 2);

            for (Map.Entry<String, String> modTag : shownTags) {
                ByteBufUtils.writeUTF8String(buffer, modTag.getKey());
                ByteBufUtils.writeUTF8String(buffer, modTag.getValue());
            }
        }
    }
}