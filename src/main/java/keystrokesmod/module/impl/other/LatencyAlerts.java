package keystrokesmod.module.impl.other;

import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LatencyAlerts extends Module {
    private final DescriptionSetting description;
    private final SliderSetting interval;
    private final SliderSetting highLatency;
    private long lastPacket;
    private long lastAlert;
    public LatencyAlerts() {
        super("Latency Alerts", category.other);
        this.registerSetting(description = new DescriptionSetting("Detects packet loss."));
        this.registerSetting(interval = new SliderSetting("Alert interval", 3.0, 0.0, 5.0, 0.1, " second"));
        this.registerSetting(highLatency = new SliderSetting("High latency", 0.5, 0.1, 5.0, 0.1, " second"));
    }

    @SubscribeEvent
    public void onPacketReceive(ReceivePacketEvent e) {
        lastPacket = System.currentTimeMillis();
    }

    public void onUpdate() {
        if (mc.isSingleplayer() || mc.getCurrentServerData() == null) {
            lastPacket = 0;
            lastAlert = 0;
            return;
        }
        long currentMs = System.currentTimeMillis();
        if (currentMs - lastPacket >= highLatency.getInput() * 1000 && currentMs - lastAlert >= interval.getInput() * 1000) {
            Utils.sendMessage("&7Packet loss detected: " + "§c" + Math.abs(System.currentTimeMillis() - lastPacket) + "&7ms");
            lastAlert = System.currentTimeMillis();
        }
    }

    public void onDisable() {
        lastPacket = 0;
        lastAlert = 0;
    }

    public void onEnable() {
        lastPacket = System.currentTimeMillis();
    }
}
