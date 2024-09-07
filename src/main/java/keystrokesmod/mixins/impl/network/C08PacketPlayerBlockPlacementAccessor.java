package keystrokesmod.mixins.impl.network;


import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(C08PacketPlayerBlockPlacement.class)
public interface C08PacketPlayerBlockPlacementAccessor {

    @Accessor("facingX")
    void setFacingX(float facingX);

    @Accessor("facingY")
    void setFacingY(float facingY);

    @Accessor("facingZ")
    void setFacingZ(float facingZ);
}
