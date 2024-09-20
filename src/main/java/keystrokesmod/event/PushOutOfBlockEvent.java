package keystrokesmod.event;

import keystrokesmod.utility.movement.Direction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@Cancelable
@AllArgsConstructor
public class PushOutOfBlockEvent extends Event {
    private @NotNull Direction direction;
    private float pushMotion;

}
