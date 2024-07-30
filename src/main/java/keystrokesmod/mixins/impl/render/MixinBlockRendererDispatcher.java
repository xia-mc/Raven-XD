package keystrokesmod.mixins.impl.render;


import keystrokesmod.module.impl.movement.fly.SpoofFly;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockRendererDispatcher.class)
public abstract class MixinBlockRendererDispatcher {

    @Inject(method = "renderBlockDamage", at = @At("HEAD"), cancellable = true)
    public void onRenderBlockDamage(IBlockState state, BlockPos pos, TextureAtlasSprite textureAtlasSprite, IBlockAccess blockAccess, CallbackInfo ci) {
        try {
            if (SpoofFly.isHidden(pos)) {
                ci.cancel();
            }
        } catch (Throwable ignored) {
        }
    }

    @Inject(method = "renderBlock", at = @At("HEAD"), cancellable = true)
    public void onRenderBlock(IBlockState state, BlockPos pos, IBlockAccess blockAccess, WorldRenderer world, CallbackInfoReturnable<Boolean> cir) {
        try {
            if (SpoofFly.isHidden(pos)) {
                cir.setReturnValue(false);
            }
        } catch (Throwable ignored) {
        }
    }
}
