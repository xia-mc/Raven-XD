package keystrokesmod.mixins.impl.network;


import net.minecraft.network.play.client.C03PacketPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(C03PacketPlayer.class)
public interface C03PacketPlayerAccessor {

    @Accessor("pitch")
    void setPitch(float pitch);

    @Accessor("rotating")
    boolean isRotating();
}
