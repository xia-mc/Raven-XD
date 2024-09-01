package keystrokesmod.script.classes;

import net.minecraft.entity.Entity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Vec3 {
    public static final Vec3 ZERO = new Vec3(0, 0, 0);

    public double x, y, z;

    public Vec3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3(net.minecraft.util.@NotNull Vec3 vec3) {
        this(vec3.xCoord, vec3.yCoord, vec3.zCoord);
    }

    public Vec3(@NotNull Entity entity) {
        this(entity.posX, entity.posY, entity.posZ);
    }

    public Vec3(@NotNull BlockPos blockPos) {
        this(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
    }

    public boolean equals(Vec3 vector2) {
        if (this == vector2) {
            return true;
        }
        else return this.x == vector2.x && this.y == vector2.y && this.z == vector2.z;
    }

    public Vec3 offset(double x, double y, double z) {
        return new Vec3(this.x + x, this.y + y, this.z + z);
    }

    public static Vec3 convert(BlockPos blockPos) {
        if (blockPos == null) {
            return null;
        }
        return new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    public double distanceTo(@NotNull Vec3 vec3) {
        double deltaX = this.x - vec3.x;
        double deltaY = this.y - vec3.y;
        double deltaZ = this.z - vec3.z;
        return MathHelper.sqrt_double(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }

    public double distanceTo(@NotNull net.minecraft.util.Vec3 vec3) {
        double deltaX = this.x - vec3.xCoord;
        double deltaY = this.y - vec3.yCoord;
        double deltaZ = this.z - vec3.zCoord;
        return MathHelper.sqrt_double(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }

    public double distanceTo(@NotNull Entity entity) {
        double deltaX = this.x - entity.posX;
        double deltaY = this.y - entity.posY;
        double deltaZ = this.z - entity.posZ;
        return MathHelper.sqrt_double(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
    }

    public double distanceToSq(@NotNull Vec3 vec3) {
        double deltaX = this.x - vec3.x;
        double deltaY = this.y - vec3.y;
        double deltaZ = this.z - vec3.z;
        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
    }

    public double x() { return x; }
    public double y() { return y; }
    public double z() { return z; }

    public Vec3 add(@NotNull Vec3 vec3) {
        return add(vec3.x(), vec3.y(), vec3.z());
    }

    public Vec3 add(final double x, final double y, final double z) {
        return new Vec3(x() + x, y() + y, z() + z);
    }

    public Vec3 subtract(@NotNull Vec3 vec3) {
        return add(new Vec3(-vec3.x(), -vec3.y(), -vec3.z()));
    }
    
    public double relative(EnumFacing.@NotNull Axis axis) {
        switch (axis) {
            case X: return x();
            case Y: return y();
            case Z: return z();
        }
        return 0;
    }

    @Contract("-> new")
    public net.minecraft.util.Vec3 toVec3() {
        return new net.minecraft.util.Vec3(x, y, z);
    }

    @Override
    public String toString() {
        return "Vec3{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
