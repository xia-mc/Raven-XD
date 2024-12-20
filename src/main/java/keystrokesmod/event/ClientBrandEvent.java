package keystrokesmod.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraftforge.fml.common.eventhandler.Event;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class ClientBrandEvent extends Event {
    private String brand;
}
