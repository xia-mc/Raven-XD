package keystrokesmod.module.impl.player.fakelag;

import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.impl.player.Blink;
import keystrokesmod.module.impl.player.FakeLag;
import keystrokesmod.module.setting.impl.SubModeValue;
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

public class Latency extends SubModeValue {

    public Latency(String name) {
        super(name);
    }
    @Override
    public void onEnable() {
        FakeLag.lastDisableTime = -1;
        FakeLag.lastHurt = false;
        FakeLag.lastStartBlinkTime = -1;
        FakeLag.packetQueue.clear();
        FakeLag.vec3 = null;
    }
    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if (FakeLag.drawRealPosition.isToggled() && FakeLag.vec3 != null) {
            if (mc.gameSettings.thirdPersonView == 0) return;

            Blink.drawBox(FakeLag.vec3.toVec3());
        }
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderTick(TickEvent.RenderTickEvent ev) {
        if (!Utils.nullCheck()) {
            FakeLag.sendPacket(false);
            FakeLag.lastDisableTime = System.currentTimeMillis();
            FakeLag.lastStartBlinkTime = -1;
            return;
        }
        FakeLag.sendPacket(true);
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
            FakeLag.sendPacket(false);
            return;
        }
        if (e.isCanceled()) {
            return;
        }
        FakeLag.packetQueue.add(new TimedPacket(packet, receiveTime));
        e.setCanceled(true);
    }
}