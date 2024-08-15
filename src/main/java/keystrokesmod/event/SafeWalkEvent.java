package keystrokesmod.event;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.fml.common.eventhandler.Event;

@Getter
@Setter
@AllArgsConstructor
public class SafeWalkEvent extends Event {
    private boolean safeWalk;
}
