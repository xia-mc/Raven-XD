package keystrokesmod.module.impl.movement.longjump;

import keystrokesmod.event.*;
import keystrokesmod.module.impl.movement.LongJump;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.CoolDown;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.packet.OutgoingPackets;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HypixelLongJump extends SubMode<LongJump> {
    private static final Set<Class<?>> shouldDelay = new HashSet<>(Arrays.asList(C0FPacketConfirmTransaction.class, C00PacketKeepAlive.class, S1CPacketEntityMetadata.class, S12PacketEntityVelocity.class, S27PacketExplosion.class, C02PacketUseEntity.class, C0DPacketCloseWindow.class, C0EPacketClickWindow.class, C0CPacketInput.class, C0BPacketEntityAction.class, C08PacketPlayerBlockPlacement.class, C07PacketPlayerDigging.class, C09PacketHeldItemChange.class, C13PacketPlayerAbilities.class, C15PacketClientSettings.class, C16PacketClientStatus.class, C17PacketCustomPayload.class, C18PacketSpectate.class, C19PacketResourcePackStatus.class, C03PacketPlayer.class, C03PacketPlayer.C04PacketPlayerPosition.class, C03PacketPlayer.C05PacketPlayerLook.class, C03PacketPlayer.C06PacketPlayerPosLook.class, C0APacketAnimation.class));

    private boolean selfDamaging = false;
    private int jumps = 0;
    private int offGroundTicks = 0;
    private int ticksSinceVelocity = 99999;

    private final CoolDown delayed = new CoolDown(100);
    private final Queue<Packet<?>> delayedPackets = new ConcurrentLinkedQueue<>();

    public HypixelLongJump(String name, @NotNull LongJump parent) {
        super(name, parent);
    }

    @Override
    public void onEnable() {
        selfDamaging = true;
        jumps = 0;
        offGroundTicks = 0;
        ticksSinceVelocity = 99999;
    }

    @Override
    public void onDisable() {
        Utils.resetTimer();
        releasePackets();
    }

    private void releasePackets() {
        for (Packet<?> p : delayedPackets) {
            if (OutgoingPackets.getOutgoingPackets().contains(p.getClass())) {
                PacketUtils.sendPacketNoEvent(p);
            } else {
                PacketUtils.receivePacketNoEvent((Packet<INetHandlerPlayClient>) p);
            }
        }
        delayedPackets.clear();
    }

    @SubscribeEvent
    public void onPostMotion(PostMotionEvent event) {
        if (delayed.hasFinished()) {
            releasePackets();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPreVelocity(@NotNull PreVelocityEvent event) {
        event.setCanceled(true);
        mc.thePlayer.motionY = event.getMotionY() / 8000.0D;

        ticksSinceVelocity = 0;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (!delayed.hasFinished()) {
            if (shouldDelay.contains(event.getPacket().getClass())) {
                event.setCanceled(true);
                delayedPackets.add(event.getPacket());
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onSendPacket(@NotNull SendPacketEvent event) {
        if (!delayed.hasFinished()) {
            if (shouldDelay.contains(event.getPacket().getClass())) {
                event.setCanceled(true);
                delayedPackets.add(event.getPacket());
            }
        }
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer.onGround)
            offGroundTicks = 0;
        else
            offGroundTicks++;
        ticksSinceVelocity++;

        if (selfDamaging) {
            MoveUtil.stop();
            if (jumps < 4) {
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.motionY = 0.42;
                    jumps++;
                }

                event.setOnGround(false);
            } else if (offGroundTicks >= 11) {
                selfDamaging = false;
                jumps = 0;
            }
        } else {
            if (mc.thePlayer.onGround) {
                MoveUtil.strafe(MoveUtil.getAllowedHorizontalDistance() - Math.random() / 100);
                mc.thePlayer.jump();
            }

            event.setOnGround(false);

            if (offGroundTicks == 1) {
                Utils.getTimer().timerSpeed = 0.2f;
                event.setOnGround(true);
            }

            if (offGroundTicks <= 5 && offGroundTicks > 1) {
                delayed.start();
            }

            if (ticksSinceVelocity <= 20 && ticksSinceVelocity > 1) {
                mc.thePlayer.motionY += 0.0239;

                MoveUtil.moveFlying(0.0039);
            }
        }
    }

    @SubscribeEvent
    public void onPostStrafe(PostPlayerInputEvent event) {
        if (selfDamaging)
            MoveUtil.stop();
    }

    @SubscribeEvent
    public void onMove(@NotNull MoveInputEvent event) {
        if (selfDamaging) {
            event.setForward(0);
            event.setStrafe(0);
        }
    }
}
