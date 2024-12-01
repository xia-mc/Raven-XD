package keystrokesmod.module.impl.render;

import keystrokesmod.event.*;
import keystrokesmod.mixins.impl.render.ItemRendererAccessor;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.combat.KillAura;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

public class Animations extends Module {
    public static final ButtonSetting swingWhileDigging = new ButtonSetting("Swing while digging", true);
    public static final ButtonSetting clientSide = new ButtonSetting("Client side (visual 1.7)", true, swingWhileDigging::isToggled);
    private final ModeSetting blockAnimation = new ModeSetting("Block animation", new String[]{"None", "1.7", "Smooth", "Exhibition", "Stab", "Spin", "Sigma", "Wood", "Swong", "Chill", "Komorebi", "Rhys", "Allah"}, 1);
    private final ModeSetting swingAnimation = new ModeSetting("Swing animation", new String[]{"None", "1.9+", "Smooth", "Punch", "Shove"}, 0);
    private final ModeSetting otherAnimation = new ModeSetting("Other animation", new String[]{"None", "1.7"}, 1);
    private final ButtonSetting fakeSlotReset = new ButtonSetting("Fake slot reset", false);

    private final SliderSetting staticStartSwingProgress = new SliderSetting("Starting Swing Progress", 0, -1, 2.5, 0.05);

    //translation
    private final SliderSetting translatex = new SliderSetting("X", 0, -4, 4, 0.05);
    private final SliderSetting translatey = new SliderSetting("Y", 0, -2, 2, 0.05);
    private final SliderSetting translatez = new SliderSetting("Z", 0, -10, 10, 0.05);

    private final ButtonSetting precustomtranslation = new ButtonSetting("Custom Translation (pre)", false);
    private final SliderSetting pretranslatex = new SliderSetting("X", 0, -4, 4, 0.05, precustomtranslation::isToggled);
    private final SliderSetting pretranslatey = new SliderSetting("Y", 0, -2, 2, 0.05, precustomtranslation::isToggled);
    private final SliderSetting pretranslatez = new SliderSetting("Z", 0, -6, 3, 0.05, precustomtranslation::isToggled);

    private final ButtonSetting customscaling = new ButtonSetting("Custom Scaling", false);
    private final SliderSetting scalex = new SliderSetting("ScaleX", 1, -1, 5, 0.05, customscaling::isToggled);
    private final SliderSetting scaley = new SliderSetting("ScaleY", 1, -1, 5, 0.05, customscaling::isToggled);
    private final SliderSetting scalez = new SliderSetting("ScaleZ", 1, -1, 5, 0.05, customscaling::isToggled);

    private final ButtonSetting customrotation = new ButtonSetting("Custom Rotation", false);
    private final SliderSetting rotatex = new SliderSetting("rotation x", 0, -180, 180, 1, customrotation::isToggled);
    private final SliderSetting rotatey = new SliderSetting("rotation y", 0, -180, 180, 1, customrotation::isToggled);
    private final SliderSetting rotatez = new SliderSetting("rotation z", 0, -180, 180, 1, customrotation::isToggled);
    private final SliderSetting swingSpeed = new SliderSetting("Swing speed", 0, -200, 50, 5);

    private int swing;

    private static final double staticscalemultiplier_x = 1;
    private static final double staticscalemultiplier_y = 1;
    private static final double staticscalemultiplier_z = 1;


    public Animations() {
        super("Animations", category.render);
        this.registerSetting(blockAnimation, swingAnimation, otherAnimation, swingWhileDigging, clientSide, fakeSlotReset);
        //this.registerSetting(customanimation);
        this.registerSetting(staticStartSwingProgress);
        this.registerSetting(new DescriptionSetting("Custom Translation"));
        this.registerSetting(translatex, translatey, translatez);
        this.registerSetting(precustomtranslation, pretranslatex, pretranslatey, pretranslatez);
        this.registerSetting(customscaling, scalex, scaley, scalez);
        this.registerSetting(customrotation, rotatex, rotatey, rotatez);
        this.registerSetting(swingSpeed);    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent event) {
        if (Utils.nullCheck()
                && swingWhileDigging.isToggled()
                && clientSide.isToggled()
                && event.getPacket() instanceof C0APacketAnimation
                && mc.thePlayer.isUsingItem()
        )
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent event) {
        if (Utils.nullCheck()
                && fakeSlotReset.isToggled()
                && event.getPacket() instanceof S0BPacketAnimation
                && SlotHandler.getHeldItem() != null
                && SlotHandler.getCurrentSlot() == mc.thePlayer.inventory.currentItem
                && KillAura.target != null
        ) {
            final S0BPacketAnimation packet = (S0BPacketAnimation) event.getPacket();
            if (packet.getAnimationType() == 1 && packet.getEntityID() == KillAura.target.getEntityId()) {
                mc.getItemRenderer().resetEquippedProgress();
            }
        }
    }

    @SuppressWarnings("IntegerDivisionInFloatingPointContext")
    @SubscribeEvent
    public void onRenderItem(@NotNull RenderItemEvent event) {
        try {
            if (event.getItemToRender().getItem() instanceof ItemMap) {
                return;
            }

            final EnumAction itemAction = event.getEnumAction();
            final ItemRendererAccessor itemRenderer = (ItemRendererAccessor) mc.getItemRenderer();
            final float animationProgression = event.getAnimationProgression();
            float swingProgress = event.getSwingProgress();
            final float convertedProgress = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);

            float staticStartSwingProgressFloat = ((float) staticStartSwingProgress.getInput());

            if(precustomtranslation.isToggled()) {
                this.pretranslate(pretranslatex.getInput(), pretranslatey.getInput(), pretranslatez.getInput());

            }

            if(customrotation.isToggled()) {
                this.rotate((float) rotatex.getInput(), (float) rotatey.getInput(), (float) rotatez.getInput());

            }

            if(customscaling.isToggled()) {
                this.scale(1, 1, 1);
            }


            this.translate(translatex.getInput(), translatey.getInput(), translatez.getInput());


            if (event.isUseItem()) {
                switch (itemAction) {
                    case NONE:
                        switch ((int) otherAnimation.getInput()) {
                            case 0:
                                //none
                                itemRenderer.transformFirstPersonItem(animationProgression, staticStartSwingProgressFloat);
                                break;
                            case 1:
                                //1.7
                                itemRenderer.transformFirstPersonItem(animationProgression, swingProgress);
                                break;
                        }
                        break;
                    case BLOCK:
                        switch ((int) blockAnimation.getInput()) {
                            case 0:
                                //none
                                itemRenderer.transformFirstPersonItem(animationProgression, staticStartSwingProgressFloat);
                                itemRenderer.blockTransformation();
                                break;

                            case 1:
                                //1.7
                                itemRenderer.transformFirstPersonItem(animationProgression, swingProgress);
                                itemRenderer.blockTransformation();
                                break;

                            case 2:
                                //smooth
                                itemRenderer.transformFirstPersonItem(animationProgression, staticStartSwingProgressFloat);
                                final float y = -convertedProgress * 2.0F;
                                this.translate(0.0F, y / 10.0F + 0.1F, 0.0F);
                                GlStateManager.rotate(y * 10.0F, 0.0F, 1.0F, 0.0F);
                                GlStateManager.rotate(250, 0.2F, 1.0F, -0.6F);
                                GlStateManager.rotate(-10.0F, 1.0F, 0.5F, 1.0F);
                                GlStateManager.rotate(-y * 20.0F, 1.0F, 0.5F, 1.0F);
                                break;

                            case 3:
                                //exhibition
                                itemRenderer.transformFirstPersonItem(animationProgression / 2.0F, staticStartSwingProgressFloat);
                                this.translate(0.0F, 0.3F, -0.0F);
                                GlStateManager.rotate(-convertedProgress * 31.0F, 1.0F, 0.0F, 2.0F);
                                GlStateManager.rotate(-convertedProgress * 33.0F, 1.5F, (convertedProgress / 1.1F), 0.0F);
                                itemRenderer.blockTransformation();
                                break;

                            case 4:
                                //stab
                                final float spin = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);

                                this.translate(0.6f, 0.3f, -0.6f + -spin * 0.7);
                                GlStateManager.rotate(6090, 0.0f, 0.0f, 0.1f);
                                GlStateManager.rotate(6085, 0.0f, 0.1f, 0.0f);
                                GlStateManager.rotate(6110, 0.1f, 0.0f, 0.0f);
                                itemRenderer.transformFirstPersonItem(0.0F, 0.0f);
                                itemRenderer.blockTransformation();
                                break;

                            case 5:
                                //spin
                                itemRenderer.transformFirstPersonItem(animationProgression, staticStartSwingProgressFloat);
                                this.translate(0, 0.2F, -1);
                                GlStateManager.rotate(-59, -1, 0, 3);
                                // Don't cast as float
                                GlStateManager.rotate(-(System.currentTimeMillis() / 2 % 360), 1, 0, 0.0F);
                                GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
                                break;

                            case 6:
                                //sigma
                                itemRenderer.transformFirstPersonItem(animationProgression, staticStartSwingProgressFloat);
                                this.translate(0.0f, 0.1F, 0.0F);
                                itemRenderer.blockTransformation();
                                GlStateManager.rotate(convertedProgress * 35.0F / 2.0F, 0.0F, 1.0F, 1.5F);
                                GlStateManager.rotate(-convertedProgress * 135.0F / 4.0F, 1.0f, 1.0F, 0.0F);
                                break;

                            case 7:
                                //wood
                                itemRenderer.transformFirstPersonItem(animationProgression / 2.0F, staticStartSwingProgressFloat);
                                this.translate(0.0F, 0.3F, -0.0F);
                                GlStateManager.rotate(-convertedProgress * 30.0F, 1.0F, 0.0F, 2.0F);
                                GlStateManager.rotate(-convertedProgress * 44.0F, 1.5F, (convertedProgress / 1.2F), 0.0F);
                                itemRenderer.blockTransformation();

                                break;

                            case 8:
                                //swong
                                itemRenderer.transformFirstPersonItem(animationProgression / 2.0F, swingProgress);
                                GlStateManager.rotate(convertedProgress * 30.0F / 2.0F, -convertedProgress, -0.0F, 9.0F);
                                GlStateManager.rotate(convertedProgress * 40.0F, 1.0F, -convertedProgress / 2.0F, -0.0F);
                                this.translate(0.0F, 0.2F, 0.0F);
                                itemRenderer.blockTransformation();

                                break;

                            case 9:
                                //chill
                                itemRenderer.transformFirstPersonItem(-0.25F, 1.0F + convertedProgress / 10.0F);
                                GL11.glRotated(-convertedProgress * 25.0F, 1.0F, 0.0F, 0.0F);
                                itemRenderer.blockTransformation();

                                break;

                            case 10:
                                //komorebi
                                this.translate(0.41F, -0.25F, -0.5555557F);
                                this.translate(0.0F, 0, 0.0F);
                                GlStateManager.rotate(35.0F, 0f, 1.5F, 0.0F);

                                final float racism = MathHelper.sin(swingProgress * swingProgress / 64 * (float) Math.PI);

                                GlStateManager.rotate(racism * -5.0F, 0.0F, 0.0F, 0.0F);
                                GlStateManager.rotate(convertedProgress * -12.0F, 0.0F, 0.0F, 1.0F);
                                GlStateManager.rotate(convertedProgress * -65.0F, 1.0F, 0.0F, 0.0F);
                                itemRenderer.blockTransformation();

                                break;

                            case 11:
                                //rhys
                                itemRenderer.transformFirstPersonItem(animationProgression, swingProgress);
                                itemRenderer.blockTransformation();
                                this.translate(-0.3F, -0.1F, -0.0F);
                                break;

                            case 12:
                                //Allah
                                itemRenderer.transformFirstPersonItem(animationProgression, staticStartSwingProgressFloat);
                                itemRenderer.blockTransformation();
                                break;
                        }
                        break;
                    case EAT:
                    case DRINK:
                        switch ((int) otherAnimation.getInput()) {
                            case 0:
                                //none
                                func_178104_a(mc.thePlayer.getHeldItem(), mc.thePlayer, event.getPartialTicks());
                                itemRenderer.transformFirstPersonItem(animationProgression, 0.0F);
                                break;
                            case 1:
                                //1.7
                                func_178104_a(mc.thePlayer.getHeldItem(), mc.thePlayer, event.getPartialTicks());
                                itemRenderer.transformFirstPersonItem(animationProgression, swingProgress);
                                break;
                        }
                        break;
                    case BOW:
                        switch ((int) otherAnimation.getInput()) {
                            case 0:
                                //none
                                itemRenderer.transformFirstPersonItem(animationProgression, 0.0F);
                                func_178098_a(mc.thePlayer.getHeldItem(), event.getPartialTicks(), mc.thePlayer);
                                break;
                            case 1:
                                //1.7
                                itemRenderer.transformFirstPersonItem(animationProgression, swingProgress);
                                func_178098_a(mc.thePlayer.getHeldItem(), event.getPartialTicks(), mc.thePlayer);
                                break;
                        }
                        break;
                }

                event.setCanceled(true);

            } else {
                switch ((int) swingAnimation.getInput()) {
                    case 0:
                        //none
                        func_178105_d(swingProgress);
                        itemRenderer.transformFirstPersonItem(animationProgression, swingProgress);
                        break;

                    case 1:
                        //1.9+
                        func_178105_d(swingProgress);
                        itemRenderer.transformFirstPersonItem(animationProgression, swingProgress);
                        this.translate(0, -((swing - 1) -
                                (swing == 0 ? 0 : Utils.getTimer().renderPartialTicks)) / 5f, 0);
                        break;

                    case 2:
                        //smooth
                        itemRenderer.transformFirstPersonItem(animationProgression, swingProgress);
                        func_178105_d(animationProgression);
                        break;

                    case 3:
                        //punch
                        itemRenderer.transformFirstPersonItem(animationProgression, swingProgress);
                        func_178105_d(swingProgress);
                        break;

                    case 4:
                        //shove
                        itemRenderer.transformFirstPersonItem(animationProgression, animationProgression);
                        func_178105_d(swingProgress);
                        break;
                }

                event.setCanceled(true);
            }
        } catch (Exception ignored) {
        }
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        try {
            if (mc.thePlayer.swingProgressInt == 1) {
                swing = 9;
            } else {
                swing = Math.max(0, swing - 1);
            }
        } catch (Exception ignore) {
        }
    }

    @SubscribeEvent
    public void onSwingAnimation(@NotNull SwingAnimationEvent event) {
        event.setAnimationEnd(event.getAnimationEnd() * (int) ((-swingSpeed.getInput() / 100) + 1));
    }


    private void translate(double x, double y, double z) {
        GlStateManager.translate(
                x + this.translatex.getInput(),
                y + this.translatey.getInput(),
                z + this.translatez.getInput()
        );
    }

    private void pretranslate(double x, double y, double z) {
        GlStateManager.translate(
                x + this.pretranslatex.getInput(),
                y + this.pretranslatey.getInput(),
                z + this.pretranslatez.getInput()
        );
    }

    private void scale(double staticscalemultiplier_x, double staticscalemultiplier_y, double staticscalemultiplier_z) {
        GlStateManager.scale(
                staticscalemultiplier_x * this.scalex.getInput(),
                staticscalemultiplier_y * this.scaley.getInput(),
                staticscalemultiplier_z * this.scalez.getInput()
        );
    }

    private void rotate(float rotatex, float rotatey, float rotatez) {
        //x rotation
        GlStateManager.rotate(
                (float) this.rotatex.getInput(),
                1,
                0,
                0
        );

        //y rotation
        GlStateManager.rotate(
                (float) this.rotatey.getInput(),
                0,
                1,
                0
        );

        //z rotation
        GlStateManager.rotate(
                (float) this.rotatez.getInput(),
                0,
                0,
                1
        );
    }
    /**
     //* @see net.minecraft.client.renderer.ItemRenderer#func_178105_d(float swingProgress)
     */
    private void func_178105_d(float swingProgress) {
        float f = -0.4F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927F);
        float f1 = 0.2F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * 3.1415927F * 2.0F);
        float f2 = -0.2F * MathHelper.sin(swingProgress * 3.1415927F);
        this.translate(f, f1, f2);
    }

    /**
     //* @see net.minecraft.client.renderer.ItemRenderer#func_178104_a(AbstractClientPlayer player, float swingProgress)
     */
    private void func_178104_a(ItemStack itemToRender, @NotNull AbstractClientPlayer p_178104_1_, float p_178104_2_) {
        if (itemToRender == null) return;

        float f = (float) p_178104_1_.getItemInUseCount() - p_178104_2_ + 1.0F;
        float f1 = f / (float) itemToRender.getMaxItemUseDuration();
        float f2 = MathHelper.abs(MathHelper.cos(f / 4.0F * 3.1415927F) * 0.1F);
        if (f1 >= 0.8F) {
            f2 = 0.0F;
        }

        this.translate(0.0F, f2, 0.0F);
        float f3 = 1.0F - (float) Math.pow(f1, 27.0);
        this.translate(f3 * 0.6F, f3 * -0.5F, f3 * 0.0F);
        GlStateManager.rotate(f3 * 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f3 * 10.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(f3 * 30.0F, 0.0F, 0.0F, 1.0F);
    }

    /**
     //* @see net.minecraft.client.renderer.ItemRenderer#func_178098_a(float, AbstractClientPlayer)
     */
    private void func_178098_a(@NotNull ItemStack itemToRender, float p_178098_1_, @NotNull AbstractClientPlayer p_178098_2_) {
        GlStateManager.rotate(-18.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(-12.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-8.0F, 1.0F, 0.0F, 0.0F);
        this.translate(-0.9F, 0.2F, 0.0F);
        float f = (float) itemToRender.getMaxItemUseDuration() - ((float) p_178098_2_.getItemInUseCount() - p_178098_1_ + 1.0F);
        float f1 = f / 20.0F;
        f1 = (f1 * f1 + f1 * 2.0F) / 3.0F;
        if (f1 > 1.0F) {
            f1 = 1.0F;
        }

        if (f1 > 0.1F) {
            float f2 = MathHelper.sin((f - 0.1F) * 1.3F);
            float f3 = f1 - 0.1F;
            float f4 = f2 * f3;
            this.translate(f4 * 0.0F, f4 * 0.01F, f4 * 0.0F);
        }

        this.translate(f1 * 0.0F, f1 * 0.0F, f1 * 0.1F);
        GlStateManager.scale(1.0F, 1.0F, 1.0F + f1 * 0.2F);
    }
}
