package keystrokesmod.module.impl.player;

import akka.japi.Pair;
import keystrokesmod.Raven;
import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.utility.ContainerUtils;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemEmptyMap;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0DPacketCloseWindow;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class InvManager extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", new String[]{"Basic", "OpenInv"}, 1);
    private final ButtonSetting notWhileMoving = new ButtonSetting("Not while moving", false, new ModeOnly(mode, 0));
    private final SliderSetting minStartDelay = new SliderSetting("Min start delay", 100, 0, 500, 10, "ms");
    private final SliderSetting maxStartDelay = new SliderSetting("Max start delay", 200, 0, 500, 10, "ms");
    private final ButtonSetting armor = new ButtonSetting("Armor", false);
    private final SliderSetting minArmorDelay = new SliderSetting("Min armor delay", 100, 0, 500, 10, "ms", armor::isToggled);
    private final SliderSetting maxArmorDelay = new SliderSetting("Max armor delay", 150, 0, 500, 10, "ms", armor::isToggled);
    private final ButtonSetting clean = new ButtonSetting("Clean", false);
    private final SliderSetting minCleanDelay = new SliderSetting("Min clean delay", 100, 0, 500, 10, "ms", clean::isToggled);
    private final SliderSetting maxCleanDelay = new SliderSetting("Max clean delay", 150, 0, 500, 10, "ms", clean::isToggled);
    private final ButtonSetting sort = new ButtonSetting("Sort", false);
    private final SliderSetting minSortDelay = new SliderSetting("Min sort delay", 100, 0, 500, 10, "ms", sort::isToggled);
    private final SliderSetting maxSortDelay = new SliderSetting("Max sort delay", 100, 0, 500, 10, "ms", sort::isToggled);
    private final SliderSetting swordSlot = new SliderSetting("Sword slot", 1, 0, 9, 1, sort::isToggled);
    private final SliderSetting blockSlot = new SliderSetting("Block slot", 2, 0, 9, 1, sort::isToggled);
    private final SliderSetting enderPearlSlot = new SliderSetting("Ender pearl slot", 3, 0, 9, 1, sort::isToggled);
    private final SliderSetting bowSlot = new SliderSetting("Bow slot", 4, 0, 9, 1, sort::isToggled);
    private final SliderSetting foodSlot = new SliderSetting("Food slot", 5, 0, 9, 1, sort::isToggled);
    private final SliderSetting throwableSlot = new SliderSetting("Throwable slot", 6, 0, 9, 1, sort::isToggled);
    private final ButtonSetting shuffle = new ButtonSetting("Shuffle", false, () -> armor.isToggled() || clean.isToggled() || sort.isToggled());

    private State state = State.NONE;
    private long nextTaskTime;
    private boolean invOpen = false;

    public InvManager() {
        super("InvManager", category.player);
        this.registerSetting(
                mode, notWhileMoving, minStartDelay, maxStartDelay,
                armor, minArmorDelay, maxArmorDelay,
                clean, minCleanDelay, maxCleanDelay,
                sort, minSortDelay, maxSortDelay, swordSlot, blockSlot, enderPearlSlot, bowSlot, foodSlot, throwableSlot,
                shuffle
        );
    }

    @Override
    public void guiUpdate() {
        Utils.correctValue(minStartDelay, maxStartDelay);
        Utils.correctValue(minArmorDelay, maxArmorDelay);
        Utils.correctValue(minCleanDelay, maxCleanDelay);
        Utils.correctValue(minSortDelay, maxSortDelay);
    }

    @Override
    public void onDisable() {
        if (invOpen && mode.getInput() == 0) {
            PacketUtils.sendPacket(new C0DPacketCloseWindow());
        }
        state = State.NONE;
        invOpen = false;
    }

    @Override
    public void onUpdate() {
        switch ((int) mode.getInput()) {
            case 0:
                invOpen = mc.currentScreen == null && !(notWhileMoving.isToggled() && MoveUtil.isMoving());
                break;
            case 1:
                invOpen = mc.currentScreen instanceof GuiInventory;
                break;
        }

        if (invOpen) {
            if (state == State.NONE) {
                state = State.BEFORE;
                int delay = Utils.randomizeInt(minStartDelay.getInput(), maxStartDelay.getInput());
                nextTaskTime = System.currentTimeMillis();
                if (delay == 0) {
                    state = State.TASKING;
                } else {
                    Raven.getExecutor().schedule(
                            () -> state = State.TASKING,
                            delay,
                            TimeUnit.MILLISECONDS
                    );
                }
            }
        } else {
            state = State.NONE;
        }
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (state != State.TASKING) return;

        armor:
        while (armor.isToggled() && System.currentTimeMillis() >= nextTaskTime) {
            final IInventory inventory = mc.thePlayer.inventory;

            List<Integer> armorTypes = new ArrayList<>(ContainerUtils.ARMOR_TYPES);

            if (shuffle.isToggled())
                Collections.shuffle(armorTypes);

            for (int i : armorTypes) {
                final int curArmorSlot = i + 5;
                final int bestArmorSlot = ContainerUtils.getBestArmor(i, null);
                if (bestArmorSlot != -1 && bestArmorSlot != curArmorSlot) {
                    if (ContainerUtils.getItemStack(curArmorSlot) != null) {
                        ContainerUtils.drop(curArmorSlot);
                    } else {
                        ContainerUtils.click(bestArmorSlot);
                    }
                    nextTaskTime = System.currentTimeMillis() + Utils.randomizeInt(minArmorDelay.getInput(), maxArmorDelay.getInput());
                    continue armor;
                }
            }
            break;
        }

        clean:
        while (clean.isToggled() && System.currentTimeMillis() >= nextTaskTime) {
            final IInventory inventory = mc.thePlayer.inventory;

            final List<Pair<Integer, ItemStack>> slots = getDropSlots(inventory);

            if (!slots.isEmpty()) {
                for (Pair<Integer, ItemStack> slot : slots) {
                    ContainerUtils.drop(slot.first());
                    nextTaskTime = System.currentTimeMillis() + Utils.randomizeInt(minCleanDelay.getInput(), maxCleanDelay.getInput());
                    continue clean;
                }
            }
            break;
        }

        // sort
        if (sort.isToggled()) {
            for (Runnable task : getSortTasks()) {
                task.run();
                if (System.currentTimeMillis() < nextTaskTime) break;
            }
        }
    }

    private void sort(int from, int to) {
        if (to == 0 || from == -1 || to == -1) return;
        if (ContainerUtils.sort(from, to)) {
            nextTaskTime = System.currentTimeMillis() + Utils.randomizeInt(minSortDelay.getInput(), maxSortDelay.getInput());
        }
    }

    private @NotNull List<Pair<Integer, ItemStack>> getDropSlots(@NotNull IInventory inventory) {
        final List<Pair<Integer, ItemStack>> result = new ArrayList<>(inventory.getSizeInventory());

        for (int i = 5; i < 45; i++) {
            final ItemStack stack = ContainerUtils.getItemStack(i);
            if (stack == null || stack.getItem() instanceof ItemEmptyMap)
                continue;
            if (!ContainerUtils.canDrop(stack, i, null))
                continue;
            result.add(new Pair<>(i, stack));
        }

        if (shuffle.isToggled())
            Collections.shuffle(result);

        return result;
    }

    private @NotNull List<Runnable> getSortTasks() {
        final List<Runnable> result = new ArrayList<>();

        result.add(() -> sort(ContainerUtils.getBestSword(null, (int) swordSlot.getInput()), (int) swordSlot.getInput()));
        result.add(() -> sort(ContainerUtils.getMostBlocks((int) blockSlot.getInput()), (int) blockSlot.getInput()));
        result.add(() -> sort(ContainerUtils.getBiggestStack(Items.ender_pearl, (int) enderPearlSlot.getInput()), (int) enderPearlSlot.getInput()));
        result.add(() -> sort(ContainerUtils.getBestBow(null), (int) bowSlot.getInput()));
        result.add(() -> sort(ContainerUtils.getBestFood((int) foodSlot.getInput()), (int) foodSlot.getInput()));
        result.add(() -> sort(ContainerUtils.getMostProjectiles((int) throwableSlot.getInput()), (int) throwableSlot.getInput()));

        return result;
    }

    enum State {
        NONE,
        BEFORE,
        TASKING
    }

    @Override
    public String getInfo() {
        return mode.getOptions()[(int) mode.getInput()];
    }
}