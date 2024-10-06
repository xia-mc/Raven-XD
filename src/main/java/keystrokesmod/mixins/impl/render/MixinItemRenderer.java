package keystrokesmod.mixins.impl.render;


import keystrokesmod.event.RenderItemEvent;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.utility.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ItemRenderer.class, priority = 1983)
public abstract class MixinItemRenderer {

    @Shadow private float prevEquippedProgress;

    @Shadow private float equippedProgress;

    @Shadow @Final private Minecraft mc;

    @Shadow protected abstract void func_178101_a(float p_178101_1_, float p_178101_2_);

    @Shadow protected abstract void func_178109_a(AbstractClientPlayer p_178109_1_);

    @Shadow protected abstract void func_178110_a(EntityPlayerSP p_178110_1_, float p_178110_2_);

    @Shadow protected abstract void renderItemMap(AbstractClientPlayer p_renderItemMap_1_, float p_renderItemMap_2_, float p_renderItemMap_3_, float p_renderItemMap_4_);

    @Shadow protected abstract void transformFirstPersonItem(float p_transformFirstPersonItem_1_, float p_transformFirstPersonItem_2_);

    @Shadow protected abstract void func_178104_a(AbstractClientPlayer p_178104_1_, float p_178104_2_);

    @Shadow protected abstract void func_178103_d();

    @Shadow protected abstract void func_178098_a(float p_178098_1_, AbstractClientPlayer p_178098_2_);

    @SuppressWarnings("deprecation")
    @Shadow public abstract void renderItem(EntityLivingBase p_renderItem_1_, ItemStack p_renderItem_2_, ItemCameraTransforms.TransformType p_renderItem_3_);

    @Shadow protected abstract void func_178095_a(AbstractClientPlayer p_178095_1_, float p_178095_2_, float p_178095_3_);

    @Shadow private ItemStack itemToRender;

    @Unique
    private boolean raven_XD$lastIsSpoofItem = false;

    /**
     * @author xia__mc
     * @reason for Animations module.
     */
    @SuppressWarnings("deprecation")
    @Inject(method = "renderItemInFirstPerson", at = @At("HEAD"), cancellable = true)
    public void renderItemInFirstPerson(final float partialTicks, @NotNull CallbackInfo ci) {
        if (!Utils.nullCheck() || !ModuleManager.animations.isEnabled()) {
            return;
        }
        ci.cancel();

        try {
            float animationProgression = 1.0F - (this.prevEquippedProgress + (this.equippedProgress - this.prevEquippedProgress) * partialTicks);
            final EntityPlayerSP thePlayer = this.mc.thePlayer;
            float swingProgress = thePlayer.getSwingProgress(partialTicks);
            final float f2 = thePlayer.prevRotationPitch + (thePlayer.rotationPitch - thePlayer.prevRotationPitch) * partialTicks;
            final float f3 = thePlayer.prevRotationYaw + (thePlayer.rotationYaw - thePlayer.prevRotationYaw) * partialTicks;
            this.func_178101_a(f2, f3);
            this.func_178109_a(thePlayer);
            this.func_178110_a(thePlayer, partialTicks);
            GlStateManager.enableRescaleNormal();
            GlStateManager.pushMatrix();

            ItemStack itemToRender = SlotHandler.getRenderHeldItem();
            if (itemToRender != null) {
                if (itemToRender != SlotHandler.getHeldItem()) {
                    animationProgression = 0.0F;
                    raven_XD$lastIsSpoofItem = true;
                } else if (raven_XD$lastIsSpoofItem) {
                    animationProgression = 0.0F;
                    raven_XD$lastIsSpoofItem = false;
                }

                EnumAction enumaction = itemToRender.getItemUseAction();
                final int itemInUseCount = thePlayer.getItemInUseCount();
                boolean useItem = itemInUseCount > 0;

                final RenderItemEvent event = new RenderItemEvent(enumaction, useItem, animationProgression, partialTicks, swingProgress, itemToRender);
                MinecraftForge.EVENT_BUS.post(event);
                enumaction = event.getEnumAction();
                useItem = event.isUseItem();
                animationProgression = event.getAnimationProgression();
                swingProgress = event.getSwingProgress();

                if (itemToRender.getItem() instanceof ItemMap) {
                    this.renderItemMap(thePlayer, f2, animationProgression, swingProgress);
                } else if (useItem) {
                    if (!event.isCanceled()) {
                        switch (enumaction) {
                            case NONE:
                                this.transformFirstPersonItem(animationProgression, 0.0F);
                                break;

                            case EAT:
                            case DRINK:
                                this.func_178104_a(thePlayer, partialTicks);
                                this.transformFirstPersonItem(animationProgression, 0.0F);
                                break;

                            case BLOCK:
                                this.transformFirstPersonItem(animationProgression, 0.0F);
                                this.func_178103_d();
                                break;

                            case BOW:
                                this.transformFirstPersonItem(animationProgression, 0.0F);
                                this.func_178098_a(partialTicks, thePlayer);
                        }
                    }
                } else if (!event.isCanceled()) {
                    this.func_178105_d(swingProgress);
                    this.transformFirstPersonItem(animationProgression, swingProgress);
                }

                this.renderItem(thePlayer, itemToRender, ItemCameraTransforms.TransformType.FIRST_PERSON);
            } else if (!thePlayer.isInvisible()) {
                this.func_178095_a(thePlayer, animationProgression, swingProgress);
            }

            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
            RenderHelper.disableStandardItemLighting();
        } catch (Exception ignored) {
        }
    }

    /**
     * @author xia__mc
     * @reason fix crash issue with LabyMod.
     */
    @Overwrite
    private void func_178105_d(float p_178105_1_) {
        float f = -0.4F * MathHelper.sin(MathHelper.sqrt_float(p_178105_1_) * 3.1415927F);
        float f1 = 0.2F * MathHelper.sin(MathHelper.sqrt_float(p_178105_1_) * 3.1415927F * 2.0F);
        float f2 = -0.2F * MathHelper.sin(p_178105_1_ * 3.1415927F);
        GlStateManager.translate(f, f1, f2);
    }
}
