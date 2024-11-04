package keystrokesmod.module.impl.player;

import keystrokesmod.Raven;
import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.RenderContainerEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.ContainerUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemEmptyMap;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ChestStealer extends Module {
    private final SliderSetting minStartDelay = new SliderSetting("Min start delay", 100, 0, 500, 10, "ms");
    private final SliderSetting maxStartDelay = new SliderSetting("Max start delay", 200, 0, 500, 10, "ms");
    private final SliderSetting minStealDelay = new SliderSetting("Min steal delay", 100, 0, 500, 10, "ms");
    private final SliderSetting maxStealDelay = new SliderSetting("Max steal delay", 150, 0, 500, 10, "ms");
    private final ButtonSetting shuffle = new ButtonSetting("Shuffle", false);
    private final ButtonSetting autoClose = new ButtonSetting("Auto close", true);
    private final ButtonSetting autoCloseIfInvFull = new ButtonSetting("Auto close if inv full", true, autoClose::isToggled);
    private final SliderSetting minCloseDelay = new SliderSetting("Min close delay", 50, 0, 500, 10, "ms", autoClose::isToggled);
    private final SliderSetting maxCloseDelay = new SliderSetting("Max close delay", 100, 0, 500, 10, "ms", autoClose::isToggled);
    private static final ButtonSetting customChest = new ButtonSetting("Custom chest", false);
    private final ButtonSetting ignoreTrash = new ButtonSetting("Ignore trash", false);
    private static final ButtonSetting silent = new ButtonSetting("Silent", false);
    private static final ButtonSetting notMoving = new ButtonSetting("Not Moving", false);
    public static ButtonSetting allowMouseControl = new ButtonSetting("Allow mouse control", false);

    private static State state = State.NONE;
    private long nextStealTime;
    private long nextCloseTime;
    private final Set<Integer> stole = new HashSet<>();

    public static boolean noChestRender() {
        return ModuleManager.chestStealer != null && ModuleManager.chestStealer.isEnabled()
                && silent.isToggled() && ContainerUtils.isChest(customChest.isToggled());
    }

    public ChestStealer() {
        super("ChestStealer", category.player);
        this.registerSetting(minStartDelay, maxStartDelay, minStealDelay, maxStealDelay, shuffle,
                autoClose, autoCloseIfInvFull, minCloseDelay, maxCloseDelay,
                customChest, ignoreTrash, silent, notMoving, allowMouseControl);
    }

    @Override
    public void guiUpdate() {
        Utils.correctValue(minStartDelay, maxStartDelay);
        Utils.correctValue(minStealDelay, maxStealDelay);
        Utils.correctValue(minCloseDelay, maxCloseDelay);
    }

    @SubscribeEvent
    public void onRenderContainer(RenderContainerEvent event) {
        if (silent.isToggled() && ContainerUtils.isChest(customChest.isToggled()))
            event.setCanceled(true);
    }

    @Override
    public void onUpdate() {
        if (ContainerUtils.isChest(customChest.isToggled())) {
            if (state == State.NONE) {
                state = State.BEFORE;
                int delay = Utils.randomizeInt(minStartDelay.getInput(), maxStartDelay.getInput());
                nextStealTime = System.currentTimeMillis();
                if (delay == 0) {
                    state = State.STEAL;
                } else {
                    Raven.getExecutor().schedule(
                            () -> state = State.STEAL,
                            delay,
                            TimeUnit.MILLISECONDS
                    );
                }
            }
        } else {
            state = State.NONE;
            stole.clear();
        }
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        // Stop player movement on X and Z axes if `notMoving` is toggled and a chest is targeted
        if (notMoving.isToggled() && ContainerUtils.isChest(customChest.isToggled())) {
            mc.thePlayer.motionX = 0;
            mc.thePlayer.motionZ = 0;
        }

        if (allowMouseControl.isToggled() && ContainerUtils.isChest(customChest.isToggled())) {
            Utils.mc.inGameHasFocus = true;
            Utils.mc.mouseHelper.grabMouseCursor();
        }

        switch (state) {
            case STEAL:
                while (nextStealTime <= System.currentTimeMillis()) {
                    if (!ContainerUtils.isChest(customChest.isToggled())
                            || (autoCloseIfInvFull.isToggled() && ContainerUtils.inventoryFull())) {
                        close();
                        return;
                    }
                    final ContainerChest containerChest = (ContainerChest) mc.thePlayer.openContainer;

                    final List<Integer> items = getUnStoleItems(containerChest);
                    if (items.isEmpty()) {
                        close();
                        return;
                    }

                    final int slot = items.get(0);

                    stole.add(slot);
                    ContainerUtils.steal(containerChest, slot);  // in 1.8, the window click is only on tick.
                    nextStealTime = System.currentTimeMillis() + Utils.randomizeInt(minStealDelay.getInput(), maxStealDelay.getInput());
                }
                break;
            case AFTER:
                if (autoClose.isToggled() && nextCloseTime <= System.currentTimeMillis()) {
                    mc.thePlayer.closeScreen();
                    state = State.NONE;
                }
                break;
        }
    }

    private void close() {
        nextCloseTime = Utils.randomizeInt((int) minCloseDelay.getInput(), (int) maxCloseDelay.getInput());
        state = State.AFTER;
    }

    private @NotNull List<Integer> getUnStoleItems(@NotNull ContainerChest containerChest) {
        IInventory chest = containerChest.getLowerChestInventory();
        List<Integer> items = new ArrayList<>(chest.getSizeInventory());
        // yes... it's so stupid... but if it drops too many frames, then I will recode it.
        for (int i = 0; i < chest.getSizeInventory(); i++) {
            if (stole.contains(i)) continue;
            ItemStack stack = chest.getStackInSlot(i);
            if (stack == null || stack.getItem() instanceof ItemEmptyMap) continue;
            if (ignoreTrash.isToggled() && ContainerUtils.canDrop(stack, -1, mc.thePlayer.inventory)) continue;
            items.add(i);
        }

        if (shuffle.isToggled()) {
            Collections.shuffle(items);
        }

        return items;
    }

    enum State {
        NONE,
        BEFORE,
        STEAL,
        AFTER
    }
}