package keystrokesmod.module.impl.player.blink;

import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.impl.player.Blink;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.backtrack.TimedPacket;
import keystrokesmod.utility.render.Animation;
import keystrokesmod.utility.render.Easing;
import keystrokesmod.utility.render.RenderUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FakeLagBlink extends SubMode<Blink> {
    private final SliderSetting maxBlinkTime;
    private final ButtonSetting slowRelease;
    private final SliderSetting releaseSpeed;
    private final ButtonSetting antiAim;
    private final ButtonSetting drawRealPosition;

    private final Queue<TimedPacket> packetQueue = new ConcurrentLinkedQueue<>();
    private boolean needToDisable = false;
    private Vec3 vec3 = Vec3.ZERO;
    private final Animation animationX = new Animation(Easing.EASE_OUT_CIRC, 200);
    private final Animation animationY = new Animation(Easing.EASE_OUT_CIRC, 200);
    private final Animation animationZ = new Animation(Easing.EASE_OUT_CIRC, 200);
    private final Animation animationProgressBar = new Animation(Easing.EASE_OUT_CIRC, 500);

    public FakeLagBlink(String name, @NotNull Blink parent) {
        super(name, parent);
        this.registerSetting(maxBlinkTime = new SliderSetting("Max blink time", 20000, 1000, 30000, 500, "ms"));
        this.registerSetting(slowRelease = new ButtonSetting("Slow release", false));
        this.registerSetting(releaseSpeed = new SliderSetting("Release speed", 2, 2, 10, 0.1, "x", slowRelease::isToggled));
        this.registerSetting(antiAim = new ButtonSetting("Anti-Aim", true));
        this.registerSetting(drawRealPosition = new ButtonSetting("Draw real position", true));
    }

    @Override
    public void onEnable() throws Throwable {
        packetQueue.clear();
        needToDisable = false;
        vec3 = new Vec3(mc.thePlayer);
        animationX.setValue(vec3.x());
        animationY.setValue(vec3.y());
        animationZ.setValue(vec3.z());
        animationProgressBar.setValue(0);
    }

    @Override
    public void onDisable() throws Throwable {
        if (!needToDisable) {
            MinecraftForge.EVENT_BUS.register(this);
            needToDisable = true;
        }
    }

    @Override
    public String getInfo() {
        return String.valueOf(packetQueue.size());
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        animationX.run(vec3.x());
        animationY.run(vec3.y());
        animationZ.run(vec3.z());
        if (drawRealPosition.isToggled()) {
            Blink.drawBox(new net.minecraft.util.Vec3(animationX.getValue(), animationY.getValue(), animationZ.getValue()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderTick(TickEvent.RenderTickEvent ev) {
        if (!Utils.nullCheck()) {
            sendPacket(false);
            return;
        } else if (needToDisable) {
            synchronized (packetQueue) {
                sendPacket(false);
                if (packetQueue.isEmpty()) {
                    MinecraftForge.EVENT_BUS.unregister(this);
                    return;
                }
            }
        }
        sendPacket(true);

        if (packetQueue.isEmpty()) {
            animationProgressBar.run(0);
        } else {
            animationProgressBar.run(System.currentTimeMillis() - packetQueue.element().getMillis());
        }
        RenderUtils.drawProgressBar(Math.min(1, (animationProgressBar.getValue() + 50) / maxBlinkTime.getInput()), "Blink");
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
                boolean shouldSend;
                if (delay && !(antiAim.isToggled() && shouldAntiAim())) {
                    shouldSend = packetQueue.element().getCold().getCum((int) this.maxBlinkTime.getInput());
                } else {
                    if (slowRelease.isToggled()) {
                        shouldSend = packetQueue.element().getCold().getCum((int) (this.maxBlinkTime.getInput() / releaseSpeed.getInput()));
                    } else {
                        shouldSend = true;
                    }
                }

                if (shouldSend) {
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

    private boolean shouldAntiAim() {
        return mc.theWorld.playerEntities.parallelStream()
                .filter(target -> target != mc.thePlayer)
                .filter(target -> !AntiBot.isBot(target))
                .filter(target -> !Utils.isTeamMate(target))
                .filter(target -> new Vec3(target).distanceTo(vec3) < 5)
                .anyMatch(target -> Utils.inFov(target.getRotationYawHead(), 120, vec3.x(), vec3.z()));
    }
}
