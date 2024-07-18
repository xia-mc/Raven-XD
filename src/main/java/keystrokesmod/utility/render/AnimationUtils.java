package keystrokesmod.utility.render;

public class AnimationUtils {
    private double value;
    private long lastMS;

    public AnimationUtils(final double value) {
        this.value = value;
        this.lastMS = System.currentTimeMillis();
    }

    public static double calculateCompensation(final double target, double current, final double speed, long delta) {
        final double diff = current - target;
        double add = (delta * (speed / 50));

        if (diff > speed) {
            if (current - add > target) {
                current -= add;
            } else {
                current = target;
            }
        } else if (diff < -speed) {
            if (current + add < target) {
                current += add;
            } else {
                current = target;
            }
        } else {
            current = target;
        }

        return current;
    }

    public void setAnimation(final double value, double speed) {
        final long currentMS = System.currentTimeMillis();
        final long delta = currentMS - this.lastMS;
        this.lastMS = currentMS;
        double deltaValue = 0.0;

        if (speed > 28) {
            speed = 28;
        }

        if (speed != 0.0) {
            deltaValue = Math.abs(value - this.value) * 0.35f / (10.0 / speed);
        }

        this.value = calculateCompensation(value, this.value, deltaValue, delta);
    }

    public double getValue() {
        return value;
    }
}