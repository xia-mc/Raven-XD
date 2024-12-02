package keystrokesmod.mixins.impl.client;


import com.google.common.base.Predicates;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.render.CustomFOV;
import keystrokesmod.module.impl.other.RotationHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.*;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Shadow private Minecraft mc;

    @Shadow private Entity pointedEntity;

    /**
     * @author xia__mc
     * @reason fix silent rotation system
     * <p>
     * I'm sorry I must overwrite this...
     */
    @Inject(method = "getMouseOver", at = @At("HEAD"), cancellable = true)
    public void getMouseOver(float p_getMouseOver_1_, CallbackInfo ci) {
        Entity entity = this.mc.getRenderViewEntity();
        if (entity != null && this.mc.theWorld != null) {
            this.mc.mcProfiler.startSection("pick");
            this.mc.pointedEntity = null;
            double d0 = this.mc.playerController.getBlockReachDistance();
            this.mc.objectMouseOver = entity.rayTrace(d0, p_getMouseOver_1_);
            double d1 = d0;
            Vec3 vec3 = entity.getPositionEyes(p_getMouseOver_1_);
            boolean flag = false;
            if (this.mc.playerController.extendedReach()) {
                d0 = 6.0;
                d1 = 6.0;
            } else if (d0 > 3.0) {
                flag = true;
            }

            if (this.mc.objectMouseOver != null) {
                d1 = this.mc.objectMouseOver.hitVec.distanceTo(vec3);
            }

            Vec3 vec31 = RotationHandler.getLook(p_getMouseOver_1_);
            Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
            this.pointedEntity = null;
            Vec3 vec33 = null;
            float f = 1.0F;
            List<Entity> list = this.mc.theWorld.getEntitiesInAABBexcluding(
                    entity, entity.getEntityBoundingBox()
                            .addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0)
                            .expand(f, f, f),
                    Predicates.and(EntitySelectors.NOT_SPECTATING, Entity::canBeCollidedWith)
            );
            double d2 = d1;

            for (Entity entity1 : list) {
                float f1 = entity1.getCollisionBorderSize();
                AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand(f1, f1, f1);
                MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);
                if (axisalignedbb.isVecInside(vec3)) {
                    if (d2 >= 0.0) {
                        this.pointedEntity = entity1;
                        vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                        d2 = 0.0;
                    }
                } else if (movingobjectposition != null) {
                    double d3 = vec3.distanceTo(movingobjectposition.hitVec);
                    if (d3 < d2 || d2 == 0.0) {
                        if (entity1 == entity.ridingEntity && !entity.canRiderInteract()) {
                            if (d2 == 0.0) {
                                this.pointedEntity = entity1;
                                vec33 = movingobjectposition.hitVec;
                            }
                        } else {
                            this.pointedEntity = entity1;
                            vec33 = movingobjectposition.hitVec;
                            d2 = d3;
                        }
                    }
                }
            }

            if (this.pointedEntity != null && flag && vec3.distanceTo(vec33) > 3.0) {
                this.pointedEntity = null;
                assert vec33 != null;
                this.mc.objectMouseOver = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, vec33, null, new BlockPos(vec33));
            }

            if (this.pointedEntity != null && (d2 < d1 || this.mc.objectMouseOver == null)) {
                this.mc.objectMouseOver = new MovingObjectPosition(this.pointedEntity, vec33);
                if (this.pointedEntity instanceof EntityLivingBase || this.pointedEntity instanceof EntityItemFrame) {
                    this.mc.pointedEntity = this.pointedEntity;
                }
            }

            this.mc.mcProfiler.endSection();
        }

        ci.cancel();
    }


    /**
     * @author kefpull
     * @reason attempt 1 have an option for CustomFOV to do the same thing as optifine's "Dynamic FOV:off". Hopefully this does not break the whole entire client :).
     * I know we're supposed to use an event or something like that, so I will do that if I can.
     * <p>
     * Source for method: line 412 in EntityRenderer.class
     */
    @Inject(method = "getFOVModifier", at = @At("RETURN"), cancellable = true)
    public void onGetFOVModifier(@NotNull CallbackInfoReturnable<Float> cir) {

        if (ModuleManager.customFOV != null && ModuleManager.customFOV.forceStatic != null) {

            if (ModuleManager.customFOV.isEnabled() && ModuleManager.customFOV.forceStatic.isToggled()) {
                cir.setReturnValue(CustomFOV.getDesiredFOV());
            }

        }
    }


}
