package keystrokesmod.module.impl.combat;

import keystrokesmod.event.*;
import keystrokesmod.mixins.impl.network.S14PacketEntityAccessor;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.impl.player.Blink;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.render.Animation;
import keystrokesmod.utility.render.Easing;
import keystrokesmod.utility.render.progress.Progress;
import keystrokesmod.utility.render.progress.ProgressManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
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
    private final ButtonSetting onlyWhileKillAura;

    public boolean working = false;
    private int eatingTicksLeft = 0;
    private int releaseLeft = 0;
    private int foodSlot;

    private final Queue<Packet<?>> delayedSend = new ConcurrentLinkedQueue<>();
    private final Queue<Packet<INetHandlerPlayClient>> delayedReceive = new ConcurrentLinkedQueue<>();
    private final HashMap<Integer, RealPositionData> realPositions = new HashMap<>();

    private final Animation animation = new Animation(Easing.EASE_OUT_CIRC, 500);
    private final Progress progress = new Progress("AutoGapple");

    public AutoGapple() {
        super("AutoGapple", category.combat, "Made for QuickMacro.");
        this.registerSetting(minHealth = new SliderSetting("Min health", 10, 1, 20, 1));
        this.registerSetting(delayBetweenHeal = new SliderSetting("Delay between heal", 5, 0, 20, 1));
        this.registerSetting(releaseTicksAfterVelocity = new SliderSetting("Release ticks after velocity", 0, 0, 5, 1));
        this.registerSetting(disableKillAura = new ButtonSetting("Disable killAura", false));
        this.registerSetting(airStuck = new ButtonSetting("Air stuck", false));
        this.registerSetting(visual = new ButtonSetting("Visual", true));
        this.registerSetting(onlyWhileKillAura = new ButtonSetting("Only while killAura", true));
    }

    @Override
    public void onDisable() {
        working = false;
        eatingTicksLeft = 0;
        releaseLeft = 0;
        release();
        realPositions.clear();
        ProgressManager.remove(progress);
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        if (eatingTicksLeft == 0 && working) {
            working = false;
            if (airStuck.isToggled()) {
                int lastSlot = SlotHandler.getCurrentSlot();
                if (foodSlot != lastSlot)
                    PacketUtils.sendPacketNoEvent(new C09PacketHeldItemChange(foodSlot));
                PacketUtils.sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                PacketUtils.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.mainInventory[foodSlot]));
                Utils.sendMessage("send.");
                mc.thePlayer.moveForward *= 0.2f;
                mc.thePlayer.moveStrafing *= 0.2f;
                release();
                if (foodSlot != lastSlot)
                    PacketUtils.sendPacketNoEvent(new C09PacketHeldItemChange(lastSlot));
                releaseLeft = (int) delayBetweenHeal.getInput();
                foodSlot = -1;
            } else {
                Utils.sendClick(1, false);
            }
        }

        if (releaseLeft > 0) {
            releaseLeft--;
        }

        if (!Utils.nullCheck() || mc.thePlayer.getHealth() >= minHealth.getInput() || (onlyWhileKillAura.isToggled() && KillAura.target == null)) {
            if (!airStuck.isToggled() && eatingTicksLeft > 0) {
                Utils.sendClick(1, false);
            }
            eatingTicksLeft = 0;
            return;
        }

        if (eatingTicksLeft > 0) {
            working = true;
            eatingTicksLeft--;

            if (!airStuck.isToggled()) {
                SlotHandler.setCurrentSlot(foodSlot);
                Utils.sendClick(1, true);
            }
            return;
        }

        if (releaseLeft > 0)
            return;

        foodSlot = eat();
        if (foodSlot != -1) {
            animation.reset();
            eatingTicksLeft = 36;
            animation.setValue(eatingTicksLeft);
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
        if (airStuck.isToggled() && working) {
            delayedSend.add(event.getPacket());
            event.setCanceled(true);
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
            for (Packet<?> p : delayedSend) {
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
                S12PacketEntityVelocity velo = (S12PacketEntityVelocity) p;
                if (velo.getEntityID() == mc.thePlayer.getEntityId()) {
                    if (releaseTicksAfterVelocity.getInput() > 0) {
                        releaseLeft = (int) releaseTicksAfterVelocity.getInput();
                    }
                    return;
                }
            }

            event.setCanceled(true);
            delayedReceive.add(p);
        }
    }

    @SubscribeEvent
    public void onMove(PreMoveEvent event) {
        if (working && airStuck.isToggled() && releaseLeft == 0) {
            event.setCanceled(true);
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

    @SubscribeEvent
    public void onRender2D(TickEvent.RenderTickEvent event) {
        if (!Utils.nullCheck() || mc.thePlayer.isDead) {
            working = false;
            eatingTicksLeft = 0;
            release();
            releaseLeft = (int) delayBetweenHeal.getInput();
            foodSlot = -1;
        }

        animation.run(eatingTicksLeft);
        if (working && visual.isToggled()) {
            progress.setProgress((32 - animation.getValue()) / 32);
            ProgressManager.add(progress);
        } else {
            ProgressManager.remove(progress);
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
