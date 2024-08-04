package keystrokesmod.utility;

import net.minecraft.init.Items;
import net.minecraft.item.*;

import java.util.Set;

import static keystrokesmod.Raven.mc;

public class ArmedAuraUtils {
    public static int getArmHypixelBedWars(Set<Integer> ignoreSlots) {
        int arm = -1;
        int level = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack != null && stack.getItem() instanceof ItemHoe) {
                if (ignoreSlots.contains(i))
                    continue;

                int curLevel;
                String name = ((ItemHoe) stack.getItem()).getMaterialName().toLowerCase();
                switch (name) {
                    default:
                    case "wood":
                        curLevel = 1;
                        break;
                    case "stone":
                        curLevel = 2;
                        break;
                    case "iron":
                        curLevel = 3;
                        break;
                    case "gold":
                        curLevel = 4;
                        break;
                    case "diamond":
                        curLevel = 5;
                        break;
                }

                if (curLevel > level) {
                    level = curLevel;
                    arm = i;
                }
            }
        }
        return arm;
    }

    public static int getArmHypixelZombie(Set<Integer> ignoreSlots) {
        int arm = getArmHypixelBedWars(ignoreSlots);
        if (arm != -1)
            return arm;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack != null && (stack.getItem() instanceof ItemPickaxe
                            || stack.getItem() instanceof ItemFlintAndSteel
                            || stack.getItem() == Items.golden_shovel)) {
                if (ignoreSlots.contains(i))
                    continue;

                return i;
            }
        }
        return -1;
    }

    public static int getArmCubeCraft(Set<Integer> ignoreSlots) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack != null && stack.getItem() instanceof ItemFirework) {
                if (ignoreSlots.contains(i))
                    continue;

                return i;
            }
        }
        return -1;
    }
}
