package keystrokesmod.module.impl.player;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import keystrokesmod.event.PreTickEvent;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.mixins.impl.network.S14PacketEntityAccessor;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.backtrack.TimedPacket;
import keystrokesmod.utility.render.Animation;
import keystrokesmod.utility.render.Easing;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.*;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Backtrack extends Module {
    public static final Color color = new Color(72, 125, 227);

    private final SliderSetting minLatency = new SliderSetting("Min latency", 50, 10, 1000, 10);
    private final SliderSetting maxLatency = new SliderSetting("Max latency", 100, 10, 1000, 10);
    private final SliderSetting minDistance = new SliderSetting("Min distance", 0.0, 0.0, 3.0, 0.1);
    private final SliderSetting maxDistance = new SliderSetting("Max distance", 6.0, 0.0, 10.0, 0.1);
    private final SliderSetting stopOnTargetHurtTime = new SliderSetting("Stop on target HurtTime", -1, -1, 10, 1);
    private final SliderSetting stopOnSelfHurtTime = new SliderSetting("Stop on self HurtTime", -1, -1, 10, 1);

    private final Queue<TimedPacket> packetQueue = new ConcurrentLinkedQueue<>();
    private final List<Packet<?>> skipPackets = new ArrayList<>();
    private @Nullable Animation animationX;
    private @Nullable Animation animationY;
    private @Nullable Animation animationZ;
    private Vec3 vec3;
    private EntityPlayer target;

    private int currentLatency = 0;

    public Backtrack() {
        super("Backtrack", category.player);
        this.registerSetting(new DescriptionSetting("Allows you to hit past opponents."));
        this.registerSetting(minLatency);
        this.registerSetting(maxLatency);
        this.registerSetting(minDistance);
        this.registerSetting(maxDistance);
        this.registerSetting(stopOnTargetHurtTime);
        this.registerSetting(stopOnSelfHurtTime);
    }

    @Override
    public String getInfo() {
        return (currentLatency == 0 ? (int) maxLatency.getInput() : currentLatency) + "ms";
    }

    @Override
    public void guiUpdate() {
        Utils.correctValue(minLatency, maxLatency);
        Utils.correctValue(minDistance, maxDistance);
    }

    @Override
    public void onEnable() {
        packetQueue.clear();
        skipPackets.clear();
        vec3 = null;
        target = null;
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (mc.thePlayer == null)
            return;

        releaseAll();
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent e) {
        try {
            final double distance = vec3.distanceTo(mc.thePlayer);
            if (distance > maxDistance.getInput()
                    || distance < minDistance.getInput()
            ) {
                currentLatency = 0;
            }

        } catch (NullPointerException ignored) {
        }
    }

    @SubscribeEvent
    public void onPreTick(PreTickEvent e) {
        while (!packetQueue.isEmpty()) {
            try {
                if (packetQueue.element().getCold().getCum(currentLatency)) {
                    Packet<INetHandlerPlayClient> packet = (Packet<INetHandlerPlayClient>) packetQueue.remove().getPacket();
                    skipPackets.add(packet);
                    PacketUtils.receivePacket(packet);
                } else {
                    break;
                }
            } catch (NullPointerException ignored) {
            }
        }

        if (packetQueue.isEmpty() && target != null) {
            vec3 = new Vec3(target.getPositionVector());
        }
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent e) {
        if (target == null || vec3 == null || target.isDead)
            return;

        final net.minecraft.util.Vec3 pos = currentLatency > 0 ? vec3.toVec3() : target.getPositionVector();

        if (animationX == null || animationY == null || animationZ == null) {
            animationX = new Animation(Easing.EASE_OUT_CIRC, 50);
            animationY = new Animation(Easing.EASE_OUT_CIRC, 50);
            animationZ = new Animation(Easing.EASE_OUT_CIRC, 50);

            animationX.setValue(pos.xCoord);
            animationY.setValue(pos.yCoord);
            animationZ.setValue(pos.zCoord);
        }

        animationX.run(pos.xCoord);
        animationY.run(pos.yCoord);
        animationZ.run(pos.zCoord);
        Blink.drawBox(new net.minecraft.util.Vec3(animationX.getValue(), animationY.getValue(), animationZ.getValue()));
    }

    @SubscribeEvent
    public void onAttack(@NotNull AttackEntityEvent e) {
        final Vec3 targetPos = new Vec3(e.target);
        if (e.target instanceof EntityPlayer) {
            if (target == null || e.target != target) {
                vec3 = targetPos;
                if (animationX != null && animationY != null && animationZ != null) {
                    long duration = target == null ? 0 : Math.min(500, Math.max(100, (long) new Vec3(e.target).distanceTo(target) * 50));
                    animationX.setDuration(duration);
                    animationY.setDuration(duration);
                    animationZ.setDuration(duration);
                }
            } else if (animationX != null && animationY != null && animationZ != null) {
                animationX.setDuration(100);
                animationY.setDuration(100);
                animationZ.setDuration(100);
            }
            target = (EntityPlayer) e.target;

            try {
                final double distance = targetPos.distanceTo(mc.thePlayer);
                if (distance > maxDistance.getInput() || distance < minDistance.getInput())
                    return;

            } catch (NullPointerException ignored) {
            }

            currentLatency = (int) (Math.random() * (maxLatency.getInput() - minLatency.getInput()) + minLatency.getInput());
        }
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent e) {
        if (!Utils.nullCheck()) return;
        Packet<?> p = e.getPacket();
        if (skipPackets.contains(p)) {
            skipPackets.remove(p);
            return;
        }

        if (target != null && stopOnTargetHurtTime.getInput() != -1 && target.hurtTime == stopOnTargetHurtTime.getInput()) {
            releaseAll();
            return;
        }
        if (stopOnSelfHurtTime.getInput() != -1 && mc.thePlayer.hurtTime == stopOnSelfHurtTime.getInput()) {
            releaseAll();
            return;
        }

        try {
            if (mc.thePlayer == null || mc.thePlayer.ticksExisted < 20) {
                packetQueue.clear();
                return;
            }

            if (target == null) {
                releaseAll();
                return;
            }

            if (e.isCanceled())
                return;

            if (p instanceof S19PacketEntityStatus
                    || p instanceof S02PacketChat
                    || p instanceof S0BPacketAnimation
                    || p instanceof S06PacketUpdateHealth
            )
                return;

            if (p instanceof S08PacketPlayerPosLook || p instanceof S40PacketDisconnect) {
                releaseAll();
                target = null;
                vec3 = null;
                return;

            } else if (p instanceof S13PacketDestroyEntities) {
                S13PacketDestroyEntities wrapper = (S13PacketDestroyEntities) p;
                for (int id : wrapper.getEntityIDs()) {
                    if (id == target.getEntityId()) {
                        target = null;
                        vec3 = null;
                        releaseAll();
                        return;
                    }
                }
            } else if (p instanceof S14PacketEntity) {
                S14PacketEntity wrapper = (S14PacketEntity) p;
                if (((S14PacketEntityAccessor) wrapper).getEntityId() == target.getEntityId()) {
                    vec3 = vec3.add(wrapper.func_149062_c() / 32.0D, wrapper.func_149061_d() / 32.0D,
                            wrapper.func_149064_e() / 32.0D);
                }
            } else if (p instanceof S18PacketEntityTeleport) {
                S18PacketEntityTeleport wrapper = (S18PacketEntityTeleport) p;
                if (wrapper.getEntityId() == target.getEntityId()) {
                    vec3 = new Vec3(wrapper.getX() / 32.0D, wrapper.getY() / 32.0D, wrapper.getZ() / 32.0D);
                }
            }

            packetQueue.add(new TimedPacket(p));
            e.setCanceled(true);
        } catch (NullPointerException ignored) {

        }
    }

    private void releaseAll() {
        if (!packetQueue.isEmpty()) {
            for (TimedPacket timedPacket : packetQueue) {
                Packet<INetHandlerPlayClient> packet = (Packet<INetHandlerPlayClient>) timedPacket.getPacket();
                skipPackets.add(packet);
                PacketUtils.receivePacket(packet);
            }
            packetQueue.clear();
        }
    }

}