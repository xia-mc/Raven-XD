package keystrokesmod.utility.gapple;

import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;

public class Class211 {
    public static int noMovePackets = 0;

    public static void packetEvent(Packet<?> packet) {
        if (packet instanceof C03PacketPlayer) {
            noMovePackets = ((C03PacketPlayer)packet).isMoving() ? 0 : noMovePackets + 1;
        }
    }
}
