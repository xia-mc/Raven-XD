package keystrokesmod.module.impl.other.anticheats.utils.phys;

import keystrokesmod.mixins.impl.entity.EntityLivingBaseAccessor;
import keystrokesmod.mixins.impl.entity.EntityPlayerAccessor;
import keystrokesmod.module.impl.other.anticheats.TRPlayer;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovementInput;
import net.minecraft.world.WorldSettings;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class PredictEngine extends EntityPlayer implements Cloneable {
    private final TRPlayer player;
    @Setter
    private MovementInput movementInput;
    @Setter
    private boolean sprinting = false;

    @Contract(pure = true)
    public PredictEngine(@NotNull TRPlayer player) {
        super(player.fabricPlayer.worldObj, player.fabricPlayer.getGameProfile());
        this.player = player;
        sync();
    }

    public void sync() {
        this.posX = player.fabricPlayer.posX;
        this.posY = player.fabricPlayer.posY;
        this.posZ = player.fabricPlayer.posZ;
        this.newPosX = ((EntityLivingBaseAccessor) player.fabricPlayer).getNewPosX();
        this.newPosY = ((EntityLivingBaseAccessor) player.fabricPlayer).getNewPosY();
        this.newPosZ = ((EntityLivingBaseAccessor) player.fabricPlayer).getNewPosZ();
        this.rotationYaw = player.fabricPlayer.rotationYaw;
        this.rotationPitch = player.fabricPlayer.rotationPitch;
        this.newRotationYaw = ((EntityLivingBaseAccessor) player.fabricPlayer).getNewRotationYaw();
        this.newRotationPitch = ((EntityLivingBaseAccessor) player.fabricPlayer).getNewRotationPitch();
        this.newPosRotationIncrements = ((EntityLivingBaseAccessor) player.fabricPlayer).getNewPosRotationIncrements();
        this.motionX = player.lastPos.x - player.currentPos.x;
        this.motionY = player.lastPos.y - player.currentPos.y;
        this.motionZ = player.lastPos.z - player.currentPos.z;
        this.fallDistance = player.fabricPlayer.fallDistance;
        this.dead = ((EntityLivingBaseAccessor) player.fabricPlayer).isDead();
        this.isDead = player.fabricPlayer.isDead;
        this.inventory = player.fabricPlayer.inventory;

        this.setPosition(this.posX, this.posY, this.posZ);
        this.setItemInUse(player.fabricPlayer.getItemInUse(), player.fabricPlayer.getItemInUseCount());
        ((EntityLivingBaseAccessor) this).setActivePotionsMap(((EntityLivingBaseAccessor) player.fabricPlayer).getActivePotionsMap());
    }

    @Override
    protected void updatePotionEffects() {
    }

    @Override
    public void onUpdate() {
        this.noClip = this.isSpectator();
        if (this.isSpectator()) {
            this.onGround = false;
        }

        if (this.getItemInUse() != null) {
            ItemStack itemstack = this.inventory.getCurrentItem();
            if (itemstack == this.getItemInUse()) {
                if (this.getItemInUseCount() <= 0) {
                    this.onItemUseFinish();
                } else {
                    this.getItemInUse().getItem().onUsingTick(this.getItemInUse(), this, this.getItemInUseCount());
                    if (this.getItemInUseCount() <= 25 && this.getItemInUseCount() % 4 == 0) {
                        this.updateItemUse(itemstack, 5);
                    }

                    ((EntityPlayerAccessor) this).setItemInUseCount(this.getItemInUseCount() - 1);
                    if (this.getItemInUseCount() == 0 && !this.worldObj.isRemote) {
                        this.onItemUseFinish();
                    }
                }
            } else {
                this.clearItemInUse();
            }
        }

        if (this.xpCooldown > 0) {
            --this.xpCooldown;
        }

        ((EntityLivingBase) this).onUpdate();

        if (this.isBurning() && this.capabilities.disableDamage) {
            this.extinguish();
        }

        this.prevChasingPosX = this.chasingPosX;
        this.prevChasingPosY = this.chasingPosY;
        this.prevChasingPosZ = this.chasingPosZ;
        double d5 = this.posX - this.chasingPosX;
        double d0 = this.posY - this.chasingPosY;
        double d1 = this.posZ - this.chasingPosZ;
        double d2 = 10.0;
        if (d5 > d2) {
            this.prevChasingPosX = this.chasingPosX = this.posX;
        }

        if (d1 > d2) {
            this.prevChasingPosZ = this.chasingPosZ = this.posZ;
        }

        if (d0 > d2) {
            this.prevChasingPosY = this.chasingPosY = this.posY;
        }

        if (d5 < -d2) {
            this.prevChasingPosX = this.chasingPosX = this.posX;
        }

        if (d1 < -d2) {
            this.prevChasingPosZ = this.chasingPosZ = this.posZ;
        }

        if (d0 < -d2) {
            this.prevChasingPosY = this.chasingPosY = this.posY;
        }

        this.chasingPosX += d5 * 0.25;
        this.chasingPosZ += d1 * 0.25;
        this.chasingPosY += d0 * 0.25;
        if (this.ridingEntity == null) {
            return;
        }

        if (!this.worldObj.isRemote) {
            this.foodStats.onUpdate(this);
            this.triggerAchievement(StatList.minutesPlayedStat);
            if (this.isEntityAlive()) {
                this.triggerAchievement(StatList.timeSinceDeathStat);
            }
        }

        double d3 = MathHelper.clamp_double(this.posX, -2.9999999E7, 2.9999999E7);
        double d4 = MathHelper.clamp_double(this.posZ, -2.9999999E7, 2.9999999E7);
        if (d3 != this.posX || d4 != this.posZ) {
            this.setPosition(d3, this.posY, d4);
        }
    }

    private boolean isHeadspaceFree(BlockPos p_isHeadspaceFree_1_, int p_isHeadspaceFree_2_) {
        for (int y = 0; y < p_isHeadspaceFree_2_; ++y) {
            if (!this.isOpenBlockSpace(p_isHeadspaceFree_1_.add(0, y, 0))) {
                return false;
            }
        }

        return true;
    }

    protected boolean pushOutOfBlocks(double p_pushOutOfBlocks_1_, double p_pushOutOfBlocks_3_, double p_pushOutOfBlocks_5_) {
        if (!this.noClip) {
            BlockPos blockpos = new BlockPos(p_pushOutOfBlocks_1_, p_pushOutOfBlocks_3_, p_pushOutOfBlocks_5_);
            double d0 = p_pushOutOfBlocks_1_ - (double) blockpos.getX();
            double d1 = p_pushOutOfBlocks_5_ - (double) blockpos.getZ();
            int entHeight = Math.max((int) Math.ceil(this.height), 1);
            boolean inTranslucentBlock = !this.isHeadspaceFree(blockpos, entHeight);
            if (inTranslucentBlock) {
                int i = -1;
                double d2 = 9999.0;
                if (this.isHeadspaceFree(blockpos.west(), entHeight) && d0 < d2) {
                    d2 = d0;
                    i = 0;
                }

                if (this.isHeadspaceFree(blockpos.east(), entHeight) && 1.0 - d0 < d2) {
                    d2 = 1.0 - d0;
                    i = 1;
                }

                if (this.isHeadspaceFree(blockpos.north(), entHeight) && d1 < d2) {
                    d2 = d1;
                    i = 4;
                }

                if (this.isHeadspaceFree(blockpos.south(), entHeight) && 1.0 - d1 < d2) {
                    i = 5;
                }

                float f = 0.1F;
                if (i == 0) {
                    this.motionX = -f;
                }

                if (i == 1) {
                    this.motionX = f;
                }

                if (i == 4) {
                    this.motionZ = -f;
                }

                if (i == 5) {
                    this.motionZ = f;
                }
            }

        }
        return false;
    }

    private boolean isOpenBlockSpace(BlockPos p_isOpenBlockSpace_1_) {
        return !this.worldObj.getBlockState(p_isOpenBlockSpace_1_).getBlock().isNormalCube();
    }

    public void onLivingUpdate() {
        boolean flag = this.movementInput.jump;
        this.movementInput.updatePlayerMoveState();
        if (this.isUsingItem() && !this.isRiding()) {
            MovementInput var10000 = this.movementInput;
            var10000.moveStrafe *= 0.2F;
            var10000.moveForward *= 0.2F;
        }

        this.pushOutOfBlocks(this.posX - (double) this.width * 0.35, this.getEntityBoundingBox().minY + 0.5, this.posZ + (double) this.width * 0.35);
        this.pushOutOfBlocks(this.posX - (double) this.width * 0.35, this.getEntityBoundingBox().minY + 0.5, this.posZ - (double) this.width * 0.35);
        this.pushOutOfBlocks(this.posX + (double) this.width * 0.35, this.getEntityBoundingBox().minY + 0.5, this.posZ - (double) this.width * 0.35);
        this.pushOutOfBlocks(this.posX + (double) this.width * 0.35, this.getEntityBoundingBox().minY + 0.5, this.posZ + (double) this.width * 0.35);

        if (this.capabilities.allowFlying) {
            if (player.currentGameType == WorldSettings.GameType.SPECTATOR) {
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

        if (this.capabilities.isFlying) {
            if (this.movementInput.sneak) {
                this.motionY -= this.capabilities.getFlySpeed() * 3.0F;
            }

            if (this.movementInput.jump) {
                this.motionY += this.capabilities.getFlySpeed() * 3.0F;
            }
        }

        super.onLivingUpdate();
        if (this.onGround && this.capabilities.isFlying && player.currentGameType != WorldSettings.GameType.SPECTATOR) {
            this.capabilities.isFlying = false;
        }

    }

    @Override
    public boolean isSprinting() {
        return sprinting;
    }

    @Override
    public PredictEngine clone() {
        try {
            return (PredictEngine) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean isSpectator() {
        return false;
    }
}
