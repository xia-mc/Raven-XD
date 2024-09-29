package keystrokesmod.event;

import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.fml.common.eventhandler.Event;

@Setter
@Getter
public class MoveInputEvent extends Event {
    private float forward, strafe;
    private boolean jump, sneak;
    private double sneakSlowDownMultiplier;

    public MoveInputEvent(float forward, float strafe, boolean jump, boolean sneak, double sneakSlowDownMultiplier) {
        this.forward = forward;
        this.strafe = strafe;
        this.jump = jump;
        this.sneak = sneak;
        this.sneakSlowDownMultiplier = sneakSlowDownMultiplier;
    }

    @Override
    public void setCanceled(boolean cancel) {
        if (cancel) {
            setForward(0);
            setStrafe(0);
            setJump(false);
            setSneak(false);
        }
    }
}