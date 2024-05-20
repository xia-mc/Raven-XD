package keystrokesmod.script.classes;

import keystrokesmod.utility.Reflection;
import keystrokesmod.utility.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLadder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class Entity {
    public net.minecraft.entity.Entity entity;
    public String type;
    public int entityID;

    public Entity(net.minecraft.entity.Entity entity) {
        this.entity = entity;
        if (entity == null) {
            return;
        }
        this.type = entity.getClass().getSimpleName();
        this.entityID = entity.getEntityId();
    }

    public boolean allowEditing() {
        if (!(entity instanceof EntityPlayer)) {
            return false;
        }
        return (((EntityPlayer) entity).capabilities.allowEdit);
    }

    public double distanceTo(Vec3 position) {
        return entity.getDistance(position.x, position.y, position.z);
    }

    public double distanceToSq(Vec3 position) {
        return entity.getDistanceSq(position.x, position.y, position.z);
    }

    public double distanceToGround() {
        return 0; // does not work
    }

    public float getAbsorption() {
        if (!(entity instanceof EntityLivingBase)) {
            return -1;
        }
        return (((EntityLivingBase) entity).getAbsorptionAmount());
    }

    public Vec3 getBlockPosition() {
        return new Vec3(entity.getPosition().getX(), entity.getPosition().getY(), entity.getPosition().getZ());
    }

    public String getDisplayName() {
        return entity.getDisplayName().getFormattedText();
    }

    public float getFallDistance() {
        return entity.fallDistance;
    }

    public String getUUID() {
        if (!(entity instanceof EntityPlayer)) {
            return entity.getUniqueID().toString();
        }
        return getNetworkPlayer().getUUID();
    }

    public float getHealth() {
        if (!(entity instanceof EntityLivingBase)) {
            return -1;
        }
        return ((EntityLivingBase) entity).getHealth();
    }



    public float getEyeHeight() {
        return entity.getEyeHeight();
    }

    public float getHeight() {
        return entity.height;
    }

    public float getWidth() {
        return entity.width;
    }

    public ItemStack getHeldItem() {
        if (!(entity instanceof EntityLivingBase)) {
            return null;
        }
        net.minecraft.item.ItemStack stack = ((EntityLivingBase) entity).getHeldItem();
        if (stack == null) {
            return null;
        }
        return new ItemStack(stack);
    }

    public int getHurtTime() {
        if (!(entity instanceof EntityLivingBase)) {
            return -1;
        }
        return ((EntityLivingBase) entity).hurtTime;
    }

    public Vec3 getLastPos() {
        return new Vec3(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ);
    }

    public float getMaxHealth() {
        if (!(entity instanceof EntityLivingBase)) {
            return -1;
        }
        return ((EntityLivingBase) entity).getMaxHealth();
    }

    public int getMaxHurtTime() {
        if (!(entity instanceof EntityLivingBase)) {
            return -1;
        }
        return ((EntityLivingBase) entity).maxHurtTime;
    }

    public String getName() {
        return entity.getName();
    }

    public NetworkPlayer getNetworkPlayer() {
        if (!(entity instanceof EntityPlayer)) {
            return null;
        }
        NetworkPlayer networkPlayer = null;
        try {
            networkPlayer = new NetworkPlayer((NetworkPlayerInfo) Reflection.getPlayerInfo.invoke(entity));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return networkPlayer;
    }

    public float getPitch() {
        return entity.rotationPitch;
    }

    public Vec3 getPosition() {
        if (entity == null) {
            return null;
        }
        return new Vec3(entity.posX, entity.posY, entity.posZ);
    }

    public Vec3 getPos() {
        if (entity == null) {
            return null;
        }
        return new Vec3(entity.posX, entity.posY, entity.posZ);
    }

    public List<Object[]> getPotionEffects() {
        List<Object[]> potionEffects = new ArrayList<>();
        if (!(entity instanceof EntityLivingBase)) {
            return potionEffects;
        }
        for (PotionEffect potionEffect : ((EntityLivingBase) entity).getActivePotionEffects()) {
            Object[] potionData = new Object[]{potionEffect.getEffectName(), potionEffect.getAmplifier(), potionEffect.getDuration()};
            potionEffects.add(potionData);
        }
        return potionEffects;
    }

    public double getSpeed() {
        return Utils.getHorizontalSpeed(entity);
    }

    public double getSwingProgress() {
        if (!(entity instanceof EntityLivingBase)) {
            return -1;
        }
        return ((EntityLivingBase) entity).swingProgress;
    }

    public int getTicksExisted() {
        return entity.ticksExisted;
    }

    public float getYaw() {
        return entity.rotationYaw;
    }

    public boolean isCreative() {
        if (!(entity instanceof EntityPlayer)) {
            return false;
        }
        return (((EntityPlayer) entity).capabilities.isCreativeMode);
    }

    public boolean isCollided() {
        return entity.isCollided;
    }

    public boolean isCollidedHorizontally() {
        return entity.isCollidedHorizontally;
    }

    public boolean isCollidedVertically() {
        return entity.isCollidedVertically;
    }

    public boolean isDead() {
        return entity.isDead;
    }

    public boolean isInvisible() {
        return entity.isInvisible();
    }

    public boolean isInWater() {
        return entity.isInWater();
    }

    public boolean isInLava() {
        return entity.isInLava();
    }

    public boolean isOnLadder() {
        int posX = MathHelper.floor_double(entity.posX);
        int posY = MathHelper.floor_double(entity.posY - 0.20000000298023224D);
        int posZ = MathHelper.floor_double(entity.posZ);
        BlockPos blockpos = new BlockPos(posX, posY, posZ);
        Block block1 = Minecraft.getMinecraft().theWorld.getBlockState(blockpos).getBlock();
        return block1 instanceof BlockLadder && !entity.onGround;
    }

    public boolean isOnEdge() {
        return Utils.onEdge(this.entity);
    }

    public boolean isSprinting() {
        return entity.isSprinting();
    }

    public boolean isSneak() {
        return entity.isSneaking();
    }

    public boolean isUsingItem() {
        if (!(entity instanceof EntityPlayer)) {
            return false;
        }
        return (((EntityPlayer) entity).isUsingItem());
    }

    public boolean onGround() {
        return entity.onGround;
    }

    public void setMotion(double x, double y, double z) {
        entity.motionX = x;
        entity.motionY = y;
        entity.motionZ = z;
    }

    public Vec3 getMotion() {
        return new Vec3(entity.motionX, entity.motionY, entity.motionZ);
    }

    public void setPitch(float pitch) {
        entity.rotationPitch = pitch;
    }

    public void setYaw(float yaw) {
        entity.rotationYaw = yaw;
    }

    public void setPosition(Vec3 position) {
        entity.setPosition(position.x, position.y, position.z);
    }
}
