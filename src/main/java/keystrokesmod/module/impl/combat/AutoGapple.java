package keystrokesmod.module.impl.combat;

import keystrokesmod.event.*;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.render.Animation;
import keystrokesmod.utility.render.Easing;
import keystrokesmod.utility.render.RenderUtils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

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
    private final Animation animation = new Animation(Easing.EASE_OUT_CIRC, 500);
    private final Queue<Packet<INetHandlerPlayServer>> delayedSend = new ConcurrentLinkedQueue<>();
    private final Queue<Packet<INetHandlerPlayClient>> delayedReceive = new ConcurrentLinkedQueue<>();

    public AutoGapple() {
        super("AutoGapple", category.combat, "Made for QuickMacro.");
        this.registerSetting(minHealth = new SliderSetting("Min health", 10, 1, 20, 1));
        this.registerSetting(delayBetweenHeal = new SliderSetting("Delay between heal", 5, 0, 20, 1));
        this.registerSetting(releaseTicksAfterVelocity = new SliderSetting("Release ticks after velocity", 1, 0, 5, 1));
        this.registerSetting(disableKillAura = new ButtonSetting("Disable killAura", false));
        this.registerSetting(airStuck = new ButtonSetting("Air stuck", false));
        this.registerSetting(visual = new ButtonSetting("Visual", true));
    }

    @Override
    public void onDisable() throws Throwable {
        working = false;
        eatingTicksLeft = 0;
        releaseLeft = 0;
        synchronized (delayedSend) {
            for (Packet<INetHandlerPlayServer> p : delayedSend) {
                PacketUtils.sendPacketNoEvent(p);
            }
            delayedSend.clear();
        }
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        if (eatingTicksLeft == 0) {
            if (working) {
                working = false;
                int lastSlot = SlotHandler.getCurrentSlot();
                PacketUtils.sendPacket(new C09PacketHeldItemChange(foodSlot));
                SlotHandler.setCurrentSlot(foodSlot);
                PacketUtils.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                PacketUtils.sendPacket(new C08PacketPlayerBlockPlacement(SlotHandler.getHeldItem()));
                mc.thePlayer.moveForward *= 0.2f;
                mc.thePlayer.moveStrafing *= 0.2f;
                release();
                PacketUtils.sendPacket(new C09PacketHeldItemChange(lastSlot));
                SlotHandler.setCurrentSlot(lastSlot);
                releaseLeft = (int) delayBetweenHeal.getInput();
                foodSlot = -1;
            }
        }
        if (eatingTicksLeft > 0)
            working = true;
        if (releaseLeft > 0)
            working = false;
        if (releaseLeft > 0)
            releaseLeft--;
        if (eatingTicksLeft > 0) {
            eatingTicksLeft--;
        }

        if (!Utils.nullCheck() || mc.thePlayer.getHealth() >= minHealth.getInput())
            return;
        if (eatingTicksLeft > 0)
            return;

        foodSlot = eat();
        if (foodSlot != -1) {
            animation.reset();
            eatingTicksLeft = 35;
            animation.setValue(eatingTicksLeft);
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

    private void release() {
        synchronized (delayedSend) {
            for (Packet<INetHandlerPlayServer> p : delayedSend) {
                PacketUtils.sendPacket(p);
            }
            delayedSend.clear();
        }
        synchronized (delayedReceive) {
            for (Packet<INetHandlerPlayClient> p : delayedReceive) {
                PacketUtils.receivePacket(p);
            }
            delayedReceive.clear();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onReceivePacket(ReceivePacketEvent event) {
        if (airStuck.isToggled()) {
            if (working && event.getPacket() instanceof S12PacketEntityVelocity) {
                event.setCanceled(true);
                delayedReceive.add((S12PacketEntityVelocity) event.getPacket());
            }
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

    @SubscribeEvent
    public void onPreVelocity(PreVelocityEvent event) {
        releaseLeft = (int) releaseTicksAfterVelocity.getInput();
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
            animation.run(eatingTicksLeft);
            RenderUtils.drawProgressBar((32 - animation.getValue()) / 32, "AutoGapple");
        }
    }
}
