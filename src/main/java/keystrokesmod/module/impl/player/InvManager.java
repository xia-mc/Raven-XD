package keystrokesmod.module.impl.player;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.block.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import org.lwjgl.input.Mouse;

import java.util.Arrays;
import java.util.List;

public class InvManager extends Module {
    private ButtonSetting autoArmor;
    private SliderSetting autoArmorDelay;
    private ButtonSetting autoSort;
    private SliderSetting sortDelay;
    private ButtonSetting stealChests;
    private ButtonSetting customChest;
    private ButtonSetting autoClose;
    private SliderSetting stealerDelay;
    private ButtonSetting inventoryCleaner;
    private ButtonSetting middleClickToClean;
    private SliderSetting cleanerDelay;
    private SliderSetting swordSlot;
    private SliderSetting blocksSlot;
    private SliderSetting goldenAppleSlot;
    private SliderSetting projectileSlot;
    private SliderSetting speedPotionSlot;
    private SliderSetting pearlSlot;
    private String[] ignoreItems = {"stick", "flesh", "string", "cake", "mushroom", "flint", "compass", "dyePowder", "feather", "shears", "anvil", "torch", "seeds", "leather", "skull", "record"};
    private int lastStole;
    private int lastSort;
    private int lastArmor;
    private int lastClean;

    public InvManager() {
        super("InvManager", category.player);
        this.registerSetting(autoArmor = new ButtonSetting("Auto armor", false));
        this.registerSetting(autoArmorDelay = new SliderSetting("Auto armor delay", 3, 1, 20, 1));
        this.registerSetting(autoSort = new ButtonSetting("Auto sort", false));
        this.registerSetting(sortDelay = new SliderSetting("Sort delay", 3, 1, 20, 1));
        this.registerSetting(stealChests = new ButtonSetting("Steal chests", false));
        this.registerSetting(customChest = new ButtonSetting("Custom chest", false));
        this.registerSetting(autoClose = new ButtonSetting("Close after stealing", false));
        this.registerSetting(stealerDelay = new SliderSetting("Stealer delay", 3, 1, 20, 1));
        this.registerSetting(inventoryCleaner = new ButtonSetting("Inventory cleaner", false));
        this.registerSetting(middleClickToClean = new ButtonSetting("Middle click to clean", false));
        this.registerSetting(cleanerDelay = new SliderSetting("Cleaner delay", 5, 1, 20, 1));
        this.registerSetting(swordSlot = new SliderSetting("Sword slot", 0, 0, 9, 1));
        this.registerSetting(blocksSlot = new SliderSetting("Blocks slot", 0, 0, 9, 1));
        this.registerSetting(goldenAppleSlot = new SliderSetting("Golden apple slot", 0, 0, 9, 1));
        this.registerSetting(projectileSlot = new SliderSetting("Projectile slot", 0, 0, 9, 1));
        this.registerSetting(speedPotionSlot = new SliderSetting("Speed potion slot", 0, 0, 9, 1));
        this.registerSetting(pearlSlot = new SliderSetting("Pearl slot", 0, 0, 9, 1));
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
                    if (sort(getBestPotion((int) speedPotionSlot.getInput(), null), (int) speedPotionSlot.getInput())) {
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
                            if (getBestPotion((int) speedPotionSlot.getInput(), inventory) != i) {
                                mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                            }
                            else {
                                mc.playerController.windowClick(chest.windowId, i, (int) (speedPotionSlot.getInput() - 1), 2, mc.thePlayer);
                            }
                        }
                        lastStole = 0;
                    }
                    stolen = true;
                }
                else if (item.getItem() instanceof ItemTool) {
                    if (lastStole++ >= stealerDelay.getInput()) {
                        if (getBestTool(item, inventory) != i) {
                            continue;
                        }
                        if (lastStole++ >= stealerDelay.getInput()) {
                            mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                            lastStole = 0;
                        }
                    }
                    stolen = true;
                }
                else if (item.getItem() instanceof ItemBow) {
                    if (lastStole++ >= stealerDelay.getInput()) {
                        if (getBestBow(inventory) != i) {
                            continue;
                        }
                        if (lastStole++ >= stealerDelay.getInput()) {
                            mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                            lastStole = 0;
                        }
                    }
                    stolen = true;
                }
                else if (item.getItem() instanceof ItemFishingRod) {
                    if (lastStole++ >= stealerDelay.getInput()) {
                        if (getBestRod(inventory) != i) {
                            continue;
                        }
                        if (lastStole++ >= stealerDelay.getInput()) {
                            mc.playerController.windowClick(chest.windowId, i, 0, 1, mc.thePlayer);
                            lastStole = 0;
                        }
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

    private int getBestSword(IInventory inventory, int desiredSlot) {
        int bestSword = -1;
        double lastDamage = -1;
        double damageInSlot = -1;
        if (desiredSlot != -1) {
            ItemStack itemStackInSlot = getItemStack(desiredSlot + 35);
            if (itemStackInSlot != null && itemStackInSlot.getItem() instanceof ItemSword) {
                damageInSlot = Utils.getDamage(itemStackInSlot);
            }
        }
        for (int i = 9; i < 45; i++) {
            ItemStack item = getItemStack(i);
            if (item == null || !(item.getItem() instanceof ItemSword)) {
                continue;
            }
            double damage = Utils.getDamage(item);
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
                double damage = Utils.getDamage(item);
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
        double lastProtection = -1;
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

    private boolean dropPotion(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof ItemPotion) {
            ItemPotion potion = (ItemPotion) stack.getItem();
            if (potion.getEffects(stack) == null) {
                return true;
            }
            for (PotionEffect effect : potion.getEffects(stack)) {
                if (effect.getPotionID() == Potion.moveSlowdown.getId() || effect.getPotionID() == Potion.weakness.getId() || effect.getPotionID() == Potion.poison.getId() || effect.getPotionID() == Potion.harm.getId()) {
                    return true;
                }
            }
        }
        return false;
    }

    private int getBestBow(IInventory inventory) {
        int bestBow = -1;
        double lastPower = -1;
        for (int i = 5; i < 45; i++) {
            ItemStack item = getItemStack(i);
            if (item == null || !(item.getItem() instanceof ItemBow)) {
                continue;
            }
            double protection = getPower(item);
            if (protection > lastPower) {
                lastPower = protection;
                bestBow = i;
            }
        }
        if (inventory != null) {
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                ItemStack item = inventory.getStackInSlot(i);
                if (item == null || !(item.getItem() instanceof ItemBow)) {
                    continue;
                }
                double power = getPower(item);
                if (power > lastPower) {
                    lastPower = power;
                    bestBow = i;
                }
            }
        }
        return bestBow;
    }

    private float getPower(ItemStack stack) {
        float score = 0;
        Item item = stack.getItem();
        if (item instanceof ItemBow) {
            score += EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
            score += EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack) * 0.5;
            score += EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) * 0.1;
        }
        return score;
    }

    private int getBestRod(IInventory inventory) {
        int bestRod = -1;
        double lastKnocback = -1;
        for (int i = 5; i < 45; i++) {
            ItemStack item = getItemStack(i);
            if (item == null || !(item.getItem() instanceof ItemFishingRod)) {
                continue;
            }
            double knockback = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, item);
            if (knockback > lastKnocback) {
                lastKnocback = knockback;
                bestRod = i;
            }
        }
        if (inventory != null) {
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                ItemStack item = inventory.getStackInSlot(i);
                if (item == null || !(item.getItem() instanceof ItemFishingRod)) {
                    continue;
                }
                double knockback = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, item);
                if (knockback > lastKnocback) {
                    lastKnocback = knockback;
                    bestRod = i;
                }
            }
        }
        return bestRod;
    }

    private int getBestTool(ItemStack itemStack, IInventory inventory) {
        int bestTool = -1;
        double lastEfficiency = -1;
        Block blockType = Blocks.dirt;
        if (itemStack.getItem() instanceof ItemAxe) {
            blockType = Blocks.log;
        }
        else if (itemStack.getItem() instanceof ItemPickaxe) {
            blockType = Blocks.stone;
        }
        for (int i = 5; i < 45; i++) {
            ItemStack item = getItemStack(i);
            if (item == null || !(item.getItem() instanceof ItemTool) || item.getItem() != itemStack.getItem()) {
                continue;
            }
            double efficiency = Utils.getEfficiency(item, blockType);
            if (efficiency > lastEfficiency) {
                lastEfficiency = efficiency;
                bestTool = i;
            }
        }
        if (inventory != null) {
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                ItemStack item = inventory.getStackInSlot(i);
                if (item == null || !(item.getItem() instanceof ItemTool) || item.getItem() != itemStack.getItem()) {
                    continue;
                }
                double efficiency = Utils.getEfficiency(item, blockType);;
                if (efficiency > lastEfficiency) {
                    lastEfficiency = efficiency;
                    bestTool = i;
                }
            }
        }
        return bestTool;
    }

    private int getBestPotion(int desiredSlot, IInventory inventory) {
        int amplifier = -1;
        int bestPotion = -1;
        double amplifierInSlot = -1;
        if (amplifierInSlot != -1) {
            ItemStack itemStackInSlot = getItemStack( desiredSlot + 35);
            if (itemStackInSlot != null && itemStackInSlot.getItem() instanceof ItemPotion) {
                amplifierInSlot = getPotionLevel(itemStackInSlot);
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
                    int score = effect.getAmplifier() + effect.getDuration();
                    if (effect.getEffectName().equals("potion.moveSpeed") && score > amplifier && score > amplifierInSlot) {
                        bestPotion = i;
                        amplifier = score;
                    }
                }
            }
        }
        if (inventory != null) {
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                ItemStack item = inventory.getStackInSlot(i);
                if (item != null && item.getItem() instanceof ItemPotion) {
                    List<PotionEffect> list = ((ItemPotion) item.getItem()).getEffects(item);
                    if (list == null) {
                        continue;
                    }
                    for (PotionEffect effect : list) {
                        if (effect.getEffectName().equals("potion.moveSpeed") && effect.getAmplifier() > amplifier && effect.getAmplifier() > amplifierInSlot) {
                            bestPotion = i;
                            amplifier = effect.getAmplifier();
                        }
                    }
                }
            }
        }
        return bestPotion;
    }

    private int getPotionLevel(ItemStack item) {
        List<PotionEffect> list = ((ItemPotion) item.getItem()).getEffects(item);
        if (list == null) {
            return -1;
        }
        for (PotionEffect effect : list) {
            if (effect.getEffectName().equals("potion.moveSpeed")) {
                return effect.getAmplifier() + effect.getDuration();
            }
        }
        return -1;
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
        if (dropPotion(itemStack)) {
            return true;
        }
        if (itemStack.getItem() instanceof ItemSword && getBestSword(null, (int) swordSlot.getInput()) != slot) {
            return true;
        }
        if (itemStack.getItem() instanceof ItemArmor && getBestArmor(((ItemArmor) itemStack.getItem()).armorType, null) != slot) {
            return true;
        }
        if (itemStack.getItem() instanceof ItemTool && getBestTool(itemStack, null) != slot) {
            return true;
        }
        if (itemStack.getItem() instanceof ItemBow && getBestBow(null) != slot) {
            return true;
        }
        if (itemStack.getItem() instanceof ItemFishingRod && getBestRod(null) != slot) {
            return true;
        }
        return false;
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
        if (stackInSlot >= biggestEggSlot && stackInSlot >=  biggestSnowballSlot) {
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
        if (item == null) {
            return null;
        }
        return item;
    }

    public static boolean canBePlaced(ItemBlock itemBlock) {
        Block block = itemBlock.getBlock();
        if (block == null) {
            return false;
        }
        if (BlockUtils.isInteractable(block) || block instanceof BlockSkull || block instanceof BlockLiquid || block instanceof BlockCactus || block instanceof BlockCarpet || block instanceof BlockTripWire || block instanceof BlockTripWireHook || block instanceof BlockTallGrass || block instanceof BlockFlower || block instanceof BlockFlowerPot || block instanceof BlockSign || block instanceof BlockLadder || block instanceof BlockTorch || block instanceof BlockRedstoneTorch || block instanceof BlockFence || block instanceof BlockPane || block instanceof BlockStainedGlassPane || block instanceof BlockGravel || block instanceof BlockClay || block instanceof BlockSand || block instanceof BlockSoulSand) {
            return false;
        }
        return true;
    }
}