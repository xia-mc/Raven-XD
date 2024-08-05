package keystrokesmod.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Getter
@Setter
@Cancelable
@AllArgsConstructor
public class PreVelocityEvent extends Event {
    private int motionX;
    private int motionY;
    private int motionZ;
}
