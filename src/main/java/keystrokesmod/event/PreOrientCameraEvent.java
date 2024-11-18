package keystrokesmod.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraftforge.fml.common.eventhandler.Event;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@Data
public class PreOrientCameraEvent extends Event {
    private float smooth;
}
