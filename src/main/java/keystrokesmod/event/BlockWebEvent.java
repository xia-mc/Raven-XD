package keystrokesmod.event;


import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Getter
@Cancelable
@AllArgsConstructor
public class BlockWebEvent extends Event {
    private final BlockPos blockPos;
    private final IBlockState blockState;
    private final Entity entity;
}
