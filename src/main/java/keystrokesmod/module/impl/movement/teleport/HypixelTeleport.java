package keystrokesmod.module.impl.movement.teleport;

import keystrokesmod.event.*;
import keystrokesmod.module.impl.movement.Teleport;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.RotationUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HypixelTeleport extends SubMode<Teleport> {
    private State state = State.NONE;
    private int hasLag = 0;
    private int timerTicks = -1;
    private final Queue<Packet<?>> delayedPackets = new ConcurrentLinkedQueue<>();
    private float yaw, pitch;

    public HypixelTeleport(String name, @NotNull Teleport parent) {
        super(name, parent);
    }

    @SubscribeEvent
    public void onClick(ClickEvent event) {
        if (timerTicks != -1) return;
        MovingObjectPosition hitResult = RotationUtils.rayCast(15, RotationHandler.getRotationYaw(), RotationHandler.getRotationPitch());
        if (hitResult != null && hitResult.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            timerTicks = (int) Math.floor(new Vec3(hitResult.getBlockPos()).distanceTo(mc.thePlayer) / MoveUtil.getAllowedHorizontalDistance());
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onMove(MoveInputEvent event) {
        if (state == State.TIMER) {
            event.setForward(1);
            event.setStrafe(0);
        }
    }

    @Override
    public void onUpdate() {
        switch (state) {
            case NONE:
                if (timerTicks != -1)
                    state = State.TIMER;
                break;
            case TIMER:
                for (int i = 0; i < timerTicks; i++) {
                    mc.thePlayer.onUpdate();
                }
                yaw = RotationHandler.getRotationYaw();
                pitch = RotationHandler.getRotationPitch();
                hasLag = 0;
                state = State.LAG;
                break;
            case LAG:
                if (hasLag >= timerTicks + 2)
                    done();
                else
                    hasLag++;
                break;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onSendPacket(SendPacketEvent event) {
        switch (state) {
            case TIMER:
                synchronized (delayedPackets) {
                    delayedPackets.add(event.getPacket());
                    event.setCanceled(true);
                }
                break;
            case LAG:
                if (event.getPacket() instanceof C03PacketPlayer) {
                    event.setCanceled(true);
                } else {
                    synchronized (delayedPackets) {
                        delayedPackets.add(event.getPacket());
                        event.setCanceled(true);
                    }
                }
                break;
        }
    }

    @SubscribeEvent
    public void onMove(@NotNull MoveEvent event) {
        if (state == State.LAG) {
            event.setCanceled(true);
            mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0;
        }
    }

    @SubscribeEvent
    public void onRotation(RotationEvent event) {
        if (state == State.LAG) {
            event.setYaw(yaw);
            event.setPitch(pitch);
            event.noSmoothBack();
        }
    }

    @Override
    public void onDisable() {
        done();
    }

    private void done() {
        state = State.NONE;
        hasLag = 0;
        timerTicks = -1;

        synchronized (delayedPackets) {
            for (Packet<?> p : delayedPackets) {
                PacketUtils.sendPacket(p);
            }
            delayedPackets.clear();
        }

        mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0;
    }

    enum State {
        NONE,
        TIMER,
        LAG
    }
}
