package keystrokesmod.module.impl.other;

import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.utility.PacketUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C16PacketClientStatus;
import org.jetbrains.annotations.NotNull;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoRespawn extends Module{
    public AutoRespawn() {
        super("AutoRespawn", category.other);
        this.registerSetting(new DescriptionSetting("Automatically respawn."));
    }
   @SubscribeEvent
    public void onReceive(@NotNull ReceivePacketEvent event) {
        if(Minecraft.getMinecraft().thePlayer.isDead){
            PacketUtils.sendPacketNoEvent(new C16PacketClientStatus(C16PacketClientStatus.EnumState.PERFORM_RESPAWN));
        }
    };
}
