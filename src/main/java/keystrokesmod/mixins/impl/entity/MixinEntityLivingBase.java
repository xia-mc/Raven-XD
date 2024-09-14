package keystrokesmod.mixins.impl.entity;

import com.google.common.collect.Maps;
import keystrokesmod.event.JumpEvent;
import keystrokesmod.event.MoveEvent;
import keystrokesmod.event.PreMoveEvent;
import keystrokesmod.event.SwingAnimationEvent;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.exploit.viaversionfix.ViaVersionFixHelper;
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

import static keystrokesmod.Raven.mc;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity {
    @Shadow protected abstract float getJumpUpwardsMotion();

    @Shadow public float moveStrafing;

    public MixinEntityLivingBase(World worldIn) {
        super(worldIn);
    }

    @Unique
    private final Map<Integer, PotionEffect> raven_bS$activePotionsMap = Maps.newHashMap();

    @Unique
    public PotionEffect raven_XD$getActivePotionEffect(@NotNull Potion potionIn) {
        return this.raven_bS$activePotionsMap.get(potionIn.id);
    }

    @Unique
    public boolean raven_XD$isPotionActive(@NotNull Potion potionIn) {
        return this.raven_bS$activePotionsMap.containsKey(potionIn.id);
    }

    @Inject(method = "moveEntityWithHeading", at = @At("HEAD"), cancellable = true)
    public void onPreMoveEntity(float moveForward, float moveStrafing, CallbackInfo ci) {
        if ((Object) this instanceof EntityPlayerSP) {
            PreMoveEvent event = new PreMoveEvent();
            MinecraftForge.EVENT_BUS.post(event);

            if (event.isCanceled())
                ci.cancel();
        }
    }

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
            if (mc.thePlayer.swingProgress > 0F) {
                p_1101461 = RotationUtils.renderYaw;
            }
            rotationYaw = RotationUtils.renderYaw;
            mc.thePlayer.rotationYawHead = RotationUtils.renderYaw;
        }
        float f = MathHelper.wrapAngleTo180_float(p_1101461 - ((EntityLivingBase)(Object)this).renderYawOffset);
        ((EntityLivingBase)(Object)this).renderYawOffset += f * 0.3F;
        float f1 = MathHelper.wrapAngleTo180_float(rotationYaw - ((EntityLivingBase)(Object)this).renderYawOffset);
        boolean flag = f1 < 90.0F || f1 >= 90.0F;

        if (f1 < -75.0F) {
            f1 = -75.0F;
        }

        if (f1 >= 75.0F) {
            f1 = 75.0F;
        }

        ((EntityLivingBase)(Object)this).renderYawOffset = rotationYaw - f1;

        if (f1 * f1 > 2500.0F) {
            ((EntityLivingBase)(Object)this).renderYawOffset += f1 * 0.2F;
        }

        if (flag) {
            p_1101462 *= -1.0F;
        }

        cir.setReturnValue(p_1101462);
    }

    /**
     * @author strangerrs
     * @reason mixin jump
     */
    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    protected void jump(CallbackInfo ci) {
        JumpEvent jumpEvent = new JumpEvent((float) MoveUtil.jumpMotion(), RotationHandler.getMovementYaw(this));
        MinecraftForge.EVENT_BUS.post(jumpEvent);
        if (jumpEvent.isCanceled()) {
            return;
        }

        this.motionY = jumpEvent.getMotionY();

        if (this.raven_XD$isPotionActive(Potion.jump)) {
            this.motionY += (float) (this.raven_XD$getActivePotionEffect(Potion.jump).getAmplifier() + 1) * 0.1F;
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

    /**
     * A part of ViaVersionFix.
     * In 1.8, the minimum motion before reset is 0.005, but in 1.12, this value is 0.003.
     * To reduce overwrites, I make this redirect mixin.
     * It works like this:
     *     abs(motion) < 0.003
     *   = abs(motion) + 0.002 < 0.005
     * @param motion the single axis motion of current entity
     * @return abs result
     */
    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Ljava/lang/Math;abs(D)D"))
    private double onAbsMotion(double motion) {
        final double absResult = Math.abs(motion);
        return ViaVersionFixHelper.is122() ? absResult + 0.002 : absResult;
    }
}
