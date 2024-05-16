package keystrokesmod.module.impl.player;

import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S19PacketEntityStatus;
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
        receivePacket(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderTick(TickEvent.RenderTickEvent ev) {
        if (!Utils.nullCheck()) {
            receivePacket(false);
            return;
        }
        receivePacket(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onReceivePacket(ReceivePacketEvent e) {
        long receiveTime = System.currentTimeMillis();
        if (!Utils.nullCheck()) {
            receivePacket(false);
            return;
        }
        if (e.isCanceled()) {
            return;
        }
        if (e.getPacket() instanceof S19PacketEntityStatus || e.getPacket() instanceof S02PacketChat || e.getPacket() instanceof S08PacketPlayerPosLook || e.getPacket() instanceof S04PacketEntityEquipment) {
            return;
        }
        delayedPackets.put(e.getPacket(), receiveTime);
        e.setCanceled(true);
    }

    private void receivePacket(boolean delay) {
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
                    PacketUtils.receivePacketNoEvent(packet);
                    packets.remove();
                }
            }
        }
        catch (Exception e) {
        }
    }
}
