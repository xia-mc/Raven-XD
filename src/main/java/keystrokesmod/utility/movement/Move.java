package keystrokesmod.utility.movement;

import lombok.Getter;

@Getter
public enum Move {
    FORWARD(0, 0.98f, 0f),
    FORWARD_RIGHT(45, 0.98f, -0.98f),
    RIGHT(90, 0f, -0.98f),
    BACKWARD_RIGHT(135, -0.98f, -0.98f),
    BACKWARD(180, -0.98f, 0f),
    BACKWARD_LEFT(225, -0.98f, 0.98f),
    LEFT(270, 0f, 0.98f),
    FORWARD_LEFT(315, 0.98f, 0.98f);

    private final float deltaYaw;
    private final float forward;
    private final float strafing;

    Move(float deltaYaw, float forward, float strafing) {
        this.deltaYaw = deltaYaw;
        this.forward = forward;
        this.strafing = strafing;
    }

    public static Move fromMovement(float forward, float strafing) {
        if (forward > 0)
            if (strafing > 0)
                return FORWARD_LEFT;
            else if (strafing < 0)
                return FORWARD_RIGHT;
            else
                return FORWARD;
        else if (forward < 0)
            if (strafing > 0)
                return BACKWARD_LEFT;
            else if (strafing < 0)
                return BACKWARD_RIGHT;
            else
                return BACKWARD;
        else
            if (strafing > 0)
                return LEFT;
            else if (strafing < 0)
                return RIGHT;
            else
                return FORWARD;
    }

    /**
     * 从实际转头与视觉视角的差值寻找最接近的移动
     * @param yaw >=0, <360.
     * @return 最接近的在实际视角的移动
     */
    public static Move fromDeltaYaw(final float yaw) {
        Move bestMove = FORWARD;
        float bestDeltaYaw = Math.abs(yaw - bestMove.getDeltaYaw());

        for (Move move : values()) {
            if (move != bestMove) {
                float deltaYaw = Math.abs(yaw - move.getDeltaYaw());
                if (deltaYaw < bestDeltaYaw) {
                    bestMove = move;
                    bestDeltaYaw = deltaYaw;
                }
            }
        }

        return bestMove;
    }

}