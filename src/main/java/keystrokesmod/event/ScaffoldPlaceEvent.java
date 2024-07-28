package keystrokesmod.event;

import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class ScaffoldPlaceEvent extends Event {
    private MovingObjectPosition hitResult;
    private boolean extra;

    public ScaffoldPlaceEvent(MovingObjectPosition hitResult, boolean extra) {
        this.hitResult = hitResult;
        this.extra = extra;
    }

    public MovingObjectPosition getHitResult() {
        return hitResult;
    }

    public void setHitResult(MovingObjectPosition hitResult) {
        this.hitResult = hitResult;
    }

    public boolean isExtra() {
        return extra;
    }

    public void setExtra(boolean extra) {
        this.extra = extra;
    }
}
