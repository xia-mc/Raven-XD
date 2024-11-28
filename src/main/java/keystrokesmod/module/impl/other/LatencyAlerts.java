package keystrokesmod.module.impl.other;

import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import lombok.Getter;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LatencyAlerts extends Module {
    @Getter
    private static boolean freeze = false;
    private final SliderSetting minLatency;
    private long lastPacket;

    public LatencyAlerts() {
        super("Latency Alerts", category.other);
        this.registerSetting(new DescriptionSetting("Detects packet loss."));
        this.registerSetting(minLatency = new SliderSetting("Min latency", 500, 50, 5000, 50, "ms"));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacketReceive(ReceivePacketEvent e) {
        if (isFreeze()) {
            Utils.sendMessage("&7Packet loss detected: §c" + (System.currentTimeMillis() - lastPacket) + "&7ms");
            freeze = false;
        }
        lastPacket = System.currentTimeMillis();
    }

    public void onUpdate() {
        if (!Utils.nullCheck() || mc.thePlayer.ticksExisted < 20) {
            freeze = false;
            lastPacket = System.currentTimeMillis();
        }

        if (System.currentTimeMillis() - lastPacket >= minLatency.getInput()) {
            freeze = true;
            mc.ingameGUI.setRecordPlaying(
                    "§7Packet loss has exceeded: §c" + (System.currentTimeMillis() - lastPacket) + "§7ms",
                    false);
        }
    }

    public void onDisable() {
        lastPacket = 0;
        freeze = false;
    }

    public void onEnable() {
        lastPacket = System.currentTimeMillis();
    }
}
