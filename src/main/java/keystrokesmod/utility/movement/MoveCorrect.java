package keystrokesmod.utility.movement;

import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.MoveUtil;
import lombok.Setter;
import net.minecraft.util.MathHelper;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Range;

import static keystrokesmod.Raven.mc;

public class MoveCorrect {
    public static final double MID_POS = 0.99999999999998;
    @Setter
    private double moveStep;
    private final Mode mode;

    public MoveCorrect(final double moveStep, Mode mode) {
        this.moveStep = moveStep;
        this.mode = mode;
    }

    @Contract(pure = true)
    public boolean isDoneX() {
        return isDoneX(MID_POS);
    }

    @Contract(pure = true)
    public boolean isDoneZ() {
        return isDoneZ(MID_POS);
    }

    public boolean moveX(boolean stopMotion) {
        return moveX(MID_POS, stopMotion);
    }

    public boolean moveZ(boolean stopMotion) {
        return moveZ(MID_POS, stopMotion);
    }

    @Contract(pure = true)
    public boolean isDoneX(double delta) {
        return mc.thePlayer.posX == Math.floor(mc.thePlayer.posX) + delta;
    }


    @Contract(pure = true)
    public boolean isDoneZ(double delta) {
        return mc.thePlayer.posZ == Math.floor(mc.thePlayer.posZ) + delta;
    }

    public boolean moveX(@Range(from = -1, to = 1) double pos, boolean stopMotion) {
        if (stopMotion)
            mc.thePlayer.motionX = 0;

        final double targetPos = (pos > 0 ? MathHelper.floor_double(mc.thePlayer.posX) : Math.round(mc.thePlayer.posX)) + pos;
        if (mc.thePlayer.posX != targetPos) {
            if (targetPos > mc.thePlayer.posX) {
                doMove(Math.min(mc.thePlayer.posX + moveStep, targetPos), mc.thePlayer.posZ);
            } else {
                doMove(Math.max(mc.thePlayer.posX - moveStep, targetPos), mc.thePlayer.posZ);
            }
        }
        return isDoneX(pos);
    }

    public boolean moveZ(@Range(from = -1, to = 1) double pos, boolean stopMotion) {
        if (stopMotion)
            mc.thePlayer.motionZ = 0;

        final double targetPos = (pos > 0 ? MathHelper.floor_double(mc.thePlayer.posZ) : Math.round(mc.thePlayer.posZ)) + pos;
        if (mc.thePlayer.posZ != targetPos) {
            if (targetPos > mc.thePlayer.posZ) {
                doMove(mc.thePlayer.posX, Math.min(mc.thePlayer.posZ + moveStep, targetPos));
            } else {
                doMove(mc.thePlayer.posX, Math.max(mc.thePlayer.posZ - moveStep, targetPos));
            }
        }
        return isDoneZ(pos);
    }

    private void doMove(double posX, double posZ) {
        switch (mode) {
            case MOTION:
                if (posX != mc.thePlayer.posX)
                    mc.thePlayer.motionX = posX - mc.thePlayer.posX;
                if (posZ != mc.thePlayer.posZ)
                    mc.thePlayer.motionZ = posZ - mc.thePlayer.posZ;
                break;
            case POSITION:
                if (BlockUtils.insideBlock(mc.thePlayer.getEntityBoundingBox().offset(posX - mc.thePlayer.posX, 0, posZ - mc.thePlayer.posZ))) {
                    break;
                }
                mc.thePlayer.setPosition(posX, mc.thePlayer.posY, posZ);
                break;
        }
    }

    public enum Mode {
        POSITION,
        MOTION
    }
}
