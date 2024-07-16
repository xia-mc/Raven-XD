package keystrokesmod.utility;

import com.google.common.base.Predicates;
import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.impl.other.anticheats.utils.world.PlayerRotation;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.*;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class RotationUtils {
    public static final Minecraft mc = Minecraft.getMinecraft();
    public static float renderPitch;
    public static float prevRenderPitch;
    public static float renderYaw;
    public static float prevRenderYaw;

    public static void setRenderYaw(float yaw) {
        mc.thePlayer.rotationYawHead = yaw;
        if (RotationHandler.rotateBody.isToggled() && RotationHandler.fullBody.isToggled()) {
            mc.thePlayer.renderYawOffset = yaw;
        }
    }

    public static float[] getRotations(BlockPos blockPos, final float n, final float n2) {
        final float[] array = getRotations(blockPos);
        return fixRotation(array[0], array[1], n, n2);
    }

    public static float[] getRotations(final BlockPos blockPos) {
        final double n = blockPos.getX() + 0.45 - mc.thePlayer.posX;
        final double n2 = blockPos.getY() + 0.45 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        final double n3 = blockPos.getZ() + 0.45 - mc.thePlayer.posZ;
        return new float[] { mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float((float)(Math.atan2(n3, n) * 57.295780181884766) - 90.0f - mc.thePlayer.rotationYaw), clampTo90(mc.thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float((float)(-(Math.atan2(n2, MathHelper.sqrt_double(n * n + n3 * n3)) * 57.295780181884766)) - mc.thePlayer.rotationPitch)) };
    }

    public static float interpolateValue(float tickDelta, float old, float newFloat) {
        return old + (newFloat - old) * tickDelta;
    }

    public static float @NotNull [] getRotations(Entity entity, final float n, final float n2) {
        final float[] array = getRotations(entity);
        if (array == null) {
            return new float[] { mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch };
        }
        return fixRotation(array[0], array[1], n, n2);
    }

    public static double distanceFromYaw(final Entity entity, final boolean b) {
        return Math.abs(MathHelper.wrapAngleTo180_double(i(entity.posX, entity.posZ) - ((b && PreMotionEvent.setRenderYaw()) ? RotationUtils.renderYaw : mc.thePlayer.rotationYaw)));
    }

    public static float i(final double n, final double n2) {
        return (float)(Math.atan2(n - mc.thePlayer.posX, n2 - mc.thePlayer.posZ) * 57.295780181884766 * -1.0);
    }

    public static boolean notInRange(final BlockPos blockPos, final double n) {
        AxisAlignedBB box = BlockUtils.getCollisionBoundingBox(blockPos);
        keystrokesmod.script.classes.Vec3 eyePos = Utils.getEyePos();
        if (box == null) {
            return eyePos.distanceTo(keystrokesmod.script.classes.Vec3.convert(blockPos)) > n;
        } else {
            return eyePos.distanceTo(getNearestPoint(box, eyePos)) > n;
        }
    }

    public static float[] getRotations(final Entity entity) {
        if (entity == null) {
            return null;
        }
        final double n = entity.posX - mc.thePlayer.posX;
        final double n2 = entity.posZ - mc.thePlayer.posZ;
        double n3;
        if (entity instanceof EntityLivingBase) {
            final EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
            n3 = entityLivingBase.posY + entityLivingBase.getEyeHeight() * 0.9 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        } else {
            n3 = (entity.getEntityBoundingBox().minY + entity.getEntityBoundingBox().maxY) / 2.0 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        }
        return new float[]{mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float((float) (Math.atan2(n2, n) * 57.295780181884766) - 90.0f - mc.thePlayer.rotationYaw), clampTo90(mc.thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float((float) (-(Math.atan2(n3, MathHelper.sqrt_double(n * n + n2 * n2)) * 57.295780181884766)) - mc.thePlayer.rotationPitch) + 3.0f)};
    }

    public static float[] getRotationsPredicated(final Entity entity, final int ticks) {
        if (entity == null) {
            return null;
        }
        if (ticks == 0) {
            return getRotations(entity);
        }
        double posX = entity.posX;
        final double posY = entity.posY;
        double posZ = entity.posZ;
        final double n2 = posX - entity.lastTickPosX;
        final double n3 = posZ - entity.lastTickPosZ;
        for (int i = 0; i < ticks; ++i) {
            posX += n2;
            posZ += n3;
        }
        final double n4 = posX - mc.thePlayer.posX;
        double n5;
        if (entity instanceof EntityLivingBase) {
            n5 = posY + entity.getEyeHeight() * 0.9 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        }
        else {
            n5 = (entity.getEntityBoundingBox().minY + entity.getEntityBoundingBox().maxY) / 2.0 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        }
        final double n6 = posZ - mc.thePlayer.posZ;
        return new float[] { mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float((float)(Math.atan2(n6, n4) * 57.295780181884766) - 90.0f - mc.thePlayer.rotationYaw), clampTo90(mc.thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float((float)(-(Math.atan2(n5, MathHelper.sqrt_double(n4 * n4 + n6 * n6)) * 57.295780181884766)) - mc.thePlayer.rotationPitch) + 3.0f) };
    }

    public static float clampTo90(final float n) {
        return MathHelper.clamp_float(n, -90.0f, 90.0f);
    }

    public static float[] fixRotation(float n, float n2, final float n3, final float n4) {
        float n5 = n - n3;
        final float abs = Math.abs(n5);
        final float n7 = n2 - n4;
        final float n8 = mc.gameSettings.mouseSensitivity * 0.6f + 0.2f;
        final double n9 = n8 * n8 * n8 * 1.2;
        final float n10 = (float) (Math.round((double) n5 / n9) * n9);
        final float n11 = (float) (Math.round((double) n7 / n9) * n9);
        n = n3 + n10;
        n2 = n4 + n11;
        if (abs >= 1.0f) {
            final int n12 = (int) RotationHandler.randomYawFactor.getInput();
            if (n12 != 0) {
                final int n13 = n12 * 100 + Utils.randomizeInt(-30, 30);
                n += Utils.randomizeInt(-n13, n13) / 100.0;
            }
        } else if (abs <= 0.04) {
            n += ((abs > 0.0f) ? 0.01 : -0.01);
        }
        return new float[]{n, clampTo90(n2)};
    }

    public static float angle(final double n, final double n2) {
        return (float) (Math.atan2(n - mc.thePlayer.posX, n2 - mc.thePlayer.posZ) * 57.295780181884766 * -1.0);
    }

    public static MovingObjectPosition rayCast(final double distance, final float yaw, final float pitch) {
        final Vec3 getPositionEyes = mc.thePlayer.getPositionEyes(1.0f);
        final float n4 = -yaw * 0.017453292f;
        final float n5 = -pitch * 0.017453292f;
        final float cos = MathHelper.cos(n4 - 3.1415927f);
        final float sin = MathHelper.sin(n4 - 3.1415927f);
        final float n6 = -MathHelper.cos(n5);
        final Vec3 vec3 = new Vec3(sin * n6, MathHelper.sin(n5), cos * n6);
        return mc.theWorld.rayTraceBlocks(getPositionEyes, getPositionEyes.addVector(vec3.xCoord * distance, vec3.yCoord * distance, vec3.zCoord * distance), false, false, false);
    }
    public static MovingObjectPosition rayTraceCustom(double blockReachDistance, float yaw, float pitch) {
        final Vec3 vec3 = mc.thePlayer.getPositionEyes(1.0F);
        final Vec3 vec31 = getVectorForRotation(pitch, yaw);
        final Vec3 vec32 = vec3.addVector(vec31.xCoord * blockReachDistance, vec31.yCoord * blockReachDistance, vec31.zCoord * blockReachDistance);
        return mc.theWorld.rayTraceBlocks(vec3, vec32, false, false, true);
    }
    public static boolean rayCastIgnoreWall(float yaw, float pitch, @NotNull EntityLivingBase target) {
        yaw = toPositive(yaw);

        AxisAlignedBB targetBox = target.getEntityBoundingBox();

        float minYaw = Float.MAX_VALUE;
        float maxYaw = Float.MIN_VALUE;
        float minPitch = Float.MAX_VALUE;
        float maxPitch = Float.MIN_VALUE;

        for (double x : new double[]{targetBox.minX, targetBox.maxX}) {
            for (double y : new double[]{targetBox.minY, targetBox.maxY}) {
                for (double z : new double[]{targetBox.minZ, targetBox.maxZ}) {
                    final keystrokesmod.script.classes.Vec3 hitPos = new keystrokesmod.script.classes.Vec3(x, y, z);

                    final float yaw1 = toPositive(PlayerRotation.getYaw(hitPos));
                    final float pitch1 = PlayerRotation.getPitch(hitPos);

                    if (minYaw > yaw1) minYaw = yaw1;
                    if (maxYaw < yaw1) maxYaw = yaw1;
                    if (minPitch > pitch1) minPitch = pitch1;
                    if (maxPitch < pitch1) maxPitch = pitch1;
                }
            }
        }

        return yaw >= minYaw && yaw <= maxYaw && pitch >= minPitch && pitch <= maxPitch;
    }

    public static float toPositive(float yaw) {
        if (yaw > 0) return yaw;

        return 360 + (yaw % 360);
    }

    @Contract("_, _ -> new")
    public static @NotNull Vec3 getVectorForRotation(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * 0.017453292F - 3.1415927F);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - 3.1415927F);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3(f1 * f2, f3, f * f2);
    }

    @Contract("_, _ -> new")
    public static @NotNull keystrokesmod.script.classes.Vec3 getNearestPoint(@NotNull AxisAlignedBB from, @NotNull keystrokesmod.script.classes.Vec3 to) {
        double pointX, pointY, pointZ;
        if (to.x() >= from.maxX) {
            pointX = from.maxX;
        } else pointX = Math.max(to.x(), from.minX);
        if (to.y() >= from.maxY) {
            pointY = from.maxY;
        } else pointY = Math.max(to.y(), from.minY);
        if (to.z() >= from.maxZ) {
            pointZ = from.maxZ;
        } else pointZ = Math.max(to.z(), from.minZ);

        return new keystrokesmod.script.classes.Vec3(pointX, pointY, pointZ);
    }

    @Contract("_, _ -> new")
    public static @NotNull keystrokesmod.script.classes.Vec3 getFarthestPoint(@NotNull AxisAlignedBB from, @NotNull keystrokesmod.script.classes.Vec3 to) {
        double pointX, pointY, pointZ;
        if (to.x() < from.maxX) {
            pointX = from.maxX;
        } else pointX = Math.min(to.x(), from.minX);
        if (to.y() < from.maxY) {
            pointY = from.maxY;
        } else pointY = Math.min(to.y(), from.minY);
        if (to.z() < from.maxZ) {
            pointZ = from.maxZ;
        } else pointZ = Math.min(to.z(), from.minZ);

        return new keystrokesmod.script.classes.Vec3(pointX, pointY, pointZ);
    }

    public static float normalize(float yaw) {
        while (yaw > 180f) {
            yaw -= 360f;
        }
        while (yaw < -180f) {
            yaw += 360f;
        }

        return yaw;
    }

    public static boolean isMouseOver(final float yaw, final float pitch, final Entity target, final float range) {
        final float partialTicks = Utils.getTimer().renderPartialTicks;
        final Entity entity = mc.thePlayer;
        MovingObjectPosition objectMouseOver;
        Entity mcPointedEntity = null;

        if (entity != null && mc.theWorld != null) {

            mc.mcProfiler.startSection("pick");
            final double d0 = mc.playerController.getBlockReachDistance();
            objectMouseOver = entity.rayTrace(d0, partialTicks);
            double d1 = d0;
            final Vec3 vec3 = entity.getPositionEyes(partialTicks);
            final boolean flag = d0 > (double) range;

            if (objectMouseOver != null) {
                d1 = objectMouseOver.hitVec.distanceTo(vec3);
            }

            final Vec3 vec31 = getVectorForRotation(pitch, yaw);
            final Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
            Entity pointedEntity = null;
            Vec3 vec33 = null;
            final float f = 1.0F;
            final List<Entity> list = mc.theWorld.getEntitiesInAABBexcluding(entity, entity.getEntityBoundingBox().addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0).expand(f, f, f), Predicates.and(EntitySelectors.NOT_SPECTATING, Entity::canBeCollidedWith));
            double d2 = d1;

            for (final Entity entity1 : list) {
                final float f1 = entity1.getCollisionBorderSize();
                final AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand(f1, f1, f1);
                final MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);

                if (axisalignedbb.isVecInside(vec3)) {
                    if (d2 >= 0.0D) {
                        pointedEntity = entity1;
                        vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                        d2 = 0.0D;
                    }
                } else if (movingobjectposition != null) {
                    final double d3 = vec3.distanceTo(movingobjectposition.hitVec);

                    if (d3 < d2 || d2 == 0.0D) {
                        pointedEntity = entity1;
                        vec33 = movingobjectposition.hitVec;
                        d2 = d3;
                    }
                }
            }

            if (pointedEntity != null && flag && vec3.distanceTo(vec33) > (double) range) {
                pointedEntity = null;
                objectMouseOver = new MovingObjectPosition(MovingObjectPosition.MovingObjectType.MISS, vec33, null, new BlockPos(vec33));
            }

            if (pointedEntity != null && (d2 < d1 || objectMouseOver == null)) {
                if (pointedEntity instanceof EntityLivingBase || pointedEntity instanceof EntityItemFrame) {
                    mcPointedEntity = pointedEntity;
                }
            }

            mc.mcProfiler.endSection();

            return mcPointedEntity == target;
        }

        return false;
    }

    public static @NotNull Optional<Triple<BlockPos, EnumFacing, keystrokesmod.script.classes.Vec3>> getPlaceSide(@NotNull BlockPos blockPos) {
        final List<BlockPos> possible = Arrays.asList(
                blockPos.down(), blockPos.east(), blockPos.west(),
                blockPos.north(), blockPos.south(), blockPos.up()
        );

        for (BlockPos pos : possible) {
            if (BlockUtils.getBlockState(pos).getBlock().isFullBlock()) {
                EnumFacing facing;
                keystrokesmod.script.classes.Vec3 hitPos;
                if (pos.getY() < blockPos.getY()) {
                    facing = EnumFacing.UP;
                    hitPos = new keystrokesmod.script.classes.Vec3(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
                } else if (pos.getX() > blockPos.getX()) {
                    facing = EnumFacing.WEST;
                    hitPos = new keystrokesmod.script.classes.Vec3(pos.getX(), pos.getY() + 0.5, pos.getZ() + 0.5);
                } else if (pos.getX() < blockPos.getX()) {
                    facing = EnumFacing.EAST;
                    hitPos = new keystrokesmod.script.classes.Vec3(pos.getX() + 1, pos.getY() + 0.5, pos.getZ() + 0.5);
                } else if (pos.getZ() < blockPos.getZ()) {
                    facing = EnumFacing.SOUTH;
                    hitPos = new keystrokesmod.script.classes.Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 1);
                } else if (pos.getZ() > blockPos.getZ()) {
                    facing = EnumFacing.NORTH;
                    hitPos = new keystrokesmod.script.classes.Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ());
                } else {
                    facing = EnumFacing.DOWN;
                    hitPos = new keystrokesmod.script.classes.Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                }

                return Optional.of(Triple.of(pos, facing, hitPos));
            }
        }
        return Optional.empty();
    }
}
