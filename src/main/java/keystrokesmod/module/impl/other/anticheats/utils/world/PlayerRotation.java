package keystrokesmod.module.impl.other.anticheats.utils.world;

import keystrokesmod.Raven;
import keystrokesmod.script.classes.Vec3;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import org.jetbrains.annotations.NotNull;

public class PlayerRotation {
    public static float getYaw(@NotNull BlockPos pos) {
        return getYaw(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
    }

    public static float getYaw(@NotNull AbstractClientPlayer from, @NotNull Vec3 pos) {
        return from.rotationYaw +
                MathHelper.wrapAngleTo180_float(
                        (float) Math.toDegrees(Math.atan2(pos.z() - from.posZ, pos.x() - from.posX)) - 90f - from.rotationYaw
                );
    }

    public static float getYaw(@NotNull Vec3 pos) {
        return getYaw(Raven.mc.thePlayer, pos);
    }

    public static float getPitch(@NotNull BlockPos pos) {
        return getPitch(new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
    }

    public static float getPitch(@NotNull AbstractClientPlayer from, @NotNull Vec3 pos) {
        double diffX = pos.x() - from.posX;
        double diffY = pos.y() - (from.posY + from.getEyeHeight());
        double diffZ = pos.z() - from.posZ;

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        return from.rotationPitch + MathHelper.wrapAngleTo180_float((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)) - from.rotationPitch);
    }

    public static float getPitch(@NotNull Vec3 pos) {
        return getPitch(Raven.mc.thePlayer, pos);
    }
}
