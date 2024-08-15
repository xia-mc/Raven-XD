package keystrokesmod.mixins.impl.entity;

import com.google.common.collect.Maps;
import keystrokesmod.event.JumpEvent;
import keystrokesmod.event.MoveEvent;
import keystrokesmod.event.SwingAnimationEvent;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.movement.Sprint;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.RotationUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity {
    public MixinEntityLivingBase(World worldIn) {
        super(worldIn);
    }

    @Unique
    private final Map<Integer, PotionEffect> raven_bS$activePotionsMap = Maps.newHashMap();

    @Shadow
    public PotionEffect getActivePotionEffect(@NotNull Potion potionIn) {
        return this.raven_bS$activePotionsMap.get(potionIn.id);
    }

    @Shadow
    public boolean isPotionActive(@NotNull Potion potionIn) {
        return this.raven_bS$activePotionsMap.containsKey(potionIn.id);
    }

    @Shadow
    public float rotationYawHead;

    @Shadow
    public float renderYawOffset;

    @Shadow
    public float swingProgress;

    @Redirect(method = "moveEntityWithHeading", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;moveEntity(DDD)V"))
    public void onMoveEntity(EntityLivingBase instance, double x, double y, double z) {
        if (instance instanceof EntityPlayerSP) {
            MoveEvent event = new MoveEvent(x, y, z);
            MinecraftForge.EVENT_BUS.post(event);

            if (event.isCanceled())
                return;

            x = event.getX();
            y = event.getY();
            z = event.getZ();
        }

        instance.moveEntity(x, y, z);
    }

    /**
     * @author strangerrs
     * @reason mixin func110146f
     */
    @Inject(method = "func_110146_f", at = @At("HEAD"), cancellable = true)
    protected void func_110146_f(float p_1101461, float p_1101462, CallbackInfoReturnable<Float> cir) {
        float rotationYaw = this.rotationYaw;
        if (RotationHandler.fullBody != null && RotationHandler.rotateBody != null && !RotationHandler.fullBody.isToggled() && RotationHandler.rotateBody.isToggled() && (EntityLivingBase) (Object) this instanceof EntityPlayerSP) {
            if (this.swingProgress > 0F) {
                p_1101461 = RotationUtils.renderYaw;
            }
            rotationYaw = RotationUtils.renderYaw;
            rotationYawHead = RotationUtils.renderYaw;
        }
        float f = MathHelper.wrapAngleTo180_float(p_1101461 - this.renderYawOffset);
        this.renderYawOffset += f * 0.3F;
        float f1 = MathHelper.wrapAngleTo180_float(rotationYaw - this.renderYawOffset);
        boolean flag = f1 < 90.0F || f1 >= 90.0F;

        if (f1 < -75.0F) {
            f1 = -75.0F;
        }

        if (f1 >= 75.0F) {
            f1 = 75.0F;
        }

        this.renderYawOffset = rotationYaw - f1;

        if (f1 * f1 > 2500.0F) {
            this.renderYawOffset += f1 * 0.2F;
        }

        if (flag) {
            p_1101462 *= -1.0F;
        }

        cir.setReturnValue(p_1101462);
    }

    @Shadow
    protected float getJumpUpwardsMotion() {
        return 0.42F;
    }

    /**
     * @author strangerrs
     * @reason mixin jump
     */
    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    protected void jump(CallbackInfo ci) {
        JumpEvent jumpEvent = new JumpEvent(this.getJumpUpwardsMotion(), RotationHandler.getMovementYaw(this));
        MinecraftForge.EVENT_BUS.post(jumpEvent);
        if (jumpEvent.isCanceled()) {
            return;
        }

        this.motionY = jumpEvent.getMotionY();

        if (this.isPotionActive(Potion.jump)) {
            this.motionY += (float) (this.getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;
        }

        if (this.isSprinting()) {
            float f = jumpEvent.getYaw() * 0.017453292F;

            if (Sprint.omni()) {
                f = (float) (MoveUtil.direction() * (180 / Math.PI));
                f *= 0.017453292F;
            }

            this.motionX -= MathHelper.sin(f) * 0.2F;
            this.motionZ += MathHelper.cos(f) * 0.2F;
        }

        this.isAirBorne = true;
        ForgeHooks.onLivingJump(((EntityLivingBase) (Object) this));
        ci.cancel();
    }

    @Inject(method = "isPotionActive(Lnet/minecraft/potion/Potion;)Z", at = @At("HEAD"), cancellable = true)
    private void isPotionActive(Potion p_isPotionActive_1_, final CallbackInfoReturnable<Boolean> callbackInfoReturnable) {
        if (ModuleManager.potions != null && ModuleManager.potions.isEnabled() && ((p_isPotionActive_1_ == Potion.confusion && ModuleManager.potions.removeNausea.isToggled()) || (p_isPotionActive_1_ == Potion.blindness && ModuleManager.potions.removeBlindness.isToggled()))) {
            callbackInfoReturnable.setReturnValue(false);
        }
    }

    /**
     * @author xia__mc
     * @reason for Animations module
     */
    @Inject(method = "getArmSwingAnimationEnd", at = @At("RETURN"), cancellable = true)
    private void onGetArmSwingAnimationEnd(@NotNull CallbackInfoReturnable<Integer> cir) {
        SwingAnimationEvent swingAnimationEvent = new SwingAnimationEvent(cir.getReturnValue());
        MinecraftForge.EVENT_BUS.post(swingAnimationEvent);

        cir.setReturnValue((int) (swingAnimationEvent.getAnimationEnd() * Utils.getTimer().timerSpeed));
    }
}
