package keystrokesmod.module.impl.other;

import com.mojang.realmsclient.gui.ChatFormatting;
import keystrokesmod.event.PreConnectEvent;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.utility.Utils;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class FlagDetector extends Module {
    public static short counter = 0;

    public FlagDetector() {
        super("FlagDetector", category.other);
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook && mc.thePlayer.ticksExisted > 40) {
            counter++;
            Utils.sendMessage(ChatFormatting.RED + "Flag Detected: " + ChatFormatting.GRAY + counter);
        }
    }

    @SubscribeEvent
    public void onConnect(@NotNull PreConnectEvent event) {
        counter = 0;
    }

    @Override
    public void onUpdate() {
        if (!Utils.nullCheck())
            counter = 0;
    }

    @Override
    public void onEnable() {
        counter = 0;
    }

    @Override
    public void onDisable() {
        counter = 0;
    }
}
