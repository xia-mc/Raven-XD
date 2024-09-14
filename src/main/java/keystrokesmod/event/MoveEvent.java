package keystrokesmod.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Setter
@Getter
@Cancelable
@AllArgsConstructor
public class MoveEvent extends Event {
    private double x;
    private double y;
    private double z;
}
