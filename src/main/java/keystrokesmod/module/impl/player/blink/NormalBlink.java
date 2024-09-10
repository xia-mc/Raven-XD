package keystrokesmod.module.impl.player.blink;

import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.player.Blink;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.render.RenderUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NormalBlink extends SubMode<Module> {
    private final ButtonSetting pulse;
    private final SliderSetting pulseDelay;
    private final ButtonSetting initialPosition;
    private final ButtonSetting overlay;

    public final List<Packet<?>> blinkedPackets = new ArrayList<>();
    private long startTime = -1;
    private Vec3 pos;

    public NormalBlink(String name, @NotNull Module parent) {
        super(name, parent);
        this.registerSetting(pulse = new ButtonSetting("Pulse", false));
        this.registerSetting(pulseDelay = new SliderSetting("Pulse delay", 1000, 0, 10000, 100, pulse::isToggled));
        this.registerSetting(initialPosition = new ButtonSetting("Show initial position", true));
        this.registerSetting(overlay = new ButtonSetting("Overlay", false));
    }

    @Override
    public void onEnable() {
        start();
    }

    private void start() {
        blinkedPackets.clear();
        pos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        startTime = System.currentTimeMillis();
    }

    public void onDisable() {
        reset();
    }

    private void reset() {
        synchronized (blinkedPackets) {
            for (Packet<?> packet : blinkedPackets) {
                PacketUtils.sendPacketNoEvent(packet);
            }
        }
        blinkedPackets.clear();
        pos = null;
    }

    @Override
    public String getInfo() {
        return String.valueOf(blinkedPackets.size());
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event) {
        if (!overlay.isToggled() || event.phase != TickEvent.Phase.END || !Utils.nullCheck()) {
            return;
        }

        RenderUtils.drawText("blinking: " + blinkedPackets.size());
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent e) {
        if (!Utils.nullCheck()) {
            this.disable();
            return;
        }
        Packet<?> packet = e.getPacket();
        if (packet.getClass().getSimpleName().startsWith("S")) {
            return;
        }
        if (packet instanceof C00Handshake
                || packet instanceof C00PacketLoginStart
                || packet instanceof C00PacketServerQuery
                || packet instanceof C01PacketEncryptionResponse
                || packet instanceof C01PacketChatMessage) {
            return;
        }
        blinkedPackets.add(packet);
        e.setCanceled(true);

        if (pulse.isToggled()) {
            if (System.currentTimeMillis() - startTime >= pulseDelay.getInput()) {
                reset();
                start();
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent e) {
        if (!Utils.nullCheck() || pos == null || !initialPosition.isToggled()) {
            return;
        }
        Blink.drawBox(pos);
    }
}
