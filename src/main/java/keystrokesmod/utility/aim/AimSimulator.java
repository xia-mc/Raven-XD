package keystrokesmod.utility.aim;

import akka.japi.Pair;
import keystrokesmod.module.impl.other.anticheats.utils.phys.Vec2;
import keystrokesmod.module.impl.other.anticheats.utils.world.PlayerRotation;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.RotationUtils;
import keystrokesmod.utility.Utils;
import lombok.Getter;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
import java.util.List;

import static keystrokesmod.Raven.mc;

public class AimSimulator {
    private double xRandom = 0;
    private double yRandom = 0;
    private double zRandom = 0;
    private long lastNoiseRandom = System.currentTimeMillis();
    private double lastNoiseDeltaX = 0;
    private double lastNoiseDeltaY = 0;
    private double lastNoiseDeltaZ = 0;
    private final List<AxisAlignedBB> boxHistory = new ArrayList<>(101);

    private boolean nearest = false;
    private double nearestAcc = 0.8;

    private boolean lazy = false;
    private double lazyAcc = 0.95;

    private boolean noise = false;
    private Pair<Float, Float> noiseRandom = new Pair<>(0.35F, 0.5F);
    private double noiseSpeed = 1;
    private long noiseDelay = 100;

    private boolean delay = false;
    private int delayTicks = 1;

    @Getter
    private Vec3 hitPos = Vec3.ZERO;

    public void setNearest(boolean value, @Range(from = 0, to = 1) double acc) {
        this.nearestAcc = acc;
        this.nearest = value;
    }

    public void setLazy(boolean value, @Range(from = 0, to = 1) double acc) {
        this.lazyAcc = acc;
        this.lazy = value;
    }

    public void setNoise(boolean value, Pair<Float, Float> noiseRandom, double noiseSpeed, long noiseDelay) {
        this.noiseRandom = noiseRandom;
        this.noiseSpeed = noiseSpeed / 100;
        this.noiseDelay = noiseDelay;
        this.noise = value;
    }

    public void setDelay(boolean value, int delayTicks) {
        this.delayTicks = delayTicks;
        this.delay = value;
    }

    public @NotNull Pair<Float, Float> getRotation(@NotNull EntityLivingBase target) {
        AxisAlignedBB targetBox = target.getEntityBoundingBox();
        if (boxHistory.size() >= 101) {
            boxHistory.remove(boxHistory.size() - 1);
        }
        while (boxHistory.size() < 101) {
            boxHistory.add(0, targetBox);
        }

        float yaw, pitch;

        final double yDiff = target.posY - mc.thePlayer.posY;
        Vec3 targetPosition;

        AxisAlignedBB aimBox = delay ? boxHistory.get(delayTicks) : targetBox;
        if (nearest) {
            targetPosition = RotationUtils.getNearestPoint(aimBox, Utils.getEyePos());
            if (MoveUtil.isMoving() || MoveUtil.isMoving(target))
                targetPosition = targetPosition.add(Utils.randomizeDouble(nearestAcc - 1, 1 - nearestAcc) * 0.4, Utils.randomizeDouble(nearestAcc - 1, 1 - nearestAcc) * 0.4, Utils.randomizeDouble(nearestAcc - 1, 1 - nearestAcc) * 0.4);
        } else {
            targetPosition = new Vec3((aimBox.maxX + aimBox.minX) / 2, aimBox.minY + target.getEyeHeight() - 0.15, (aimBox.maxZ + aimBox.minZ) / 2);
        }


        if (yDiff >= 0 && lazy) {
            if (targetPosition.y() - yDiff > target.posY) {
                targetPosition = new Vec3(targetPosition.x(), targetPosition.y() - yDiff, targetPosition.z());
            } else {
                targetPosition = new Vec3(target.posX, target.posY + 0.2, target.posZ);
            }
            if (!target.onGround && (MoveUtil.isMoving() || MoveUtil.isMoving(target)))
                targetPosition.y += Utils.randomizeDouble(lazyAcc - 1, 1 - lazyAcc) * 0.4;
        }

        if (noise) {
            if (System.currentTimeMillis() - lastNoiseRandom >= noiseDelay) {
                xRandom = random(noiseRandom.first());
                yRandom = random(noiseRandom.second());
                zRandom = random(noiseRandom.first());
                lastNoiseRandom = System.currentTimeMillis();
            }

            lastNoiseDeltaX = rotMove(xRandom, lastNoiseDeltaX, noiseSpeed);
            lastNoiseDeltaY = rotMove(yRandom, lastNoiseDeltaY, noiseSpeed);
            lastNoiseDeltaZ = rotMove(zRandom, lastNoiseDeltaZ, noiseSpeed);

            targetPosition.x = normal(targetBox.maxX, targetBox.minX, targetPosition.x + lastNoiseDeltaX);
            targetPosition.y = normal(targetBox.maxY, targetBox.minY, targetPosition.y + lastNoiseDeltaY);
            targetPosition.z = normal(targetBox.maxZ, targetBox.minZ, targetPosition.z + lastNoiseDeltaZ);
        }

        yaw = PlayerRotation.getYaw(targetPosition);
        pitch = PlayerRotation.getPitch(targetPosition);
        hitPos = targetPosition;

        return new Pair<>(yaw, pitch);
    }

    private static float random(double multiple) {
        return (float) ((Math.random() - 0.5) * 2 * multiple);
    }

    private static double normal(double max, double min, double current) {
        if (current >= max) return max;
        return Math.max(current, min);
    }

    public static float rotMove(double target, double current, double diff) {
        return rotMoveNoRandom((float) target, (float) current, (float) diff);
    }

    public static float rotMoveNoRandom(float target, float current, float diff) {
        float delta;
        if (target > current) {
            float dist1 = target - current;
            float dist2 = current + 360 - target;
            if (dist1 > dist2) {  // 另一边移动更近
                delta = -current - 360 + target;
            } else {
                delta = dist1;
            }
        } else if (target < current) {
            float dist1 = current - target;
            float dist2 = target + 360 - current;
            if (dist1 > dist2) {  // 另一边移动更近
                delta = current + 360 + target;
            } else {
                delta = -dist1;
            }
        } else {
            return current;
        }

        delta = RotationUtils.normalize(delta);

        if (Math.abs(delta) < 0.1 * Math.random() + 0.1) {
            return current;
        } else if (Math.abs(delta) <= diff) {
            return current + delta;
        } else {
            if (delta < 0) {
                return current - diff;
            } else if (delta > 0) {
                return current + diff;
            } else {
                return current;
            }
        }
    }

    public static boolean yawEquals(float yaw1, float yaw2) {
        return Math.abs(RotationUtils.normalize(yaw1) - RotationUtils.normalize(yaw2)) < 0.1;
    }

    public static boolean equals(@NotNull Vec2 rot1, @NotNull Vec2 rot2) {
        return yawEquals(rot1.x, rot2.x) && Math.abs(rot1.y - rot2.y) < 0.1;
    }
}
