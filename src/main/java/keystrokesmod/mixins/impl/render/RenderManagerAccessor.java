package keystrokesmod.mixins.impl.render;


import net.minecraft.client.renderer.entity.RenderManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderManager.class)
public interface RenderManagerAccessor {
    @Accessor("renderPosX")
    double getRenderPosX();

    @Accessor("renderPosY")
    double getRenderPosY();

    @Accessor("renderPosY")
    void setRenderPosY(double renderPosY);

    @Accessor("renderPosZ")
    double getRenderPosZ();
}
