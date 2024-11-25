package keystrokesmod.event;

import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class BlockAABBEvent extends Event {
    private final World world;
    private final Block block;
    private final BlockPos blockPos;
    private final AxisAlignedBB maskBoundingBox;
    private AxisAlignedBB boundingBox;

    public BlockAABBEvent(World world, Block block, BlockPos blockPos, AxisAlignedBB boundingBox, AxisAlignedBB maskBoundingBox) {
        this.world = world;
        this.block = block;
        this.blockPos = blockPos;
        this.boundingBox = boundingBox;
        this.maskBoundingBox = maskBoundingBox;
    }

    public World getWorld() {
        return world;
    }

    public Block getBlock() {
        return block;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public AxisAlignedBB getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(AxisAlignedBB boundingBox) {
        this.boundingBox = boundingBox;
    }

    public AxisAlignedBB getMaskBoundingBox() {
        return maskBoundingBox;
    }
}