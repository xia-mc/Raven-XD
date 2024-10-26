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
    public final ButtonSetting disableKillAura;
    private final ButtonSetting airStuck;
    private final ButtonSetting visual;
    private final ButtonSetting onlyWhileKillAura;

    public boolean working = false;
    private int eatingTicksLeft = 0;
    private int releaseLeft = 0;
    private int foodSlot;

    private final Queue<Packet<?>> delayedSend = new ConcurrentLinkedQueue<>();
    private final HashMap<Integer, RealPositionData> realPositions = new HashMap<>();

    private final Animation animation = new Animation(Easing.EASE_OUT_CIRC, 500);
    private final Progress progress = new Progress("AutoGapple");

    public AutoGapple() {
        super("AutoGapple", category.combat, "Made for QuickMacro.");
        this.registerSetting(minHealth = new SliderSetting("Min health", 10, 1, 20, 1));
        this.registerSetting(delayBetweenHeal = new SliderSetting("Delay between heal", 5, 0, 20, 1));
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
            int lastSlot = SlotHandler.getCurrentSlot();
            if (foodSlot != lastSlot)
                PacketUtils.sendPacketNoEvent(new C09PacketHeldItemChange(foodSlot));
            PacketUtils.sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            PacketUtils.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.mainInventory[foodSlot]));
            Utils.sendMessage("send.");
            release();
            if (foodSlot != lastSlot)
                PacketUtils.sendPacketNoEvent(new C09PacketHeldItemChange(lastSlot));
            releaseLeft = (int) delayBetweenHeal.getInput();
            foodSlot = -1;
        }

        if (releaseLeft > 0) {
            releaseLeft--;
        }

        if (!Utils.nullCheck() || mc.thePlayer.getHealth() >= minHealth.getInput() || (onlyWhileKillAura.isToggled() && KillAura.target == null)) {
            eatingTicksLeft = 0;
            return;
        }

        if (eatingTicksLeft > 0) {
            working = true;
            eatingTicksLeft--;

            return;
        }

        if (releaseLeft > 0)
            return;

        foodSlot = eat();
        if (foodSlot != -1) {
            animation.reset();
            eatingTicksLeft = 36;
            animation.setValue(eatingTicksLeft);
            mc.theWorld.playerEntities.parallelStream()
                    .filter(p -> p != mc.thePlayer)
                    .forEach(p -> realPositions.put(p.getEntityId(), new RealPositionData(p)));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onSendPacket(SendPacketEvent event) {
        if (working) {
            delayedSend.add(event.getPacket());
            event.setCanceled(true);
        }
    }

    private void release() {
        synchronized (delayedSend) {
            for (Packet<?> p : delayedSend) {
                PacketUtils.sendPacket(p);
            }
            delayedSend.clear();
        }
    }

    @SubscribeEvent
    public void onMove(PreMoveEvent event) {
        if (working && releaseLeft == 0 && airStuck.isToggled()) {
            event.setCanceled(true);
            delayedSend.add(new C03PacketPlayer(mc.thePlayer.onGround));
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
