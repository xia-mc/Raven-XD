package keystrokesmod.module.impl.player;

import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.mixins.impl.network.S08PacketPlayerPosLookAccessor;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoRotate extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", new String[]{"Cancel", "Silent"}, 0);

    public NoRotate() {
        super("NoRotate", category.player);
        this.registerSetting(mode);
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent event) {
        if (Utils.nullCheck() && event.getPacket() instanceof S08PacketPlayerPosLook) {
            final S08PacketPlayerPosLook packet = (S08PacketPlayerPosLook) event.getPacket();
            switch ((int) mode.getInput()) {
                case 1:
                    RotationHandler.setRotationYaw(packet.getYaw());
                    RotationHandler.setRotationPitch(packet.getPitch());
                case 0:
                    // Reflection is TOO SLOW
                    final S08PacketPlayerPosLookAccessor p = (S08PacketPlayerPosLookAccessor) packet;
                    p.setYaw(mc.thePlayer.rotationYaw);
                    p.setPitch(mc.thePlayer.rotationPitch);
                    break;
            }
        }
    }
}
