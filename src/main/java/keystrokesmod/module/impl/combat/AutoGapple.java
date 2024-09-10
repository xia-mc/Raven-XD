package keystrokesmod.module.impl.combat;

import keystrokesmod.event.*;
import keystrokesmod.mixins.impl.network.S14PacketEntityAccessor;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.impl.player.Blink;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.render.Animation;
import keystrokesmod.utility.render.Easing;
import keystrokesmod.utility.render.RenderUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.*;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AutoGapple extends Module {
    private final SliderSetting minHealth;
    private final SliderSetting delayBetweenHeal;
    private final SliderSetting releaseTicksAfterVelocity;
    public final ButtonSetting disableKillAura;
    private final ButtonSetting airStuck;
    private final ButtonSetting visual;

    public boolean working = false;
    private int eatingTicksLeft = 0;
    private int releaseLeft = 0;
    private int foodSlot;

    public static double motionX, motionY, motionZ;
    private float yaw, pitch;
    private final Animation progress = new Animation(Easing.EASE_OUT_CIRC, 500);
    private final Queue<Packet<INetHandlerPlayServer>> delayedSend = new ConcurrentLinkedQueue<>();
    private final Queue<Packet<INetHandlerPlayClient>> delayedReceive = new ConcurrentLinkedQueue<>();
    private final HashMap<Integer, RealPositionData> realPositions = new HashMap<>();

    public AutoGapple() {
        super("AutoGapple", category.combat, "Made for QuickMacro.");
        this.registerSetting(minHealth = new SliderSetting("Min health", 10, 1, 20, 1));
        this.registerSetting(delayBetweenHeal = new SliderSetting("Delay between heal", 5, 0, 20, 1));
        this.registerSetting(releaseTicksAfterVelocity = new SliderSetting("Release ticks after velocity", 0, 0, 5, 1));
        this.registerSetting(disableKillAura = new ButtonSetting("Disable killAura", false));
        this.registerSetting(airStuck = new ButtonSetting("Air stuck", false));
        this.registerSetting(visual = new ButtonSetting("Visual", true));
    }

    @Override
    public void onDisable() {
        working = false;
        eatingTicksLeft = 0;
        releaseLeft = 0;
        release();
        realPositions.clear();
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        if (eatingTicksLeft == 0 && working) {
            working = false;
            if (airStuck.isToggled()) {
                int lastSlot = SlotHandler.getCurrentSlot();
                PacketUtils.sendPacket(new C09PacketHeldItemChange(foodSlot));
                SlotHandler.setCurrentSlot(foodSlot);
                PacketUtils.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                PacketUtils.sendPacket(new C08PacketPlayerBlockPlacement(SlotHandler.getHeldItem()));
                Utils.sendMessage("send.");
                mc.thePlayer.moveForward *= 0.2f;
                mc.thePlayer.moveStrafing *= 0.2f;
                release();
                PacketUtils.sendPacket(new C09PacketHeldItemChange(lastSlot));
                SlotHandler.setCurrentSlot(lastSlot);
                releaseLeft = (int) delayBetweenHeal.getInput();
                foodSlot = -1;
            } else {
                Utils.sendClick(1, false);
            }
        }

        if (releaseLeft > 0) {
            releaseLeft--;
            working = false;
            return;
        }

        if (!Utils.nullCheck() || mc.thePlayer.getHealth() >= minHealth.getInput()) {
            eatingTicksLeft = 0;
            return;
        }

        if (eatingTicksLeft > 0) {
            working = true;
            eatingTicksLeft--;
            return;
        }

        foodSlot = eat();
        if (foodSlot != -1) {
            progress.reset();
            eatingTicksLeft = 36;
            progress.setValue(eatingTicksLeft);
            if (airStuck.isToggled()) {
                mc.theWorld.playerEntities.parallelStream()
                        .filter(p -> p != mc.thePlayer)
                        .forEach(p -> realPositions.put(p.getEntityId(), new RealPositionData(p)));
            } else {
                SlotHandler.setCurrentSlot(foodSlot);
                Utils.sendClick(1, true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onSendPacket(SendPacketEvent event) {
        if (airStuck.isToggled()) {
            if (working && event.getPacket() instanceof C0FPacketConfirmTransaction) {
                event.setCanceled(true);
                delayedSend.add((C0FPacketConfirmTransaction) event.getPacket());
            }
            if (working && event.getPacket() instanceof C08PacketPlayerBlockPlacement) {
                event.setCanceled(true);
                delayedSend.add((C03PacketPlayer) event.getPacket());
            }
            if (working && event.getPacket() instanceof C03PacketPlayer) {
                event.setCanceled(true);
                delayedSend.add((C03PacketPlayer) event.getPacket());
            }
        }
    }

    @SubscribeEvent
    public void onSprint(SprintEvent event) {
        if (airStuck.isToggled() && working) {
            // to fix NoSlowD
            event.setSprint(false);
        }
    }

    private void release() {
        synchronized (delayedReceive) {
            for (Packet<INetHandlerPlayClient> p : delayedReceive) {
                PacketUtils.receivePacket(p);
            }
            delayedReceive.clear();
        }
        synchronized (delayedSend) {
            for (Packet<INetHandlerPlayServer> p : delayedSend) {
                PacketUtils.sendPacket(p);
            }
            delayedSend.clear();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onReceivePacket(ReceivePacketEvent event) {
        if (airStuck.isToggled() && working) {
            if (event.isCanceled())
                return;

            final Packet<INetHandlerPlayClient> p = event.getPacket();

            if (p instanceof S19PacketEntityStatus
                    || p instanceof S02PacketChat
                    || p instanceof S0BPacketAnimation
                    || p instanceof S06PacketUpdateHealth
                    || p instanceof S32PacketConfirmTransaction
            )
                return;

            if (p instanceof S08PacketPlayerPosLook || p instanceof S40PacketDisconnect) {
                onDisable();
                return;

            } else if (p instanceof S13PacketDestroyEntities) {
                S13PacketDestroyEntities wrapper = (S13PacketDestroyEntities) p;
                for (int id : wrapper.getEntityIDs()) {
                    realPositions.remove(id);
                }
            } else if (p instanceof S14PacketEntity) {
                S14PacketEntity wrapper = (S14PacketEntity) p;
                final int id = ((S14PacketEntityAccessor) wrapper).getEntityId();
                if (realPositions.containsKey(id)) {
                    final RealPositionData data = realPositions.get(id);
                    data.vec3 = data.vec3.add(wrapper.func_149062_c() / 32.0D, wrapper.func_149061_d() / 32.0D,
                            wrapper.func_149064_e() / 32.0D);
                }
            } else if (p instanceof S18PacketEntityTeleport) {
                S18PacketEntityTeleport wrapper = (S18PacketEntityTeleport) p;
                final int id = wrapper.getEntityId();
                if (realPositions.containsKey(id)) {
                    final RealPositionData data = realPositions.get(id);
                    data.vec3 = new Vec3(wrapper.getX() / 32.0D, wrapper.getY() / 32.0D, wrapper.getZ() / 32.0D);
                }
            } else if (p instanceof S12PacketEntityVelocity) {
                if (((S12PacketEntityVelocity) p).getEntityID() == mc.thePlayer.getEntityId()) {
                    if (releaseTicksAfterVelocity.getInput() > 0) {
                        working = false;
                        releaseLeft = (int) releaseTicksAfterVelocity.getInput();
                        release();
                    }
                }
            }

            event.setCanceled(true);
            delayedReceive.add(p);
        }
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if (working && airStuck.isToggled()) {
            event.setCanceled(true);
            mc.thePlayer.motionX = motionX;
            mc.thePlayer.motionY = motionY;
            mc.thePlayer.motionZ = motionZ;
        } else {
            motionX = mc.thePlayer.motionX;
            motionY = mc.thePlayer.motionY;
            motionZ = mc.thePlayer.motionZ;
            yaw = RotationHandler.getRotationYaw();
            pitch = RotationHandler.getRotationPitch();
        }
    }

    private int eat() {
        int slot = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack getStackInSlot = mc.thePlayer.inventory.getStackInSlot(i);
            if (getStackInSlot != null && getStackInSlot.getItem() == Items.golden_apple) {
                slot = i;
                break;
            }
        }
        return slot;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRotation(RotationEvent event) {
        if (working && airStuck.isToggled()) {
            event.setYaw(yaw);
            event.setPitch(pitch);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPreMotion(PreMotionEvent event) {
        if (working && airStuck.isToggled()) {
            event.setYaw(yaw);
            event.setPitch(pitch);
        }
    }

    @SubscribeEvent
    public void onRender2D(TickEvent.RenderTickEvent event) {
        if (working && visual.isToggled()) {
            progress.run(eatingTicksLeft);
            RenderUtils.drawProgressBar((32 - progress.getValue()) / 32, "AutoGapple");
        }
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent e) {
        if (eatingTicksLeft == 0)
            return;

        for (RealPositionData data : realPositions.values()) {
            data.animationX.run(data.vec3.x());
            data.animationY.run(data.vec3.y());
            data.animationZ.run(data.vec3.z());
            Blink.drawBox(new net.minecraft.util.Vec3(data.animationX.getValue(), data.animationY.getValue(), data.animationZ.getValue()));
        }
    }

    private static class RealPositionData {
        public Vec3 vec3;
        public Animation animationX = new Animation(Easing.EASE_OUT_CIRC, 150);
        public Animation animationY = new Animation(Easing.EASE_OUT_CIRC, 150);
        public Animation animationZ = new Animation(Easing.EASE_OUT_CIRC, 150);

        public RealPositionData(EntityPlayer player) {
            vec3 = new Vec3(player);
            animationX.setValue(vec3.x());
            animationY.setValue(vec3.y());
            animationZ.setValue(vec3.z());
        }
    }
}
