package keystrokesmod.module.impl.player.fakelag;

import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.impl.player.Blink;
import keystrokesmod.module.impl.player.FakeLag;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.backtrack.TimedPacket;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LatencyFakeLag extends SubMode<FakeLag> {
    private final SliderSetting delay;
    private final ButtonSetting drawRealPosition;

    private Vec3 vec3 = null;
    private final Queue<TimedPacket> packetQueue = new ConcurrentLinkedQueue<>();

    public LatencyFakeLag(String name, @NotNull FakeLag parent) {
        super(name, parent);
        this.registerSetting(delay = new SliderSetting("Delay", 200, 25, 1000, 5, "ms"));
        this.registerSetting(drawRealPosition = new ButtonSetting("Draw real position", true));
    }

    @Override
    public void onEnable() {
        packetQueue.clear();
        vec3 = null;
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if (drawRealPosition.isToggled() && vec3 != null) {
            if (mc.gameSettings.thirdPersonView == 0) return;

            Blink.drawBox(vec3.toVec3());
        }
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
    public void onSendPacket(@NotNull SendPacketEvent e) {
        if (!Utils.nullCheck()) return;
        final Packet<?> packet = e.getPacket();
        if (packet instanceof C00Handshake
                || packet instanceof C00PacketLoginStart
                || packet instanceof C00PacketServerQuery
                || packet instanceof C01PacketEncryptionResponse
                || packet instanceof C01PacketChatMessage) {
            return;
        }
        long receiveTime = System.currentTimeMillis();
        if (!Utils.nullCheck()) {
            sendPacket(false);
            return;
        }
        if (e.isCanceled()) {
            return;
        }
        packetQueue.add(new TimedPacket(packet, receiveTime));
        e.setCanceled(true);
    }

    public void sendPacket(boolean delay) {
        try {
            while (!packetQueue.isEmpty()) {
                if (!delay || packetQueue.element().getCold().getCum((int) this.delay.getInput())) {
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