package keystrokesmod.mixins.impl.world;

import keystrokesmod.module.impl.exploit.viaversionfix.ViaVersionFixHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLadder;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.block.BlockLadder.FACING;

@Mixin(BlockLadder.class)
public abstract class MixinBlockLadder extends Block {
    public MixinBlockLadder(Material p_i46399_1_, MapColor p_i46399_2_) {
        super(p_i46399_1_, p_i46399_2_);
    }

    /**
     * A part of ViaVersionFix
     */
    @Inject(method = "setBlockBoundsBasedOnState", at = @At(value = "HEAD"), cancellable = true)
    public void onSetBlockBoundsBasedOnState(@NotNull IBlockAccess worldIn, BlockPos pos, CallbackInfo ci) {
        IBlockState iblockstate = worldIn.getBlockState(pos);

        float needMinus = 0.125f;
        // Fix ladder simulation
        if (ViaVersionFixHelper.is122()) {
            needMinus = 0.1875F;
        }

        if (iblockstate.getBlock() == this)
            switch (iblockstate.getValue(FACING)) {
                case NORTH:
                    this.setBlockBounds(0f, 0f, 1f - needMinus, 1f, 1f, 1f);
                    break;

                case SOUTH:
                    this.setBlockBounds(0f, 0f, 0f, 1f, 1f, needMinus);
                    break;

                case WEST:
                    this.setBlockBounds(1f - needMinus, 0f, 0f, 1f, 1f, 1f);
                    break;

                case EAST:
                default:
                    this.setBlockBounds(0f, 0f, 0f, needMinus, 1f, 1f);
            }
        ci.cancel();
    }
}
