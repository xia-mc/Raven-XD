package keystrokesmod.module.impl.other.anticheats.utils.world;

import keystrokesmod.script.classes.Vec3;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlayerMove {
    public static double getXzTickSpeed(@NotNull Vec3 lastTick, @NotNull Vec3 currentTick) {
            return getXZOnlyPos(currentTick).distanceTo(getXZOnlyPos(lastTick));
    }

    public static double getXzSecSpeed(@NotNull Vec3 lastTick, @NotNull Vec3 currentTick) {
        return getXzTickSpeed(lastTick, currentTick) * 20;  // IDK what's the fucking tps
    }

    @Contract("_ -> new")
    public static @NotNull Vec3 getXZOnlyPos(@NotNull Vec3 position) {
        return new Vec3(position.x(), 0, position.z());
    }

    public static double getJumpDistance(@NotNull AbstractClientPlayer player) {
        try {
            final int x = player.getActivePotionEffects().stream()
                    .filter(effect -> effect.getEffectName().toLowerCase().contains("jump"))
                    .findAny()
                    .orElseThrow(NullPointerException::new)
                    .getAmplifier() + 1;
//            return -9.331952072919326 * x * x - 3.672263213983712 * x + 0.6261016701268645;  // chat gpt
          return 0.04837 * x + 0.5356 * x + 1.252;  // numpy
        } catch (NullPointerException e) {
            return 1.252203340253729;
        }
    }

    public static boolean isNoMove(@NotNull Vec3 motion) {
        return motion.x() == 0 && motion.z() == 0;
    }

    /**
     * Gets the players predicted jump motion the specified amount of ticks ahead
     *
     * @return predicted jump motion
     */
    public static double predictedMotion(final double motion, final int ticks) {
        if (ticks == 0) return motion;
        double predicted = motion;

        for (int i = 0; i < ticks; i++) {
            predicted = (predicted - 0.08) * 0.98F;
        }

        return predicted;
    }

    /**
     * Used to get the players speed
     */
    public static double speed(@NotNull EntityPlayer player) {
        return speed(player.motionX, player.motionZ);
    }

    public static double speed(final double motionX, final double motionZ) {
        return Math.hypot(motionX, motionZ);
    }

    @Contract("_, _, _ -> new")
    public static @NotNull Vec3 getStrafeMotion(final double speed, final double yaw, final double motionY) {
        return new Vec3(-MathHelper.sin((float) yaw) * speed, motionY, MathHelper.cos((float) yaw) * speed);
    }

    /**
     * Gets the players' movement yaw
     */
    public static double direction(float moveForward, float moveStrafing, float rotationYaw) {
        if (moveForward < 0) {
            rotationYaw += 180;
        }

        float forward = 1;

        if (moveForward < 0) {
            forward = -0.5F;
        } else if (moveForward > 0) {
            forward = 0.5F;
        }

        if (moveStrafing > 0) {
            rotationYaw -= 70 * forward;
        }

        if (moveStrafing < 0) {
            rotationYaw += 70 * forward;
        }

        return Math.toRadians(rotationYaw);
    }

    public static boolean isInvalidMotion(@NotNull Vec3 motion) {
        return Math.abs(motion.x()) >= 3.9
                || Math.abs(motion.y()) >= 3.9
                || Math.abs(motion.z()) >= 3.9;
    }

    public static double getMaxXZDiff(@NotNull Vec3 motion1, @NotNull Vec3 motion2) {
        return Math.max(Math.abs(motion1.x() - motion2.x()), Math.abs(motion1.z() - motion2.z()));
    }

    public static @NotNull List<Vec3> getPosHistoryDiff(final @NotNull List<Vec3> posHistory) {
        List<Vec3> result = new ArrayList<>(posHistory.size() - 1);

        for (int i = 0; i < posHistory.size() - 1; i++) {
            result.add(posHistory.get(i + 1).subtract(posHistory.get(i)));
        }

        return result;
    }
}
