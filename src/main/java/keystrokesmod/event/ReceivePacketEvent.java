package keystrokesmod.event;

import lombok.Getter;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Getter
@Cancelable
public class ReceivePacketEvent extends Event {
    private final Packet<INetHandlerPlayClient> packet;

    public ReceivePacketEvent(Packet<?> packet) {
        try {
            this.packet = (Packet<INetHandlerPlayClient>) packet;
        } catch (ClassCastException e) {
            throw new RuntimeException("Invalid packet received!");
        }
    }

}
