package keystrokesmod.utility;

import net.minecraft.util.MathHelper;

public class TimerUtil {

    public long lastMS = System.currentTimeMillis();

    public long getCurrentMS() {
        return System.nanoTime() / 1000000L;
    }

    public void reset() {
        lastMS = System.currentTimeMillis();
    }

    public boolean hasTimeElapsed(long time, boolean reset) {
        if (System.currentTimeMillis() - lastMS > time) {
            if (reset) reset();
            return true;
        }

        return false;
    }

    public boolean delay(double milliseconds) {
        return MathHelper.clamp_float(getCurrentMS() - lastMS, 0, (float) milliseconds) >= milliseconds;
    }

    public boolean hasTimeElapsed(long time) {
        return System.currentTimeMillis() - lastMS > time;
    }

    public boolean hasTimeElapsed(double time) {
        return hasTimeElapsed((long) time);
    }

    public long getTime() {
        return System.currentTimeMillis() - lastMS;
    }

    public void setTime(long time) {
        lastMS = time;
    }

}
