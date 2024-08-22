package keystrokesmod.module.impl.movement.longjump;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.mixins.impl.network.S27PacketExplosionAccessor;
import keystrokesmod.module.impl.movement.LongJump;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GrimVelocityLongJump extends SubMode<LongJump> {
    private final SliderSetting delayTime;
    private final ModeSetting releaseType;
    private final SliderSetting releaseDelay;
    private final ButtonSetting timer;
    private final SliderSetting timerSpeed;
    private final ButtonSetting debug;

    private long lastVelocityTime = -1;
    private boolean delayed = false;
    private final Queue<Packet<INetHandlerPlayClient>> delayedPackets = new ConcurrentLinkedQueue<>();

    public GrimVelocityLongJump(String name, @NotNull LongJump parent) {
        super(name, parent);
        this.registerSetting(delayTime = new SliderSetting("delay time", 5000, 500, 10000, 500));
        this.registerSetting(releaseType = new ModeSetting("Release type", new String[]{"Continuing", "Instant"}, 0));
        this.registerSetting(releaseDelay = new SliderSetting("Release delay", 2, 1, 10, 1, "ticks", new ModeOnly(releaseType, 0)));
        this.registerSetting(timer = new ButtonSetting("Timer", false));
        this.registerSetting(timerSpeed = new SliderSetting("Timer speed", 0.5, 0.01, 1, 0.01, timer::isToggled));
        this.registerSetting(debug = new ButtonSetting("Debug", false));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S27PacketExplosion) {
            event.setCanceled(true);
            delayedPackets.add((S27PacketExplosion) event.getPacket());
        }
        if (event.getPacket() instanceof S12PacketEntityVelocity) {
            if (((S12PacketEntityVelocity) event.getPacket()).getEntityID() != mc.thePlayer.getEntityId()) return;

            if (lastVelocityTime == -1) {
                lastVelocityTime = System.currentTimeMillis();
                delayed = true;
            }
            event.setCanceled(true);
            delayedPackets.add((S12PacketEntityVelocity) event.getPacket());
        } else if (event.getPacket() instanceof S32PacketConfirmTransaction) {
            if (delayed) {
                if (System.currentTimeMillis() - lastVelocityTime >= (int) delayTime.getInput()) {
                    delayed = false;
                }
                event.setCanceled(true);
                delayedPackets.add((S32PacketConfirmTransaction) event.getPacket());
            }
        }
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (!delayed && !delayedPackets.isEmpty()) {
            if (timer.isToggled())
                Utils.getTimer().timerSpeed = (float) timerSpeed.getInput();

            Packet<INetHandlerPlayClient> packet = delayedPackets.poll();

            if (packet instanceof S12PacketEntityVelocity) {
                S12PacketEntityVelocity p = (S12PacketEntityVelocity) packet;
                double motionX = p.getMotionX() / 8000.0;
                double motionY = p.getMotionY() / 8000.0;
                double motionZ = p.getMotionZ() / 8000.0;

                switch ((int) releaseType.getInput()) {
                    case 0:
                        if (mc.thePlayer.ticksExisted % (int) releaseDelay.getInput() + 1 != 0)
                            return;
                        mc.thePlayer.motionX = motionX;
                        mc.thePlayer.motionY = motionY;
                        mc.thePlayer.motionZ = motionZ;
                        break;
                    case 1:
                        mc.thePlayer.motionX += motionX;
                        mc.thePlayer.motionY += motionY;
                        mc.thePlayer.motionZ += motionZ;
                        break;
                }
                if (debug.isToggled()) {
                    Utils.sendMessage(String.format("Apply %.2f %.2f %.2f", motionX, motionY, motionZ));
                }
            } else {
                PacketUtils.receivePacketNoEvent(packet);
            }
        } else if (timer.isToggled())
            Utils.getTimer().timerSpeed = 1;
    }

    @Override
    public void onDisable() {
        for (Packet<INetHandlerPlayClient> p : delayedPackets) {
            PacketUtils.receivePacketNoEvent(p);
        }

        delayed = false;
        lastVelocityTime = -1;
        delayedPackets.clear();
        Utils.resetTimer();
    }

    private void release() {
        if (delayed) {
            if (debug.isToggled())
                Utils.sendMessage("release " + delayedPackets.size() + " packets.");

            for (Packet<INetHandlerPlayClient> p : delayedPackets) {
                PacketUtils.receivePacketNoEvent(p);
            }
        }

        delayed = false;
        lastVelocityTime = -1;
        delayedPackets.clear();
    }
}
