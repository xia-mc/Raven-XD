package keystrokesmod.mixins.impl.entity;

import com.mojang.authlib.GameProfile;
import keystrokesmod.event.*;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.movement.NoSlow;
import keystrokesmod.module.impl.movement.Sprint;
import keystrokesmod.module.impl.movement.fly.FakeFly;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.utility.RotationUtils;
import keystrokesmod.utility.movement.Direction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C0CPacketInput;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovementInput;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    /**
     * @author strangerrrs
     * @reason mixin on update
     */
    @Overwrite
    public void onUpdate() {
        if (this.worldObj.isBlockLoaded(new BlockPos(this.posX, 0.0, this.posZ))) {
            RotationUtils.prevRenderPitch = RotationUtils.renderPitch;
            RotationUtils.prevRenderYaw = RotationUtils.renderYaw;

            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new PreUpdateEvent());

            super.onUpdate();

            if (this.isRiding()) {
                this.sendQueue.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(this.rotationYaw, this.rotationPitch, this.onGround));
                this.sendQueue.addToSendQueue(new C0CPacketInput(this.moveStrafing, this.moveForward, this.movementInput.jump, this.movementInput.sneak));
            } else {
                this.onUpdateWalkingPlayer();
            }

            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new PostUpdateEvent());
        }

    }

    /**
     * @author strangerrrs
     * @reason mixin on update walking player
     */
    @Overwrite
    public void onUpdateWalkingPlayer() {
        if (ModuleManager.rotationHandler != null)
            ModuleManager.rotationHandler.onPrePreMotion();

        PreMotionEvent preMotionEvent = new PreMotionEvent(
                this.posX,
                this.getEntityBoundingBox().minY,
                this.posZ,
                RotationHandler.getRotationYaw(rotationYaw),
                RotationHandler.getRotationPitch(rotationPitch),
                this.onGround,
                this.isSprinting(),
                this.isSneaking()
        );

        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(preMotionEvent);
        if (preMotionEvent.isCanceled())
            return;

        boolean flag = preMotionEvent.isSprinting();
        if (flag != this.serverSprintState) {
            if (flag) {
                this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.START_SPRINTING));
            } else {
                this.sendQueue.addToSendQueue(new C0BPacketEntityAction(this, C0BPacketEntityAction.Action.STOP_SPRINTING));
            }

            this.serverSprintState = flag;
        }

        boolean flag1 = preMotionEvent.isSneaking();
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
                RotationUtils.setRenderYaw(preMotionEvent.getYaw());
                preMotionEvent.setRenderYaw(false);
            }

            if (FakeFly.hideRotation()) {
                RotationUtils.renderPitch = rotationPitch;
                RotationUtils.renderYaw = rotationYaw;
            } else {
                RotationUtils.renderPitch = preMotionEvent.getPitch();
                RotationUtils.renderYaw = preMotionEvent.getYaw();
            }

            double d0 = preMotionEvent.getPosX() - this.lastReportedPosX;
            double d1 = preMotionEvent.getPosY() - this.lastReportedPosY;
            double d2 = preMotionEvent.getPosZ() - this.lastReportedPosZ;
            double d3 = preMotionEvent.getYaw() - this.lastReportedYaw;
            double d4 = preMotionEvent.getPitch() - this.lastReportedPitch;
            boolean flag2 = d0 * d0 + d1 * d1 + d2 * d2 > 9.0E-4 || this.positionUpdateTicks >= 20;
            boolean flag3 = d3 != 0.0 || d4 != 0.0;
            if (this.ridingEntity == null) {
                if (flag2 && flag3) {
                    this.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(preMotionEvent.getPosX(), preMotionEvent.getPosY(), preMotionEvent.getPosZ(), preMotionEvent.getYaw(), preMotionEvent.getPitch(), preMotionEvent.isOnGround()));
                } else if (flag2) {
                    this.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(preMotionEvent.getPosX(), preMotionEvent.getPosY(), preMotionEvent.getPosZ(), preMotionEvent.isOnGround()));
                } else if (flag3) {
                    this.sendQueue.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(preMotionEvent.getYaw(), preMotionEvent.getPitch(), preMotionEvent.isOnGround()));
                } else {
                    this.sendQueue.addToSendQueue(new C03PacketPlayer(preMotionEvent.isOnGround()));
                }
            } else {
                this.sendQueue.addToSendQueue(new C03PacketPlayer.C06PacketPlayerPosLook(this.motionX, -999.0D, this.motionZ, preMotionEvent.getYaw(), preMotionEvent.getPitch(), preMotionEvent.isOnGround()));
                flag2 = false;
            }

            ++this.positionUpdateTicks;

            if (flag2) {
                this.lastReportedPosX = preMotionEvent.getPosX();
                this.lastReportedPosY = preMotionEvent.getPosY();
                this.lastReportedPosZ = preMotionEvent.getPosZ();
                this.positionUpdateTicks = 0;
            }

            if (flag3) {
                this.lastReportedYaw = preMotionEvent.getYaw();
                this.lastReportedPitch = preMotionEvent.getPitch();
            }
        }

        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new PostMotionEvent());
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
                this.mc.displayGuiScreen(null);
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
        boolean usingItemModified = this.isUsingItem() || (ModuleManager.killAura != null && ModuleManager.killAura.isEnabled() && ModuleManager.killAura.block.get() && ((Object) this) == Minecraft.getMinecraft().thePlayer && ModuleManager.killAura.rmbDown && ModuleManager.killAura.manualBlock.isToggled());
        boolean stopSprint = (this.isUsingItem() && ModuleManager.noSlow != null && ModuleManager.noSlow.isEnabled() && NoSlow.getForwardSlowed() == 0.8);

        if (!stopSprint) {
            stopSprint = Sprint.stopSprint();
        }

        if (usingItemModified && !this.isRiding()) {
            MovementInput var10000 = this.movementInput;
            var10000.moveStrafe *= NoSlow.getStrafeSlowed();
            var10000 = this.movementInput;
            var10000.moveForward *= NoSlow.getForwardSlowed();
            if (stopSprint) {
                this.sprintToggleTimer = 0;
            }
        }

        this.pushOutOfBlocks(this.posX - (double) this.width * 0.35, this.getEntityBoundingBox().minY + 0.5, this.posZ + (double) this.width * 0.35);
        this.pushOutOfBlocks(this.posX - (double) this.width * 0.35, this.getEntityBoundingBox().minY + 0.5, this.posZ - (double) this.width * 0.35);
        this.pushOutOfBlocks(this.posX + (double) this.width * 0.35, this.getEntityBoundingBox().minY + 0.5, this.posZ - (double) this.width * 0.35);
        this.pushOutOfBlocks(this.posX + (double) this.width * 0.35, this.getEntityBoundingBox().minY + 0.5, this.posZ + (double) this.width * 0.35);
        boolean flag3 = (float) this.getFoodStats().getFoodLevel() > 6.0F || this.capabilities.allowFlying;
        if (this.onGround && !flag1 && !flag2 && (Sprint.omni() || this.movementInput.moveForward >= f) && !this.isSprinting() && flag3 && (!usingItemModified || !stopSprint) && !this.isPotionActive(Potion.blindness)) {
            if (this.sprintToggleTimer <= 0 && !this.mc.gameSettings.keyBindSprint.isKeyDown()) {
                this.sprintToggleTimer = 7;
            } else {
                this.setSprinting(true);
            }
        }

        if ((!this.isSprinting() && (Sprint.omni() || this.movementInput.moveForward >= f) && flag3 && (!usingItemModified || !stopSprint) && !this.isPotionActive(Potion.blindness) && this.mc.gameSettings.keyBindSprint.isKeyDown())) {
            this.setSprinting(true);
        }

        if (this.isSprinting() && (!Sprint.omni() && (this.movementInput.moveForward < f || this.isCollidedHorizontally || !flag3) || (ModuleManager.scaffold != null && ModuleManager.scaffold.isEnabled() && !ModuleManager.scaffold.sprint() && !ModuleManager.tower.canSprint()))) {
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
                this.motionY -= this.capabilities.getFlySpeed() * 3.0F;
            }

            if (this.movementInput.jump) {
                this.motionY += this.capabilities.getFlySpeed() * 3.0F;
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
