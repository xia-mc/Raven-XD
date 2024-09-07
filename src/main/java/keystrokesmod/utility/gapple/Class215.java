package keystrokesmod.utility.gapple;


import keystrokesmod.mixins.impl.network.C03PacketPlayerAccessor;
import keystrokesmod.module.impl.other.anticheats.utils.phys.Vec2;
import net.minecraft.network.play.client.C03PacketPlayer;
import org.jetbrains.annotations.NotNull;

public class Class215 {
    public static Vec2 currentRotation = new Vec2(0.0f, 0.0f);

    public static void getCurrentRotation(@NotNull C03PacketPlayer rotationPacket) {
        if (((C03PacketPlayerAccessor) rotationPacket).isRotating()) {
            currentRotation = new Vec2(rotationPacket.getYaw(), rotationPacket.getPitch());
        }
    }
}

