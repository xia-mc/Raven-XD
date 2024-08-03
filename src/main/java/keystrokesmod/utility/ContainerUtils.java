package keystrokesmod.utility;

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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

import static keystrokesmod.Raven.mc;

public class ContainerUtils {
    public static final Set<String> IGNORE_ITEMS = new HashSet<>(Arrays.asList("stick", "flesh", "string", "cake", "mushroom", "flint", "compass", "dyePowder", "feather", "shears", "anvil", "torch", "seeds", "leather", "skull", "record"));
    public static final List<Integer> ARMOR_TYPES = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
    public static final Set<Item> THROWABLES = new HashSet<>(Arrays.asList(Items.snowball, Items.egg));

    public static boolean canBePlaced(ItemBlock itemBlock) {
        Block block = itemBlock.getBlock();
        if (block == null) {
            return false;
        }
        return !BlockUtils.isInteractable(block) && !(block instanceof BlockTNT) && !(block instanceof BlockWeb) && !(block instanceof BlockLever) && !(block instanceof BlockButton) && !(block instanceof BlockSkull) && !(block instanceof BlockLiquid) && !(block instanceof BlockCactus) && !(block instanceof BlockCarpet) && !(block instanceof BlockTripWire) && !(block instanceof BlockTripWireHook) && !(block instanceof BlockTallGrass) && !(block instanceof BlockFlower) && !(block instanceof BlockFlowerPot) && !(block instanceof BlockSign) && !(block instanceof BlockLadder) && !(block instanceof BlockTorch) && !(block instanceof BlockRedstoneTorch) && !(block instanceof BlockFence) && !(block instanceof BlockPane) && !(block instanceof BlockStainedGlassPane) && !(block instanceof BlockGravel) && !(block instanceof BlockClay) && !(block instanceof BlockSand) && !(block instanceof BlockSoulSand);
    }

    public static <T extends Item> int getSlot(Class<T> item, Predicate<T> predicate) {
        int slot = -1;
        int highestStack = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack itemStack = mc.thePlayer.inventory.mainInventory[i];
            if (itemStack != null && item.isInstance(itemStack.getItem()) && predicate.test(item.cast(itemStack.getItem())) && itemStack.stackSize > 0) {
                if (mc.thePlayer.inventory.mainInventory[i].stackSize > highestStack) {
                    highestStack = mc.thePlayer.inventory.mainInventory[i].stackSize;
                    slot = i;
                }
            }
        }
        return slot;
    }

    public static int getSlot(Class<? extends Item> item) {
        return getSlot(item, t -> true);
    }

    public static boolean isRest(Item item) {
        return item instanceof ItemFood || item instanceof ItemPotion;
    }

    public static int getBestSword(IInventory inventory, int desiredSlot) {
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
        return bestSword;
    }

    public static int getBestArmor(int armorType, IInventory inventory) {
        int bestArmor = -1;
        int bestLevel = -1;
        for (int i = 5; i < 45; i++) {
            ItemStack item = getItemStack(i);
            if (item == null || !(item.getItem() instanceof ItemArmor) || !(((ItemArmor) item.getItem()).armorType == armorType)) {
                continue;
            }
            int level = getArmorLevel(item);
            if (level > bestLevel) {
                bestLevel = level;
                bestArmor = i;
            }
        }
        if (inventory != null) {
            for (int i = 0; i < inventory.getSizeInventory(); i++) {
                ItemStack item = inventory.getStackInSlot(i);
                if (item == null || !(item.getItem() instanceof ItemArmor) || !(((ItemArmor) item.getItem()).armorType == armorType)) {
                    continue;
                }
                int protection = getArmorLevel(item);
                if (protection > bestLevel) {
                    bestLevel = protection;
                    bestArmor = i;
                }
            }
        }
        return bestArmor;
    }

    public static boolean dropPotion(ItemStack stack) {
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

    public static int getBestBow(IInventory inventory) {
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

    public static float getPower(ItemStack stack) {
        float score = 0;
        Item item = stack.getItem();
        if (item instanceof ItemBow) {
            score += EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
            score += EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack) * 0.5;
            score += EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack) * 0.1;
        }
        return score;
    }

    public static int getBestRod(IInventory inventory) {
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

    public static int getBestTool(ItemStack itemStack, IInventory inventory) {
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

    public static int getBestPotion(int desiredSlot, IInventory inventory) {
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

    public static int getPotionLevel(ItemStack item) {
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

    public static int getBiggestStack(Item targetItem, int desiredSlot) {
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

    public static boolean canDrop(@NotNull ItemStack itemStack, int slot, @Nullable IInventory inventory) {
        if (IGNORE_ITEMS.contains(itemStack.getUnlocalizedName().toLowerCase())) {
            return true;
        }
        if (dropPotion(itemStack)) {
            return true;
        }
        if (itemStack.getItem() instanceof ItemSword && getBestSword(inventory, -1) != slot) {
            return true;
        }
        if (itemStack.getItem() instanceof ItemArmor && getBestArmor(((ItemArmor) itemStack.getItem()).armorType, inventory) != slot) {
            return true;
        }
        if (itemStack.getItem() instanceof ItemTool && getBestTool(itemStack, inventory) != slot) {
            return true;
        }
        if (itemStack.getItem() instanceof ItemBow && getBestBow(inventory) != slot) {
            return true;
        }
        if (itemStack.getItem() instanceof ItemFishingRod && getBestRod(inventory) != slot) {
            return true;
        }
        return false;
    }

    public static int getBestFood(int desiredSlot) {
        float foodLevel = 0;
        int slot = -1;

        if (desiredSlot != -1) {
            final ItemStack stack = getItemStack(desiredSlot);
            if (stack != null && stack.getItem() instanceof ItemFood) {
                foodLevel = ((ItemFood) stack.getItem()).getSaturationModifier(stack);
                slot = desiredSlot;
            }
        }

        for (int i = 9; i < 45; i++) {
            final ItemStack stack = getItemStack(i);
            if (stack != null && stack.getItem() instanceof ItemFood) {
                float thisFoodLevel = ((ItemFood) stack.getItem()).getSaturationModifier(stack);
                if (thisFoodLevel > foodLevel + 1) {
                    foodLevel = thisFoodLevel;
                    slot = i;
                }
            }
        }
        return slot;
    }

    public static ItemStack getItemStack(int i) {
        Slot slot = mc.thePlayer.inventoryContainer.getSlot(i);
        if (slot == null) {
            return null;
        }
        return slot.getStack();
    }

    public static boolean isChest(boolean customChest) {
        if (mc.thePlayer.openContainer instanceof ContainerChest) {
            final String name = ((ContainerChest) mc.thePlayer.openContainer).getLowerChestInventory().getName();

            return customChest || name.equals(Blocks.chest.getLocalizedName());
        }
        return false;
    }

    public static int getArmorLevel(final @NotNull ItemStack itemStack) {
        int level = 0;

        final Item item = itemStack.getItem();
        if (item == Items.diamond_helmet || item == Items.diamond_chestplate || item == Items.diamond_leggings || item == Items.diamond_boots)
            level += 15;
        else if (item == Items.iron_helmet || item == Items.iron_chestplate || item == Items.iron_leggings || item == Items.iron_boots)
            level += 10;
        else if (item == Items.golden_helmet || item == Items.golden_chestplate || item == Items.golden_leggings || item == Items.golden_boots)
            level += 5;
        else if (item == Items.chainmail_helmet || item == Items.chainmail_chestplate || item == Items.chainmail_leggings || item == Items.chainmail_boots)
            level += 5;

        level += getProtection(itemStack);

        return level;
    }

    public static int getProtection(final @NotNull ItemStack itemStack) {
        return ((ItemArmor)itemStack.getItem()).damageReduceAmount + EnchantmentHelper.getEnchantmentModifierDamage(new ItemStack[] { itemStack }, DamageSource.generic);
    }

    public static void click(int slot) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, 0, 1, mc.thePlayer);
    }

    public static boolean sort(int bestSlot, int desiredSlot) {
        if (bestSlot != -1 && bestSlot != desiredSlot + 35) {
            swap(bestSlot, desiredSlot - 1);
            return true;
        }
        return false;
    }

    public static void drop(int slot) {
        mc.playerController.windowClick(0, slot, 1, 4, mc.thePlayer);
    }

    public static void swap(int slot, int hSlot) {
        mc.playerController.windowClick(mc.thePlayer.inventoryContainer.windowId, slot, hSlot, 2, mc.thePlayer);
    }

    public static boolean isSpeedPot(ItemStack item) {
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

    public static boolean inventoryFull() {
        for (int i = 9; i < 45; i++) {
            if (mc.thePlayer.inventoryContainer.getSlot(i).getStack() == null) {
                return false;
            }
        }
        return true;
    }

    public static void steal(@NotNull ContainerChest chest, int slot) {
        mc.playerController.windowClick(chest.windowId, slot, 0, 1, mc.thePlayer);
    }

    public static int getMostBlocks(int desiredSlot) {
        int count = 0;
        int biggestSlot = -1;

        if (desiredSlot != -1) {
            ItemStack item = getItemStack(desiredSlot);
            if (item != null && item.getItem() instanceof ItemBlock && canBePlaced((ItemBlock) item.getItem())) {
                count = item.stackSize;
                biggestSlot = desiredSlot;
            }
        }

        for (int i = 9; i < 45; i++) {
            ItemStack item = getItemStack(i);
            if (item != null && item.getItem() instanceof ItemBlock && item.stackSize > count && canBePlaced((ItemBlock) item.getItem())) {
                count = item.stackSize;
                biggestSlot = i;
            }
        }
        return biggestSlot;
    }

    public static int getMostProjectiles(int desiredSlot) {
        int biggestSnowballSlot = getBiggestStack(Items.snowball, desiredSlot);
        int biggestEggSlot = getBiggestStack(Items.egg, desiredSlot);
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
        else if (biggestSnowballSlot != -1) {
            biggestSlot = biggestSnowballSlot;
        }
        return biggestSlot;
    }
}
