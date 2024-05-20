package keystrokesmod.module.impl.player;

import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FakeLag extends Module {
    private final SliderSetting packetDelay;
    private final ConcurrentHashMap<Packet, Long> delayedPackets = new ConcurrentHashMap<>();

    public FakeLag() {
        super("Fake Lag", category.player);
        this.registerSetting(packetDelay = new SliderSetting("Packet delay", 200, 25, 1000, 5, "ms"));
    }

    public String getInfo() {
        return (int) packetDelay.getInput() + "ms";
    }

    public void onEnable() {
        delayedPackets.clear();
    }

    public void onDisable() {
        sendPacket(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderTick(TickEvent.RenderTickEvent ev) {
        if (!Utils.nullCheck()) {
            sendPacket(false);
            return;
        }
        sendPacket(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSendPacket(SendPacketEvent e) {
        long receiveTime = System.currentTimeMillis();
        if (!Utils.nullCheck()) {
            sendPacket(false);
            return;
        }
        if (e.isCanceled()) {
            return;
        }
        delayedPackets.put(e.getPacket(), receiveTime);
        e.setCanceled(true);
    }

    private void sendPacket(boolean delay) {
        try {
            Iterator<Map.Entry<Packet, Long>> packets = delayedPackets.entrySet().iterator();
            while (packets.hasNext()) {
                Map.Entry<Packet, Long> entry = packets.next();
                Packet packet = entry.getKey();
                if (packet == null) {
                    continue;
                }
                long receiveTime = entry.getValue();
                long ms = System.currentTimeMillis();
                if (Utils.getDifference(ms, receiveTime) > packetDelay.getInput() || !delay) {
                    PacketUtils.sendPacketNoEvent(packet);
                    packets.remove();
                }
            }
        }
        catch (Exception e) {
        }
    }
}
