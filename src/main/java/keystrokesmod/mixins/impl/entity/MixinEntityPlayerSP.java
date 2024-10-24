package keystrokesmod.mixins.impl.entity;

import com.mojang.authlib.GameProfile;
import keystrokesmod.event.*;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.movement.NoSlow;
import keystrokesmod.module.impl.movement.Sprint;
import keystrokesmod.module.impl.movement.fly.FakeFly;
import keystrokesmod.utility.RotationUtils;
import keystrokesmod.utility.movement.Direction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovementInput;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

import static keystrokesmod.utility.movement.Direction.*;

@Mixin(value = EntityPlayerSP.class, priority = 999)
public abstract class MixinEntityPlayerSP extends AbstractClientPlayer {
    @Shadow
    public int sprintingTicksLeft;

    public MixinEntityPlayerSP(World p_i45074_1_, GameProfile p_i45074_2_) {
        super(p_i45074_1_, p_i45074_2_);
    }

    @Override
    @Shadow
    public abstract void setSprinting(boolean p_setSprinting_1_);

    @Shadow
    protected int sprintToggleTimer;
    @Shadow
    public float prevTimeInPortal;
    @Shadow
    public float timeInPortal;
    @Shadow
    protected Minecraft mc;
    @Shadow
    public MovementInput movementInput;

    @Override
    @Shadow
    public abstract void sendPlayerAbilities();

    @Shadow
    protected abstract boolean isCurrentViewEntity();

    @Shadow
    public abstract boolean isRidingHorse();

    @Shadow
    private int horseJumpPowerCounter;
    @Shadow
    private float horseJumpPower;

    @Shadow
    protected abstract void sendHorseJump();

    @Shadow
    private boolean serverSprintState;
    @Shadow
    @Final
    public NetHandlerPlayClient sendQueue;

    @Override
    @Shadow
    public abstract boolean isSneaking();

    @Shadow
    private boolean serverSneakState;
    @Shadow
    private double lastReportedPosX;
    @Shadow
    private double lastReportedPosY;
    @Shadow
    private double lastReportedPosZ;
    @Shadow
    private float lastReportedYaw;
    @Shadow
    private float lastReportedPitch;
    @Shadow
    private int positionUpdateTicks;

    @Unique
    private boolean raven_bS$isHeadspaceFree(BlockPos p_isHeadspaceFree_1_, int p_isHeadspaceFree_2_) {
        for(int y = 0; y < p_isHeadspaceFree_2_; ++y) {
            if (!this.isOpenBlockSpace(p_isHeadspaceFree_1_.add(0, y, 0))) {
                return false;
            }
        }

        return true;
    }

    @Shadow protected abstract boolean isOpenBlockSpace(BlockPos p_isOpenBlockSpace_1_);

    @Inject(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;onUpdate()V"), cancellable = true)
    public void onPreUpdate(CallbackInfo ci) {
        RotationUtils.prevRenderPitch = RotationUtils.renderPitch;
        RotationUtils.prevRenderYaw = RotationUtils.renderYaw;

        PreUpdateEvent event = new PreUpdateEvent();
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled())
            ci.cancel();
    }

    @Inject(method = "onUpdate", at = @At("RETURN"))
    public void onPostUpdate(CallbackInfo ci) {
        if (this.worldObj.isBlockLoaded(new BlockPos(this.posX, 0.0, this.posZ))) {  // ensure compat
            MinecraftForge.EVENT_BUS.post(new PostUpdateEvent());
        }
    }

    /**
     * @author strangerrrs
     * @reason mixin on update walking player
     */
    @Overwrite
    public void onUpdateWalkingPlayer() {

        //Todo: EventUpdate Pre
        PreMotionEvent pre = new PreMotionEvent(
                this.posX,
                this.posY,
                this.posZ,
                rotationYaw,
                rotationPitch,
                this.onGround,
                this.isSprinting(),
                this.isSneaking()
        );

        MinecraftForge.EVENT_BUS.post(pre);
        if (pre.isCanceled()) {
            //Todo: EventUpdate Post
            MinecraftForge.EVENT_BUS.post(new PostMotionEvent());
            return;
        }

        boolean flag = pre.isSprinting();
        if (flag != this.serverSprintState) {
            if (flag) {
                this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.START_SPRINTING));
            } else {
                this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.STOP_SPRINTING));
            }

            this.serverSprintState = flag;
        }

        boolean flag1 = pre.isSneaking();

        if (flag1 != this.serverSneakState) {
            if (flag1) {
                this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.START_SNEAKING));
            } else {
                this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.STOP_SNEAKING));
            }

            this.serverSneakState = flag1;
        }

        if (this.isCurrentViewEntity()) {

            if (PreMotionEvent.setRenderYaw()) {
                RotationUtils.setRenderYaw(pre.getYaw());
                pre.setRenderYaw(false);
            }

            if (FakeFly.hideRotation()) {
                RotationUtils.renderPitch = rotationPitch;
                RotationUtils.renderYaw = rotationYaw;
            } else {
                RotationUtils.renderPitch = pre.getPitch();
                RotationUtils.renderYaw = pre.getYaw();
            }

            double d0 = pre.getPosX() - this.lastReportedPosX;
            double d1 = pre.getPosY() - this.lastReportedPosY;
            double d2 = pre.getPosZ() - this.lastReportedPosZ;
            double d3 = (double) (pre.getYaw() - this.lastReportedYaw);
            double d4 = (double) (pre.getPitch() - this.lastReportedPitch);
            boolean flag2 = d0 * d0 + d1 * d1 + d2 * d2 > 9.0E-4D || this.positionUpdateTicks >= 20;
            boolean flag3 = d3 != 0.0D || d4 != 0.0D;

            if (this.ridingEntity == null) {
                if (flag2 && flag3) {
                    this.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(pre.getPosX(), pre.getPosY(), pre.getPosZ(), pre.getYaw(), pre.getPitch(), pre.isOnGround()));
                } else if (flag2) {
                    this.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(pre.getPosX(), pre.getPosY(), pre.getPosZ(), pre.isOnGround()));
                } else if (flag3) {
                    this.sendQueue.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(pre.getYaw(), pre.getPitch(), pre.isOnGround()));
                } else {
                    this.sendQueue.addToSendQueue(new C03PacketPlayer(pre.isOnGround()));
                }
            } else {
                this.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(this.motionX, -999.0D, this.motionZ, pre.getYaw(), pre.getPitch(), pre.isOnGround()));
                flag2 = false;
            }

            ++this.positionUpdateTicks;

            if (flag2) {
                this.lastReportedPosX = pre.getPosX();
                this.lastReportedPosY = pre.getPosY();
                this.lastReportedPosZ = pre.getPosZ();
                this.positionUpdateTicks = 0;
            }

            if (flag3) {
                this.lastReportedYaw = pre.getYaw();
                this.lastReportedPitch = pre.getPitch();
            }
        }

        //Todo: EventUpdate Post
        MinecraftForge.EVENT_BUS.post(new PostMotionEvent());
    }

    /**
     * @author strangerrrs
     * @reason mixin on living update
     */
    @Overwrite
    public void onLivingUpdate() {
        if (this.sprintingTicksLeft > 0) {
            --this.sprintingTicksLeft;

            if (this.sprintingTicksLeft == 0) {
                this.setSprinting(false);
            }
        }

        if (this.sprintToggleTimer > 0) {
            --this.sprintToggleTimer;
        }

        this.prevTimeInPortal = this.timeInPortal;

        if (this.inPortal) {
            if (this.mc.currentScreen != null && !this.mc.currentScreen.doesGuiPauseGame()) {
                this.mc.displayGuiScreen((GuiScreen) null);
            }

            if (this.timeInPortal == 0.0F) {
                this.mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("portal.trigger"), this.rand.nextFloat() * 0.4F + 0.8F));
            }

            this.timeInPortal += 0.0125F;

            if (this.timeInPortal >= 1.0F) {
                this.timeInPortal = 1.0F;
            }

            this.inPortal = false;
        } else if (this.isPotionActive(Potion.confusion) && this.getActivePotionEffect(Potion.confusion).getDuration() > 60) {
            this.timeInPortal += 0.006666667F;

            if (this.timeInPortal > 1.0F) {
                this.timeInPortal = 1.0F;
            }
        } else {
            if (this.timeInPortal > 0.0F) {
                this.timeInPortal -= 0.05F;
            }

            if (this.timeInPortal < 0.0F) {
                this.timeInPortal = 0.0F;
            }
        }

        if (this.timeUntilPortal > 0) {
            --this.timeUntilPortal;
        }

        boolean flag = this.movementInput.jump;
        boolean flag1 = this.movementInput.sneak;
        float f = 0.8F;
        boolean flag2 = this.movementInput.moveForward >= f;
        this.movementInput.updatePlayerMoveState();

        // no slow
        @SuppressWarnings("EqualsBetweenInconvertibleTypes")
        final boolean autoBlocking = ModuleManager.killAura != null
                && ModuleManager.killAura.isEnabled()
                && ModuleManager.killAura.block.get()
                && Objects.equals(this, Minecraft.getMinecraft().thePlayer)
                && ModuleManager.killAura.autoBlockMode.getInput() != 0;
        final boolean usingItemModified = this.isUsingItem() || autoBlocking;
        boolean stopSprint = Sprint.stopSprint() || this.isUsingItem()
                && (ModuleManager.noSlow != null && ModuleManager.noSlow.isEnabled() && NoSlow.getForwardSlowed() <= 0.8)
                || (autoBlocking && ModuleManager.killAura.slowdown.getInput() <= 0.8);

        if (usingItemModified && !this.isRiding()) {
            MovementInput var10000 = this.movementInput;
            var10000.moveStrafe *= autoBlocking ? (float) ModuleManager.killAura.slowdown.getInput() : NoSlow.getStrafeSlowed();
            var10000 = this.movementInput;
            var10000.moveForward *= autoBlocking ? (float) ModuleManager.killAura.slowdown.getInput() : NoSlow.getForwardSlowed();
            if (stopSprint) {
                this.sprintToggleTimer = 0;
            }
        }

        this.pushOutOfBlocks(this.posX - (double) this.width * 0.35D, this.getEntityBoundingBox().minY + 0.5D, this.posZ + (double) this.width * 0.35D);
        this.pushOutOfBlocks(this.posX - (double) this.width * 0.35D, this.getEntityBoundingBox().minY + 0.5D, this.posZ - (double) this.width * 0.35D);
        this.pushOutOfBlocks(this.posX + (double) this.width * 0.35D, this.getEntityBoundingBox().minY + 0.5D, this.posZ - (double) this.width * 0.35D);
        this.pushOutOfBlocks(this.posX + (double) this.width * 0.35D, this.getEntityBoundingBox().minY + 0.5D, this.posZ + (double) this.width * 0.35D);
        boolean flag3 = (float) this.getFoodStats().getFoodLevel() > 6.0F || this.capabilities.allowFlying;

        if (this.onGround && !flag1 && !flag2 && this.movementInput.moveForward >= f && !this.isSprinting() && flag3 && !this.isUsingItem() && !this.isPotionActive(Potion.blindness)) {
            if (this.sprintToggleTimer <= 0 && !this.mc.gameSettings.keyBindSprint.isKeyDown()) {
                this.sprintToggleTimer = 7;
            } else {
                this.setSprinting(true);
            }
        }

        if (!this.isSprinting() && this.movementInput.moveForward >= f && flag3 && !this.isUsingItem() && !this.isPotionActive(Potion.blindness) && this.mc.gameSettings.keyBindSprint.isKeyDown()) {
            this.setSprinting(true);
        }

        if (this.isSprinting() && (this.movementInput.moveForward < f || this.isCollidedHorizontally || !flag3)) {
            this.setSprinting(false);
        }

        if (this.capabilities.allowFlying) {
            if (this.mc.playerController.isSpectatorMode()) {
                if (!this.capabilities.isFlying) {
                    this.capabilities.isFlying = true;
                    this.sendPlayerAbilities();
                }
            } else if (!flag && this.movementInput.jump) {
                if (this.flyToggleTimer == 0) {
                    this.flyToggleTimer = 7;
                } else {
                    this.capabilities.isFlying = !this.capabilities.isFlying;
                    this.sendPlayerAbilities();
                    this.flyToggleTimer = 0;
                }
            }
        }

        if (this.capabilities.isFlying && this.isCurrentViewEntity()) {
            if (this.movementInput.sneak) {
                this.motionY -= (double) (this.capabilities.getFlySpeed() * 3.0F);
            }

            if (this.movementInput.jump) {
                this.motionY += (double) (this.capabilities.getFlySpeed() * 3.0F);
            }
        }

        if (this.isRidingHorse()) {
            if (this.horseJumpPowerCounter < 0) {
                ++this.horseJumpPowerCounter;

                if (this.horseJumpPowerCounter == 0) {
                    this.horseJumpPower = 0.0F;
                }
            }

            if (flag && !this.movementInput.jump) {
                this.horseJumpPowerCounter = -10;
                this.sendHorseJump();
            } else if (!flag && this.movementInput.jump) {
                this.horseJumpPowerCounter = 0;
                this.horseJumpPower = 0.0F;
            } else if (flag) {
                ++this.horseJumpPowerCounter;

                if (this.horseJumpPowerCounter < 10) {
                    this.horseJumpPower = (float) this.horseJumpPowerCounter * 0.1F;
                } else {
                    this.horseJumpPower = 0.8F + 2.0F / (float) (this.horseJumpPowerCounter - 9) * 0.1F;
                }
            }
        } else {
            this.horseJumpPower = 0.0F;
        }

        super.onLivingUpdate();

        if (this.onGround && this.capabilities.isFlying && !this.mc.playerController.isSpectatorMode()) {
            this.capabilities.isFlying = false;
            this.sendPlayerAbilities();
        }
    }

    /**
     * @author xia__mc
     * @reason for vulcan phase
     */
    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    protected void pushOutOfBlocks(double p_pushOutOfBlocks_1_, double p_pushOutOfBlocks_3_, double p_pushOutOfBlocks_5_, CallbackInfoReturnable<Boolean> cir) {
        if (!this.noClip) {
            BlockPos blockpos = new BlockPos(p_pushOutOfBlocks_1_, p_pushOutOfBlocks_3_, p_pushOutOfBlocks_5_);
            double d0 = p_pushOutOfBlocks_1_ - (double) blockpos.getX();
            double d1 = p_pushOutOfBlocks_5_ - (double) blockpos.getZ();
            int entHeight = Math.max((int) Math.ceil(this.height), 1);
            if (!this.raven_bS$isHeadspaceFree(blockpos, entHeight)) {
                Direction direction = null;
                double d2 = 9999.0;
                if (this.raven_bS$isHeadspaceFree(blockpos.west(), entHeight) && d0 < d2) {
                    d2 = d0;
                    direction = NEGATIVE_X;
                }

                if (this.raven_bS$isHeadspaceFree(blockpos.east(), entHeight) && 1.0 - d0 < d2) {
                    d2 = 1.0 - d0;
                    direction = POSITIVE_X;
                }

                if (this.raven_bS$isHeadspaceFree(blockpos.north(), entHeight) && d1 < d2) {
                    d2 = d1;
                    direction = NEGATIVE_Z;
                }

                if (this.raven_bS$isHeadspaceFree(blockpos.south(), entHeight) && 1.0 - d1 < d2) {
                    direction = POSITIVE_Z;
                }

                if (direction != null) {
                    PushOutOfBlockEvent event = new PushOutOfBlockEvent(direction, 0.1F);
                    MinecraftForge.EVENT_BUS.post(event);
                    if (event.isCanceled())
                        cir.setReturnValue(false);
                    direction = event.getDirection();
                    final float pushMotion = event.getPushMotion();

                    switch (direction) {
                        case POSITIVE_X:
                            this.motionX = pushMotion;
                            break;
                        case NEGATIVE_X:
                            this.motionX = -pushMotion;
                            break;
                        case POSITIVE_Z:
                            this.motionZ = pushMotion;
                            break;
                        case NEGATIVE_Z:
                            this.motionZ = -pushMotion;
                            break;
                    }
                }
            }

        }
        cir.setReturnValue(false);
    }
}