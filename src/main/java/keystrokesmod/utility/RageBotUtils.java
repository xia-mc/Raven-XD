package keystrokesmod.utility;

import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.other.anticheats.utils.world.PlayerRotation;
import keystrokesmod.script.classes.Vec3;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static keystrokesmod.Raven.mc;

public class RageBotUtils {
    public static int getArmHypixelBedWars(Set<Integer> ignoreSlots) {
        int arm = -1;
        int level = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack != null && isArmHypixelBedWars(stack.getItem())) {
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

    public static boolean isArmHypixelBedWars(Item item) {
        return item instanceof ItemHoe;
    }

    public static int getArmHypixelZombie(Set<Integer> ignoreSlots) {
        int arm = getArmHypixelBedWars(ignoreSlots);
        if (arm != -1)
            return arm;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack != null && isArmHypixelZombie(stack.getItem())) {
                if (ignoreSlots.contains(i))
                    continue;

                return i;
            }
        }
        return -1;
    }

    public static boolean isArmHypixelZombie(Item item) {
        return item instanceof ItemHoe || item instanceof ItemPickaxe || item instanceof ItemFlintAndSteel || item == Items.golden_shovel;
    }

    public static int getArmCubeCraft(Set<Integer> ignoreSlots) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack != null && isArmCubeCraft(stack.getItem())) {
                if (ignoreSlots.contains(i))
                    continue;

                return i;
            }
        }
        return -1;
    }

    public static boolean isArmCubeCraft(Item item) {
        return item instanceof ItemFirework;
    }

    public static int getArmCSGO(Set<Integer> ignoreSlots) {
        int arm = -1;
        int level = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
            if (stack != null && isArmCSGO(stack.getItem())) {
                if (ignoreSlots.contains(i))
                    continue;

                int curLevel;
                String name = stack.getItem().getUnlocalizedName().toLowerCase();
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

    public static boolean isArmCSGO(Item item) {
        return item instanceof ItemTool;
    }

    public static boolean isTeammateCSGO(EntityPlayer target) {
        try {
            final ItemStack selfHead = Objects.requireNonNull(mc.thePlayer.inventory.armorInventory[3]);
            final ItemStack selfChest = Objects.requireNonNull(mc.thePlayer.inventory.armorInventory[2]);
            final ItemStack targetHead = Objects.requireNonNull(target.inventory.armorInventory[3]);
            final ItemStack targetChest = Objects.requireNonNull(target.inventory.armorInventory[2]);

            return isEqualArmorCSGO(selfHead, targetHead) || isEqualArmorCSGO(selfChest, targetChest);
        } catch (NullPointerException e) {
            // is bot, so don't attack
            return true;
        }
    }

    private static boolean isEqualArmorCSGO(@NotNull ItemStack stack1, @NotNull ItemStack stack2) {
        final ItemArmor armor1, armor2;
        try {
            armor1 = (ItemArmor) stack1.getItem();
            armor2 = (ItemArmor) stack2.getItem();
        } catch (ClassCastException e) {
            return false;
        }

        if (armor1 == armor2)
            return true;
        if (armor1.getArmorMaterial() == ItemArmor.ArmorMaterial.IRON && armor2.getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER && armor2.getColor(stack2) == 255) {
            return true;
        }
        if (armor2.getArmorMaterial() == ItemArmor.ArmorMaterial.IRON && armor1.getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER && armor1.getColor(stack2) == 255) {
            return true;
        }
        if (armor1.getArmorMaterial() == ItemArmor.ArmorMaterial.CHAIN && armor2.getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER && armor2.getColor(stack2) == 16711680) {
            return true;
        }
        if (armor2.getArmorMaterial() == ItemArmor.ArmorMaterial.CHAIN && armor1.getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER && armor1.getColor(stack2) == 16711680) {
            return true;
        }
        return false;
    }

    public static @Nullable Vec3 getHitPos(EntityLivingBase entity, int predTicks) {
        double preResult;
        switch ((int) ModuleManager.rageBot.priorityHitBox.getInput()) {
            default:
            case 0:
                preResult = entity.getEyeHeight();
                break;
            case 1:
                preResult = entity.getEyeHeight() - 0.6;
                break;
            case 2:
                preResult = 0.5;
        }

        @Nullable Vec3 hitPos = testCanHit(entity, preResult, predTicks)
                .orElse(testCanHit(entity, entity.getEyeHeight(), predTicks)
                        .orElse(testCanHit(entity, entity.getEyeHeight() - 0.6, predTicks)
                                .orElse(testCanHit(entity, 0.5, predTicks).orElse(null))
                        )
                );

        return hitPos;
    }

    private static @NotNull Vec3 getPredHitPos(@NotNull EntityLivingBase entity, double yOffset, int predTicks) {
        Vec3 result = new Vec3(entity).add(0, yOffset, 0);

        return MoveUtil.predictedPos(entity, new Vec3(entity.motionX, entity.motionY, entity.motionZ), result, predTicks);
    }

    private static Optional<Vec3> testCanHit(EntityLivingBase entity, double hitHeight, int predTicks) {
        Vec3 hitPos = getPredHitPos(entity, hitHeight, predTicks);
        if (RotationUtils.rayCast(Utils.getEyePos().distanceTo(hitPos), PlayerRotation.getYaw(hitPos), PlayerRotation.getPitch(hitPos)) == null) {
            return Optional.of(hitPos);
        }
        return Optional.empty();
    }
}
