package keystrokesmod.module.impl.other.anticheats.utils.phys;

import keystrokesmod.utility.aim.AimSimulator;
import net.minecraft.util.MathHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;


/**
 * copy from Minecraft 1.20.1 (officialMapping)
 */
public class Vec2 {
    public static final Vec2 ZERO = new Vec2(0.0f, 0.0f);
    public static final Vec2 ONE = new Vec2(1.0f, 1.0f);
    public static final Vec2 UNIT_X = new Vec2(1.0f, 0.0f);
    public static final Vec2 NEG_UNIT_X = new Vec2(-1.0f, 0.0f);
    public static final Vec2 UNIT_Y = new Vec2(0.0f, 1.0f);
    public static final Vec2 NEG_UNIT_Y = new Vec2(0.0f, -1.0f);
    public static final Vec2 MAX = new Vec2(Float.MAX_VALUE, Float.MAX_VALUE);
    public static final Vec2 MIN = new Vec2(Float.MIN_VALUE, Float.MIN_VALUE);
    public float x;
    public float y;

    public Vec2(float f, float g) {
        this.x = f;
        this.y = g;
    }

    public Vec2 scale(float f) {
        return new Vec2(this.x * f, this.y * f);
    }

    public float dot(@NotNull Vec2 vec2) {
        return this.x * vec2.x + this.y * vec2.y;
    }

    public Vec2 add(@NotNull Vec2 vec2) {
        return new Vec2(this.x + vec2.x, this.y + vec2.y);
    }

    public Vec2 add(float f) {
        return new Vec2(this.x + f, this.y + f);
    }

    public Vec2 add(float x, float y) {
        return new Vec2(this.x + x, this.y + y);
    }

    public boolean equals(@NotNull Vec2 vec2) {
        return AimSimulator.yawEquals(this.x, vec2.x) && this.y == vec2.y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vec2 vec2 = (Vec2) o;
        return this.equals(vec2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public Vec2 normalized() {
        float f = MathHelper.sqrt_float(this.x * this.x + this.y * this.y);
        return f < 1.0E-4f ? ZERO : new Vec2(this.x / f, this.y / f);
    }

    public float length() {
        return MathHelper.sqrt_float(this.x * this.x + this.y * this.y);
    }

    public float lengthSquared() {
        return this.x * this.x + this.y * this.y;
    }

    public float distanceToSqr(@NotNull Vec2 vec2) {
        float f = vec2.x - this.x;
        float g = vec2.y - this.y;
        return f * f + g * g;
    }

    public Vec2 negated() {
        return new Vec2(-this.x, -this.y);
    }
}
