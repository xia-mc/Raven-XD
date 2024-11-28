package keystrokesmod.module.impl.other;

import keystrokesmod.Raven;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.PacketUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class AutoRespawn extends Module {
    private final SliderSetting delay;

    public AutoRespawn() {
        super("AutoRespawn", category.other);
        this.registerSetting(new DescriptionSetting("Automatically respawns you after you die."));
        this.registerSetting(delay = new SliderSetting("Delay (ms)", 0, 0, 100000, 1000));
    }

    @SubscribeEvent
    public void onReceive(@NotNull ReceivePacketEvent event) {
        if (Minecraft.getMinecraft().thePlayer.isDead) {
            Raven.getExecutor().schedule(() ->
                            PacketUtils.sendPacketNoEvent(new C16PacketClientStatus(C16PacketClientStatus.EnumState.PERFORM_RESPAWN)),
                    (long) delay.getInput(),
                    TimeUnit.MILLISECONDS);
        }
    }

}
