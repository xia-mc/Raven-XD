package keystrokesmod.module.impl.player;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.player.fakelag.Dynamic;
import keystrokesmod.module.impl.player.fakelag.Latency;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeValue;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.backtrack.TimedPacket;
import net.minecraft.network.Packet;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FakeLag extends Module {
    private final ModeValue mode;
    private final SliderSetting delay;
    public static ButtonSetting drawRealPosition = null;
    public static ButtonSetting debug = null;
    public static ButtonSetting dynamicIgnoreTeammates = null;
    public static ButtonSetting dynamicStopOnHurt = null;
    public static SliderSetting dynamicStopOnHurtTime = null;
    public static SliderSetting dynamicStartRange = null;
    public static SliderSetting dynamicStopRange = null;
    public static SliderSetting dynamicMaxTargetRange = null;
    public static long lastDisableTime = -1;
    public static boolean lastHurt = false;
    public static long lastStartBlinkTime = -1;
    @Nullable
    public static Queue<TimedPacket> packetQueue = new ConcurrentLinkedQueue<>();
    public static double delayInt;
    public static Vec3 vec3 = null;

    public FakeLag() {
        super("Fake Lag", category.player);
        this.registerSetting(mode = new ModeValue("Mode", this).add(new Latency("Latency")).add(new Dynamic("Dynamic")).setDefaultValue("Latency"));
       final ModeOnly mode1 = new ModeOnly(mode, 1);
        this.registerSetting(delay = new SliderSetting("Delay", 200, 25, 1000, 5, "ms"));
        this.registerSetting(drawRealPosition = new ButtonSetting("Draw real position", true));
        this.registerSetting(debug = new ButtonSetting("Debug", false));
        this.registerSetting(dynamicIgnoreTeammates = new ButtonSetting("Dynamic Ignore teammates", true, mode1));
        this.registerSetting(dynamicStopOnHurt = new ButtonSetting("Dynamic Stop on hurt", true, mode1));
        this.registerSetting(dynamicStopOnHurtTime = new SliderSetting("Dynamic Stop on hurt time", 500, 0, 1000, 5, "ms", mode1));
        this.registerSetting(dynamicStartRange = new SliderSetting("Dynamic Start range", 6.0, 3.0, 10.0, 0.1, "blocks", mode1));
        this.registerSetting(dynamicStopRange = new SliderSetting("Dynamic Stop range", 3.5, 1.0, 6.0, 0.1, "blocks", mode1));
        this.registerSetting(dynamicMaxTargetRange = new SliderSetting("Dynamic Max target range", 15.0, 6.0, 20.0, 0.5, "blocks", mode1));
    }

    public String getInfo() {
        return mode.getSubModeValues().get((int) mode.getInput()).getName();
    }
    @Override
    public void guiUpdate() {
        delayInt = delay.getInput();
        Utils.correctValue(dynamicStopRange, dynamicStartRange);
        Utils.correctValue(dynamicStartRange, dynamicMaxTargetRange);
    }

    public void onEnable() {
        mode.enable(mode.getSubModeValues().get((int) mode.getInput()));
    }

    public void onDisable() {
        sendPacket(true);
    }
    public static void sendPacket(boolean delay) {
        try {
            while (!packetQueue.isEmpty()) {
                if (!delay || packetQueue.element().getCold().getCum((long) delayInt)) {
                    Packet<?> packet = packetQueue.remove().getPacket();
                    if (packet == null) continue;

                    PacketUtils.getPos(packet).ifPresent(pos -> vec3 = pos);
                    PacketUtils.sendPacketNoEvent(packet);
                } else {
                    break;
                }
            }
        } catch (Exception ignored) {
        }
    }
}