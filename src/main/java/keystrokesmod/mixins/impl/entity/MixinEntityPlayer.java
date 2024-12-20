package keystrokesmod.mixins.impl.entity;

import keystrokesmod.event.EyeHeightEvent;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.movement.KeepSprint;
import keystrokesmod.module.impl.render.Particles;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static keystrokesmod.Raven.mc;

@SuppressWarnings("DataFlowIssue")
@Mixin(value = EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase {
    public MixinEntityPlayer(World p_i1594_1_) {
        super(p_i1594_1_);
    }

    /**
     * @author strangerrrs
     * @reason mixin attack target entity with current item
     */
    @SuppressWarnings({"UnresolvedMixinReference", "ExtractMethodRecommender"})
    @Inject(method = "attackTargetEntityWithCurrentItem", at = @At("HEAD"), cancellable = true)
    public void attackTargetEntityWithCurrentItem(Entity p_attackTargetEntityWithCurrentItem_1_, CallbackInfo ci) {
        if (ForgeHooks.onPlayerAttackTarget(((EntityPlayer) (Object) this), p_attackTargetEntityWithCurrentItem_1_)) {
            if (p_attackTargetEntityWithCurrentItem_1_.canAttackWithItem() && !p_attackTargetEntityWithCurrentItem_1_.hitByEntity(this)) {
                float f = (float) this.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
                int i = 0;
                float f1;
                if (p_attackTargetEntityWithCurrentItem_1_ instanceof EntityLivingBase) {
                    f1 = EnchantmentHelper.func_152377_a(this.getHeldItem(), ((EntityLivingBase) p_attackTargetEntityWithCurrentItem_1_).getCreatureAttribute());
                } else {
                    f1 = EnchantmentHelper.func_152377_a(this.getHeldItem(), EnumCreatureAttribute.UNDEFINED);
                }

                i += EnchantmentHelper.getKnockbackModifier(this);
                if (this.isSprinting()) {
                    ++i;
                }

                if (f > 0.0F || f1 > 0.0F) {
                    boolean flag = this.fallDistance > 0.0F && !this.onGround && !this.isOnLadder() && !this.isInWater() && !this.isPotionActive(Potion.blindness) && this.ridingEntity == null && p_attackTargetEntityWithCurrentItem_1_ instanceof EntityLivingBase;
                    if (flag && f > 0.0F) {
                        f *= 1.5F;
                    }

                    f += f1;
                    boolean flag1 = false;
                    int j = EnchantmentHelper.getFireAspectModifier(this);
                    if (p_attackTargetEntityWithCurrentItem_1_ instanceof EntityLivingBase && j > 0 && !p_attackTargetEntityWithCurrentItem_1_.isBurning()) {
                        flag1 = true;
                        p_attackTargetEntityWithCurrentItem_1_.setFire(1);
                    }

                    double d0 = p_attackTargetEntityWithCurrentItem_1_.motionX;
                    double d1 = p_attackTargetEntityWithCurrentItem_1_.motionY;
                    double d2 = p_attackTargetEntityWithCurrentItem_1_.motionZ;
                    boolean flag2 = p_attackTargetEntityWithCurrentItem_1_.attackEntityFrom(DamageSource.causePlayerDamage(((EntityPlayer) (Object) this)), f);
                    if (flag2) {
                        if (i > 0) {
                            p_attackTargetEntityWithCurrentItem_1_.addVelocity(-MathHelper.sin(this.rotationYaw * 3.1415927F / 180.0F) * (float) i * 0.5F, 0.1, MathHelper.cos(this.rotationYaw * 3.1415927F / 180.0F) * (float) i * 0.5F);
                            if (ModuleManager.keepSprint != null && ModuleManager.keepSprint.isEnabled()) {
                                KeepSprint.keepSprint(p_attackTargetEntityWithCurrentItem_1_);
                            }
                            else {
                                this.motionX *= 0.6D;
                                this.motionZ *= 0.6D;
                                this.setSprinting(false);
                            }
                        }

                        if (p_attackTargetEntityWithCurrentItem_1_ instanceof EntityPlayerMP && p_attackTargetEntityWithCurrentItem_1_.velocityChanged) {
                            ((EntityPlayerMP) p_attackTargetEntityWithCurrentItem_1_).playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(p_attackTargetEntityWithCurrentItem_1_));
                            p_attackTargetEntityWithCurrentItem_1_.velocityChanged = false;
                            p_attackTargetEntityWithCurrentItem_1_.motionX = d0;
                            p_attackTargetEntityWithCurrentItem_1_.motionY = d1;
                            p_attackTargetEntityWithCurrentItem_1_.motionZ = d2;
                        }

                        for (int criticals = 0; criticals < Particles.getCriticalsMultiplier(flag); criticals++) {
                            mc.thePlayer.onCriticalHit(p_attackTargetEntityWithCurrentItem_1_);
                        }

                        for (int sharpness = 0; sharpness < Particles.getSharpnessMultiplier(f1 > 0.0F); sharpness++) {
                            mc.thePlayer.onEnchantmentCritical(p_attackTargetEntityWithCurrentItem_1_);
                        }

                        if (f >= 18.0F) {
                            mc.thePlayer.triggerAchievement(AchievementList.overkill);
                        }

                        this.setLastAttacker(p_attackTargetEntityWithCurrentItem_1_);
                        if (p_attackTargetEntityWithCurrentItem_1_ instanceof EntityLivingBase) {
                            EnchantmentHelper.applyThornEnchantments((EntityLivingBase) p_attackTargetEntityWithCurrentItem_1_, this);
                        }

                        EnchantmentHelper.applyArthropodEnchantments(this, p_attackTargetEntityWithCurrentItem_1_);
                        ItemStack itemstack = mc.thePlayer.getCurrentEquippedItem();
                        Entity entity1 = p_attackTargetEntityWithCurrentItem_1_;
                        if (p_attackTargetEntityWithCurrentItem_1_ instanceof EntityDragonPart) {
                            IEntityMultiPart ientitymultipart = ((EntityDragonPart) p_attackTargetEntityWithCurrentItem_1_).entityDragonObj;
                            if (ientitymultipart instanceof EntityLivingBase) {
                                entity1 = (EntityLivingBase) ientitymultipart;
                            }
                        }
                        Entity entity = entity1;

                        if (itemstack != null && entity instanceof EntityLivingBase) {
                            itemstack.hitEntity((EntityLivingBase) entity, ((EntityPlayer) (Object) this));
                            if (itemstack.stackSize <= 0) {
                                mc.thePlayer.destroyCurrentEquippedItem();
                            }
                        }

                        if (p_attackTargetEntityWithCurrentItem_1_ instanceof EntityLivingBase) {
                            mc.thePlayer.addStat(StatList.damageDealtStat, Math.round(f * 10.0F));
                            if (j > 0) {
                                p_attackTargetEntityWithCurrentItem_1_.setFire(j * 4);
                            }
                        }

                        mc.thePlayer.addExhaustion(0.3F);
                    } else if (flag1) {
                        p_attackTargetEntityWithCurrentItem_1_.extinguish();
                    }
                }
            }

        }
        ci.cancel();
    }

    @Inject(method = "getEyeHeight", at = @At("RETURN"), cancellable = true)
    public void onGetEyeHeight(@NotNull CallbackInfoReturnable<Float> cir) {
        EyeHeightEvent event = new EyeHeightEvent(cir.getReturnValue());
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isSet()) {
            mc.thePlayer.cameraYaw = 0;
            mc.thePlayer.cameraPitch = 0;
            cir.setReturnValue((float) event.getEyeHeight());
        }
    }
}
