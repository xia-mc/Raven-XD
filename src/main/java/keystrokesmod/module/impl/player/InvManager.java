package keystrokesmod.module.impl.player;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.block.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.*;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import org.lwjgl.input.Mouse;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class InvManager extends Module {
    private final ButtonSetting autoArmor;
    private final SliderSetting autoArmorDelay;
    private final ButtonSetting autoSort;
    private final SliderSetting sortDelay;
    private final ButtonSetting stealChests;
    private final ButtonSetting customChest;
    private final ButtonSetting autoClose;
    private final SliderSetting stealerDelay;
    private final ButtonSetting inventoryCleaner;
    private final ButtonSetting middleClickToClean;
    private final SliderSetting cleanerDelay;
    private final SliderSetting swordSlot;
    private final SliderSetting blocksSlot;
    private final SliderSetting goldenAppleSlot;
    private final SliderSetting projectileSlot;
    private final SliderSetting speedPotionSlot;
    private final SliderSetting pearlSlot;
    private final String[] ignoreItems = {"stick", "flesh", "string", "cake", "mushroom", "flint", "compass", "dyePowder", "feather", "shears", "anvil", "torch", "seeds", "leather", "skull", "record"};
    private int lastStole;
    private int lastSort;
    private int lastArmor;
    private int lastClean;

    public InvManager() {
        super("InvManager", category.player);
        this.registerSetting(autoArmor = new ButtonSetting("Auto armor", false));
        this.registerSetting(autoArmorDelay = new SliderSetting("Auto armor delay", 3.0, 1.0, 20.0, 1.0));
        this.registerSetting(autoSort = new ButtonSetting("Auto sort", false));
        this.registerSetting(sortDelay = new SliderSetting("Sort delay", 3.0, 1.0, 20.0, 1.0));
        this.registerSetting(stealChests = new ButtonSetting("Steal chests", false));
        this.registerSetting(customChest = new ButtonSetting("Custom chest", false));
        this.registerSetting(autoClose = new ButtonSetting("Close after stealing", false));
        this.registerSetting(stealerDelay = new SliderSetting("Stealer delay", 3.0, 1.0, 20.0, 1.0));
        this.registerSetting(inventoryCleaner = new ButtonSetting("Inventory cleaner", false));
        this.registerSetting(middleClickToClean = new ButtonSetting("Middle click to clean", false));
        this.registerSetting(cleanerDelay = new SliderSetting("Cleaner delay", 5.0, 1.0, 20.0, 1.0));
        this.registerSetting(swordSlot = new SliderSetting("Sword slot", 0.0, 0.0, 9.0, 1.0));
        this.registerSetting(blocksSlot = new SliderSetting("Blocks slot", 0.0, 0.0, 9.0, 1.0));
        this.registerSetting(goldenAppleSlot = new SliderSetting("Golden apple slot", 0.0, 0.0, 9.0, 1.0));
        this.registerSetting(projectileSlot = new SliderSetting("Projectile slot", 0.0, 0.0, 9.0, 1.0));
        this.registerSetting(speedPotionSlot = new SliderSetting("Speed potion slot", 0.0, 0.0, 9.0, 1.0));
        this.registerSetting(pearlSlot = new SliderSetting("Pearl slot", 0.0, 0.0, 9.0, 1.0));
    }

    public void onEnable() {
        resetDelay();
    }

    public void onUpdate() {
        if (Utils.inInventory()) {
            if (autoArmor.isToggled() && lastArmor++ >= autoArmorDelay.getInput()) {
                for (int i = 0; i < 4; i++) {
                    int bestSlot = getBestArmor(i, null);
                    if (bestSlot == i + 5) {
                        continue;
                    }
                    if (bestSlot != -1) {
                        if (getItemStack(i + 5) != null) {
                            drop(i + 5);
                        } else {
                            click(bestSlot, 0, true);
                            lastArmor = 0;
                        }
                        return;
                    }
                }
            }
            if (autoSort.isToggled() && lastSort++ >= sortDelay.getInput()) {
                if (swordSlot.getInput() != 0) {
                    if (sort(getBestSword(null, (int) swordSlot.getInput()), (int) swordSlot.getInput())) {
                        lastSort = 0;
                        return;
                    }
                }
                if (blocksSlot.getInput() != 0) {
                    if (sort(getMostBlocks(), (int) blocksSlot.getInput())) {
                        lastSort = 0;
                        return;
                    }
                }
                if (goldenAppleSlot.getInput() != 0) {
                    if (sort(getBiggestStack(Items.golden_apple, (int) goldenAppleSlot.getInput()), (int) goldenAppleSlot.getInput())) {
                        lastSort = 0;
                        return;
                    }
                }
                if (projectileSlot.getInput() != 0) {
                    if (sort(getMostProjectiles((int) projectileSlot.getInput()), (int) projectileSlot.getInput())) {
                        lastSort = 0;
                        return;
                    }
                }
                if (speedPotionSlot.getInput() != 0) {
                    if (sort(getBestPotion((int) speedPotionSlot.getInput()), (int) speedPotionSlot.getInput())) {
                        lastSort = 0;
                        return;
                    }
                }
                if (pearlSlot.getInput() != 0) {
                    if (sort(getBiggestStack(Items.ender_pearl, (int) pearlSlot.getInput()), (int) pearlSlot.getInput())) {
                        lastSort = 0;
                        return;
                    }
                }
            }
            if (inventoryCleaner.isToggled()) {
                if (middleClickToClean.isToggled() && !Mouse.isButtonDown(2)) {
                    return;
                }
                if (lastClean++ >= cleanerDelay.getInput()) {
                    for (int i = 5; i < 45; i++) {
                        ItemStack stack = getItemStack(i);
                        if (stack == null) {
                            continue;
                        }
                        if (!canDrop(stack, i)) {
                            continue;
                        }
                        drop(i);
                        lastClean = 0;
                        break;
                    }
                }
            }
        }
        else if (stealChests.isToggled() && mc.thePlayer.openContainer instanceof ContainerChest) {
            ContainerChest chest = (ContainerChest) mc.thePlayer.openContainer;
            if (chest == null || inventoryFull()) {
                autoClose();
                return;
            }
            String name = chest.getLowerChestInventory().getName();
            if (!customChest.isToggled() && !name.equals("Chest") && !name.equals("Ender Chest") && !name.equals("Large Chest")) {
                return;
            }
            boolean notEmpty = false;
            boolean stolen = false;
            int size = chest.getLowerChestInventory().getSizeInventory();
            for (int i = 0; i < size; i++) {
                ItemStack item = chest.getLowerChestInventory().getStackInSlot(i);
                if (item == null) {
                    continue;
                }
                if (Arrays.stream(ignoreItems).anyMatch(item.getUnlocalizedName().toLowerCase()::contains)) {
                    continue;
                }
                IInventory inventory = chest.getLowerChestInventory();
                notEmpty = true;
                if (item.getItem() instanceof ItemSword) {
                    if (getBestSword(inventory, (int) swordSlot.getInput()) != i) {
                        continue;
                    }
                    if (lastStole++ >= stealerDelay.getInput()) {
                        if (swordSlot.getInput() != 0) {
                            mc.playerController.windowClick(chest.windowId, i, (int) swordSlot.getInput() - 1, 2, mc.thePlayer);
                        }
                        else {
                            mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                        }
                        lastStole = 0;
                    }
                    stolen = true;
                }
                else if (item.getItem() instanceof ItemBlock) {
                    if (!canBePlaced((ItemBlock) item.getItem())) {
                        continue;
                    }
                    if (lastStole++ >= stealerDelay.getInput()) {
                        mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                        lastStole = 0;
                    }
                    stolen = true;
                }
                else if (item.getItem() instanceof ItemAppleGold) {
                    if (lastStole++ >= stealerDelay.getInput()) {
                        mc.playerController.windowClick(chest.windowId, i, (int) (goldenAppleSlot.getInput() - 1), 2, mc.thePlayer);
                        lastStole = 0;
                    }
                    stolen = true;
                }
                else if (item.getItem() instanceof ItemSnowball || item.getItem() instanceof ItemEgg) {
                    if (lastStole++ >= stealerDelay.getInput()) {
                        mc.playerController.windowClick(chest.windowId, i, (int) (projectileSlot.getInput() - 1), 2, mc.thePlayer);
                        lastStole = 0;
                    }
                    stolen = true;
                }
                else if (item.getItem() instanceof ItemEnderPearl) {
                    if (lastStole++ >= stealerDelay.getInput()) {
                        mc.playerController.windowClick(chest.windowId, i, (int) (pearlSlot.getInput() - 1), 2, mc.thePlayer);
                        lastStole = 0;
                    }
                    stolen = true;
                }
                else if (item.getItem() instanceof ItemArmor) {
                    if (getBestArmor(((ItemArmor) item.getItem()).armorType, inventory) != i) {
                        continue;
                    }
                    if (lastStole++ >= stealerDelay.getInput()) {
                        mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                        lastStole = 0;
                    }
                    stolen = true;
                }
                else if (item.getItem() instanceof ItemPotion) {
                    if (lastStole++ >= stealerDelay.getInput()) {
                        if (!isSpeedPot(item)) {
                            mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                        } else {
                            mc.playerController.windowClick(chest.windowId, i, (int) (speedPotionSlot.getInput() - 1), 2, mc.thePlayer);
                        }
                        lastStole = 0;
                    }
                    stolen = true;
                }
                else {
                    if (lastStole++ >= stealerDelay.getInput()) {
                        mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                        lastStole = 0;
                    }
                    stolen = true;
                }
            }

            if (inventoryFull() || !notEmpty || !stolen) {
                autoClose();
            }
        }
        else {
            resetDelay();
        }
    }

    private int getProtection(final ItemStack itemStack) {
        return ((ItemArmor)itemStack.getItem()).damageReduceAmount + EnchantmentHelper.getEnchantmentModifierDamage(new ItemStack[] { itemStack }, DamageSource.generic);
    }

    private void click(int slot, int mouseButton, boolean shiftClick) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, mouseButton, shiftClick ? 1 : 0, mc.thePlayer);
    }

    private boolean sort(int bestSlot, int desiredSlot) {
        if (bestSlot != -1 && bestSlot != desiredSlot + 35) {
            swap(bestSlot, desiredSlot - 1);
            return true;
        }
        return false;
    }

    private void drop(int slot) {
        mc.playerController.windowClick(0, slot, 1, 4, mc.thePlayer);
    }

    private void swap(int slot, int hSlot) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, hSlot, 2, mc.thePlayer);
    }

    private boolean isSpeedPot(ItemStack item) {
        List<PotionEffect> list = ((ItemPotion) item.getItem()).getEffects(item);
        if (list == null) {
            return false;
        }
        for (PotionEffect effect : list) {
            if (effect.getEffectName().equals("potion.moveSpeed")) {
                return true;
            }
        }
        return false;
    }
    private boolean inventoryFull() {
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getStack() == null) {
                return false;
            }
        }
        return true;
    }

    private void resetDelay() {
        lastStole = lastArmor = lastClean = lastSort = 0;
    }

    private void autoClose() {
        if (autoClose.isToggled()) {
            mc.thePlayer.closeScreen();
        }
    }

    public double getDamage(final ItemStack itemStack) {
        double getAmount = 0.0;
        for (final Map.Entry<String, AttributeModifier> entry : itemStack.getAttributeModifiers().entries()) {
            if (entry.getKey().equals("generic.attackDamage")) {
                getAmount = entry.getValue().getAmount();
                break;
            }
        }
        return getAmount + EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, itemStack) * 1.25;
    }

    private int getBestSword(IInventory inventory, int desiredSlot) {
        int bestSword = -1;
        double lastDamage = 0;
        double damageInSlot = 0;
        if (desiredSlot != -1) {
            ItemStack itemStackInSlot = getItemStack(desiredSlot + 35);
            if (itemStackInSlot != null) {
                damageInSlot = getDamage(itemStackInSlot);
            }
        }
        for (int i = 9; i < 45; i++) {
            ItemStack item = getItemStack(i);
            if (item == null || !(item.getItem() instanceof ItemSword)) {
                continue;
            }
            double damage = getDamage(item);
            if (damage > lastDamage && damage > damageInSlot) {
                lastDamage = damage;
                bestSword = i;
            }
        }
        if (inventory != null) {
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                ItemStack item = inventory.getStackInSlot(i);
                if (item == null || !(item.getItem() instanceof ItemSword)) {
                    continue;
                }
                double damage = getDamage(item);
                if (damage > lastDamage && damage > damageInSlot) {
                    lastDamage = damage;
                    bestSword = i;
                }
            }
        }
        if (bestSword == -1) {
            bestSword = desiredSlot + 35;
        }
        return bestSword;
    }

    private int getBestArmor(int armorType, IInventory inventory) {
        int bestArmor = -1;
        double lastProtection = 0;
        for (int i = 5; i < 45; i++) {
            ItemStack item = getItemStack(i);
            if (item == null || !(item.getItem() instanceof ItemArmor) || !(((ItemArmor) item.getItem()).armorType == armorType)) {
                continue;
            }
            double protection = getProtection(item);
            if (protection > lastProtection) {
                lastProtection = protection;
                bestArmor = i;
            }
        }
        if (inventory != null) {
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                ItemStack item = inventory.getStackInSlot(i);
                if (item == null || !(item.getItem() instanceof ItemArmor) || !(((ItemArmor) item.getItem()).armorType == armorType)) {
                    continue;
                }
                double protection = getProtection(item);
                if (protection > lastProtection) {
                    lastProtection = protection;
                    bestArmor = i;
                }
            }
        }
        return bestArmor;
    }

    private int getBestPotion(int desiredSlot) {
        int amplifier = 0;
        int bestPotion = -1;
        double amplifierInSlot = 0;
        if (amplifierInSlot != -1) {
            ItemStack itemStackInSlot = getItemStack( desiredSlot + 35);
            if (itemStackInSlot != null) {
                amplifierInSlot = getDamage(itemStackInSlot);
            }
        }
        for (int i = 9; i < 45; i++) {
            ItemStack item = getItemStack(i);
            if (item != null && item.getItem() instanceof ItemPotion) {
                List<PotionEffect> list = ((ItemPotion) item.getItem()).getEffects(item);
                if (list == null) {
                    continue;
                }
                for (PotionEffect effect : list) {
                    if (effect.getEffectName().equals("effect.speed") && effect.getAmplifier() > amplifier && effect.getAmplifier() > amplifierInSlot) {
                        bestPotion = i;
                        amplifier = effect.getAmplifier();
                    }
                }
            }
        }
        return bestPotion;
    }

    private int getBiggestStack(Item targetItem, int desiredSlot) {
        int stack = 0;
        int biggestSlot = -1;
        int stackInSlot = -1;
        if (desiredSlot != -1) {
            ItemStack itemStackInSlot = getItemStack(desiredSlot + 35);
            if (itemStackInSlot != null) {
                stackInSlot = itemStackInSlot.stackSize;
            }
        }
        for (int i = 9; i < 45; i++) {
            ItemStack item = getItemStack(i);
            if (item != null && item.getItem() == targetItem && item.stackSize > stack && item.stackSize > stackInSlot) {
                stack = item.stackSize;
                biggestSlot = i;
            }
        }
        return biggestSlot;
    }

    private boolean canDrop(ItemStack itemStack, int slot) {
        if (Arrays.stream(ignoreItems).anyMatch(itemStack.getUnlocalizedName().toLowerCase()::contains)) {
            return true;
        }
        if (itemStack.getItem() instanceof ItemSword && getBestSword(null, (int) swordSlot.getInput()) != slot) {
            return true;
        }
        return itemStack.getItem() instanceof ItemArmor && getBestArmor(((ItemArmor) itemStack.getItem()).armorType, null) != slot;
    }

    private int getMostProjectiles(int desiredSlot) {
        int biggestSnowballSlot = getBiggestStack(Items.snowball, (int) projectileSlot.getInput());
        int biggestEggSlot = getBiggestStack(Items.egg, (int) projectileSlot.getInput());
        int biggestSlot = -1;
        int stackInSlot = 0;
        if (desiredSlot != -1) {
            ItemStack itemStackInSlot = getItemStack(desiredSlot + 35);
            if (itemStackInSlot != null && (itemStackInSlot.getItem() instanceof ItemEgg || itemStackInSlot.getItem() instanceof ItemSnowball)) {
                stackInSlot = itemStackInSlot.stackSize;
            }
        }
        if (stackInSlot > biggestEggSlot && stackInSlot > biggestSnowballSlot) {
            return -1;
        }
        if (biggestEggSlot > biggestSnowballSlot) {
            biggestSlot = biggestEggSlot;
        }
        else if (biggestSnowballSlot > biggestEggSlot) {
            biggestSlot = biggestSnowballSlot;
        }
        else if (biggestSnowballSlot != -1 && biggestEggSlot != -1 && biggestEggSlot == biggestSnowballSlot) {
            biggestSlot = biggestSnowballSlot;
        }
        return biggestSlot;
    }

    private int getMostBlocks() {
        int stack = 0;
        int biggestSlot = -1;
        ItemStack itemStackInSlot = getItemStack((int) (blocksSlot.getInput() + 35));
        int stackInSlot = 0;
        if (itemStackInSlot != null) {
            stackInSlot = itemStackInSlot.stackSize;
        }
        for (int i = 9; i < 45; i++) {
            ItemStack item = getItemStack(i);
            if (item != null && item.getItem() instanceof ItemBlock && item.stackSize > stack && canBePlaced((ItemBlock) item.getItem()) && item.stackSize > stackInSlot) {
                stack = item.stackSize;
                biggestSlot = i;
            }
        }
        return biggestSlot;
    }

    private ItemStack getItemStack(int i) {
        Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
        if (slot == null) {
            return null;
        }
        ItemStack item = slot.getStack();
        return item;
    }

    public static boolean canBePlaced(ItemBlock itemBlock) {
        Block block = itemBlock.getBlock();
        if (block == null) {
            return false;
        }
        return !BlockUtils.isInteractable(block) && !(block instanceof BlockSkull) && !(block instanceof BlockLiquid) && !(block instanceof BlockCactus) && !(block instanceof BlockCarpet) && !(block instanceof BlockTripWire) && !(block instanceof BlockTripWireHook) && !(block instanceof BlockTallGrass) && !(block instanceof BlockFlower) && !(block instanceof BlockFlowerPot) && !(block instanceof BlockSign) && !(block instanceof BlockLadder) && !(block instanceof BlockTorch) && !(block instanceof BlockRedstoneTorch) && !(block instanceof BlockFence) && !(block instanceof BlockPane) && !(block instanceof BlockStainedGlassPane) && !(block instanceof BlockGravel) && !(block instanceof BlockClay) && !(block instanceof BlockSand) && !(block instanceof BlockSoulSand);
    }
}
