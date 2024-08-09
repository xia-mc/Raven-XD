package keystrokesmod.mixins.impl.client;


import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.utility.RotationUtils;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Redirect(method = "getMouseOver", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;getLook(F)Lnet/minecraft/util/Vec3;"))
    public Vec3 onGetLook(Entity instance, float partialTicks) {
        if (partialTicks == 1.0F) {
            return RotationUtils.getVectorForRotation(RotationHandler.getRotationPitch(), RotationHandler.getRotationYaw());
        } else {
            float f = RotationHandler.getPrevRotationPitch() + (RotationHandler.getRotationPitch() - RotationHandler.getPrevRotationPitch()) * partialTicks;
            float f1 = RotationHandler.getPrevRotationYaw() + (RotationHandler.getRotationYaw() - RotationHandler.getPrevRotationYaw()) * partialTicks;
            return RotationUtils.getVectorForRotation(f, f1);
        }
    }
}
