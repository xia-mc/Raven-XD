package keystrokesmod.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Setter
@Getter
@Cancelable
@AllArgsConstructor
public class SendPacketEvent extends Event {
    private Packet<?> packet;

    public Packet getPacket() {
        return packet;
    }

}