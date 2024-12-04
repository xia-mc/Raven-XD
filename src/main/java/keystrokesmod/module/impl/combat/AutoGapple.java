package keystrokesmod.module.impl.combat;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.PreMoveEvent;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.event.PreVelocityEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.render.Animation;
import keystrokesmod.utility.render.Easing;
import keystrokesmod.utility.render.progress.Progress;
import keystrokesmod.utility.render.progress.ProgressManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class AutoGapple extends Module {
    private final SliderSetting minHealth;
    private final SliderSetting eatTicks;
    private final SliderSetting pauseOnVelocity;
    private final SliderSetting storeDelay;
    private final SliderSetting storePauseTicks;
    private final ButtonSetting visual;
    private final ButtonSetting onlyWhileKillAura;

    private final Animation animation = new Animation(Easing.EASE_OUT_CIRC, 500);
    private final Progress progress = new Progress("AutoGapple");
    public boolean working = false;
    private int stored = 0;
    private int pauseTicks = 0;
    private boolean storeDelayed = false;
    private boolean toCancel = false;

    public AutoGapple() {
        super("AutoGapple", category.combat, "Made for QuickMacro.");
        this.registerSetting(minHealth = new SliderSetting("Min health", 10, 1, 20, 1));
        this.registerSetting(eatTicks = new SliderSetting("Eat ticks", 31, 31, 35, 1));
        this.registerSetting(pauseOnVelocity = new SliderSetting("Pause on velocity", 1, 0, 2, 1));
        this.registerSetting(storeDelay = new SliderSetting("Store delay", 5, 0, 10, 1));
        this.registerSetting(storePauseTicks = new SliderSetting("Store pause ticks", 1, 1, 3, 1, () -> storeDelay.getInput() > 0));
        this.registerSetting(visual = new ButtonSetting("Visual", true));
        this.registerSetting(onlyWhileKillAura = new ButtonSetting("Only while killAura", true));
    }

    @Override
    public void onDisable() {
        reset();
        ProgressManager.remove(progress);
    }

    public void reset() {
        working = false;
        stored = 0;
        pauseTicks = 0;
        storeDelayed = false;
        toCancel = false;
        progress.setProgress(0);
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        if (!Utils.nullCheck()
                || mc.thePlayer.isDead
                || mc.thePlayer.getHealth() >= minHealth.getInput()
                || (onlyWhileKillAura.isToggled() && KillAura.target == null)) {
            working = false;
            return;
        }

        if (stored >= eatTicks.getInput() && working) {
            reset();

            int foodSlot = getFoodSlot();
            int curSlot = SlotHandler.getCurrentSlot();
            if (foodSlot != curSlot) {
                SlotHandler.setCurrentSlot(foodSlot);
            }
            mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, SlotHandler.getHeldItem());
            PacketUtils.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.DROP_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            Utils.sendMessage("send.");
            for (int i = 0; i < stored; i++) {
                PacketUtils.sendPacket(new C03PacketPlayer(mc.thePlayer.onGround));
            }
            if (foodSlot != curSlot) {
                SlotHandler.setCurrentSlot(curSlot);
            }
        }

        if (getFoodSlot() != -1) {
            working = true;
        } else {
            reset();
        }
    }

    @SubscribeEvent
    public void onPreVelocity(PreVelocityEvent event) {
        pauseTicks += (int) pauseOnVelocity.getInput();
    }

    @SubscribeEvent
    public void onPreMove(PreMoveEvent event) {
        if (!working) return;

        if (!storeDelayed && stored % (int) storeDelay.getInput() == 1) {
            storeDelayed = true;
            pauseTicks = (int) storePauseTicks.getInput();
        }

        if (pauseTicks > 0) {
            pauseTicks--;
        } else {
            event.setCanceled(true);
            stored++;
            storeDelayed = false;
            toCancel = true;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPreMotion(PreMotionEvent event) {
        if (toCancel)
            event.setCanceled(true);
        toCancel = false;
    }

    public int getFoodSlot() {
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
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        animation.run(eatTicks.getInput() - stored);
        if (working && visual.isToggled()) {
            progress.setProgress((eatTicks.getInput() - animation.getValue()) / eatTicks.getInput());
            ProgressManager.add(progress);
        } else {
            ProgressManager.remove(progress);
        }
    }
}
