package keystrokesmod.mixins.impl.network;

import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(C07PacketPlayerDigging.class)
public interface C07PacketPlayerDiggingAccessor {

    @Accessor("facing")
    void setFacing(EnumFacing facing);

    @Accessor("position")
    void setPosition(BlockPos facing);
}
