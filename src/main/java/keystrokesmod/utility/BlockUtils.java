package keystrokesmod.utility;

import keystrokesmod.script.classes.Vec3;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockUtils {
    public static final Minecraft mc = Minecraft.getMinecraft();

    public static boolean isSamePos(BlockPos blockPos, BlockPos blockPos2) {
        return blockPos == blockPos2 || (blockPos.getX() == blockPos2.getX() && blockPos.getY() == blockPos2.getY() && blockPos.getZ() == blockPos2.getZ());
    }

    public static boolean notFull(Block block) {
        return block instanceof BlockFenceGate || block instanceof BlockLadder || block instanceof BlockFlowerPot || block instanceof BlockBasePressurePlate || isFluid(block) || block instanceof BlockFence || block instanceof BlockAnvil || block instanceof BlockEnchantmentTable || block instanceof BlockChest;
    }

    public static boolean isFluid(Block block) {
        return block.getMaterial() == Material.lava || block.getMaterial() == Material.water;
    }

    public static boolean isInteractable(Block block) {
        return block instanceof BlockFurnace || block instanceof BlockFenceGate || block instanceof BlockChest || block instanceof BlockEnderChest || block instanceof BlockEnchantmentTable || block instanceof BlockBrewingStand || block instanceof BlockBed || block instanceof BlockDropper || block instanceof BlockDispenser || block instanceof BlockHopper || block instanceof BlockAnvil || block == Blocks.crafting_table;
    }

    public static float getBlockHardness(final Block block, final ItemStack itemStack, boolean ignoreSlow, boolean ignoreGround) {
        final float getBlockHardness = block.getBlockHardness(mc.theWorld, null);
        if (getBlockHardness < 0.0f) {
            return 0.0f;
        }
        return (block.getMaterial().isToolNotRequired() || (itemStack != null && itemStack.canHarvestBlock(block))) ? (getToolDigEfficiency(itemStack, block, ignoreSlow, ignoreGround) / getBlockHardness / 30.0f) : (getToolDigEfficiency(itemStack, block, ignoreSlow, ignoreGround) / getBlockHardness / 100.0f);
    }

    public static float getToolDigEfficiency(ItemStack itemStack, Block block, boolean ignoreSlow, boolean ignoreGround) {
        float n = (itemStack == null) ? 1.0f : itemStack.getItem().getStrVsBlock(itemStack, block);
        if (n > 1.0f) {
            final int getEnchantmentLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack);
            if (getEnchantmentLevel > 0 && itemStack != null) {
                n += getEnchantmentLevel * getEnchantmentLevel + 1;
            }
        }
        if (mc.thePlayer.isPotionActive(Potion.digSpeed)) {
            n *= 1.0f + (mc.thePlayer.getActivePotionEffect(Potion.digSpeed).getAmplifier() + 1) * 0.2f;
        }
        if (!ignoreSlow) {
            if (mc.thePlayer.isPotionActive(Potion.digSlowdown)) {
                float n2;
                switch (mc.thePlayer.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) {
                    case 0: {
                        n2 = 0.3f;
                        break;
                    }
                    case 1: {
                        n2 = 0.09f;
                        break;
                    }
                    case 2: {
                        n2 = 0.0027f;
                        break;
                    }
                    default: {
                        n2 = 8.1E-4f;
                        break;
                    }
                }
                n *= n2;
            }
            if (mc.thePlayer.isInsideOfMaterial(Material.water) && !EnchantmentHelper.getAquaAffinityModifier(mc.thePlayer)) {
                n /= 5.0f;
            }
            if (!mc.thePlayer.onGround && !ignoreGround) {
                n /= 5.0f;
            }
        }
        return n;
    }

    public static Block getBlock(BlockPos blockPos) {
        return getBlockState(blockPos).getBlock();
    }

    public static Block getBlock(double x, double y, double z) {
        return getBlock(new BlockPos(x, y, z));
    }

    public static IBlockState getBlockState(BlockPos blockPos) {
        return mc.theWorld.getBlockState(blockPos);
    }

    public static boolean isBlockUnderNoCollisions() {
        for (int offset = 0; offset < mc.thePlayer.posY + mc.thePlayer.getEyeHeight(); offset += 2) {
            BlockPos blockPos = new BlockPos(mc.thePlayer.posX, offset, mc.thePlayer.posZ);

            if (mc.theWorld.getBlockState(blockPos).getBlock() != Blocks.air) {
                return true;
            }
        }
        return false;
    }

    public static boolean check(final BlockPos blockPos, final Block block) {
        return getBlock(blockPos) == block;
    }

    public static boolean replaceable(BlockPos blockPos) {
        if (!Utils.nullCheck()) {
            return true;
        }
        return getBlock(blockPos).isReplaceable(mc.theWorld, blockPos);
    }

    public static boolean isBlockUnder() {
        if (mc.thePlayer.posY < 0.0) {
            return false;
        } else {
            for(int offset = 0; offset < (int)mc.thePlayer.posY + 2; offset += 2) {
                AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox().offset(0.0, (double)(-offset), 0.0);
                if (!mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty()) {
                    return true;
                }
            }

            return false;
        }
    }

    public static boolean isBlockUnder(int distance) {
        for(int y = (int)mc.thePlayer.posY; y >= (int)mc.thePlayer.posY - distance; --y) {
            if (!(mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, y, mc.thePlayer.posZ)).getBlock() instanceof BlockAir)) {
                return true;
            }
        }

        return false;
    }

    public static @NotNull List<BlockPos> getAllInBox(@NotNull BlockPos from, @NotNull BlockPos to) {
        final List<BlockPos> blocks = new ArrayList<>();

        BlockPos min = new BlockPos(Math.min(from.getX(), to.getX()),
                Math.min(from.getY(), to.getY()), Math.min(from.getZ(), to.getZ()));
        BlockPos max = new BlockPos(Math.max(from.getX(), to.getX()),
                Math.max(from.getY(), to.getY()), Math.max(from.getZ(), to.getZ()));

        for (int x = min.getX(); x <= max.getX(); x++)
            for (int y = min.getY(); y <= max.getY(); y++)
                for (int z = min.getZ(); z <= max.getZ(); z++)
                    blocks.add(new BlockPos(x, y, z));

        return blocks;
    }

    public static @NotNull List<BlockPos> getAllInSphere(@NotNull Vec3 from, double distance) {
        final int blockDistance = (int) Math.round(distance);
        final List<BlockPos> blocks = new ArrayList<>();

        for (BlockPos blockPos : getAllInBox(
                new BlockPos(from.x() - blockDistance, from.y() - blockDistance, from.z() - blockDistance),
                new BlockPos(from.x() + blockDistance, from.y() + blockDistance, from.z() + blockDistance)
        )) {
            AxisAlignedBB box = getCollisionBoundingBox(blockPos);
            if (box == null) continue;

            if (RotationUtils.getNearestPoint(box, from).distanceTo(from) <= distance)
                blocks.add(blockPos);
        }

        return blocks;
    }

    public static @Nullable AxisAlignedBB getCollisionBoundingBox(BlockPos blockPos) {
        final IBlockState blockState = getBlockState(blockPos);
        final Block block = blockState.getBlock();

        if (block instanceof BlockAir) {
            return null;
        }
        if (block instanceof BlockGlass) {
            return new AxisAlignedBB(
                    blockPos.getX(), blockPos.getY(), blockPos.getZ(),
                    blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1
            );
        }
        return block.getCollisionBoundingBox(mc.theWorld, blockPos, blockState);
    }


    public static @NotNull Set<BlockPos> getSurroundBlocks(@NotNull AbstractClientPlayer target) {
        AxisAlignedBB playerBox = target.getEntityBoundingBox();

        int minX = MathHelper.floor_double(playerBox.minX) - 1;
        int minY = MathHelper.floor_double(playerBox.minY) - 1;
        int minZ = MathHelper.floor_double(playerBox.minZ) - 1;
        int maxX = MathHelper.floor_double(playerBox.maxX) + 1;
        int maxY = MathHelper.floor_double(playerBox.maxY) + 1;
        int maxZ = MathHelper.floor_double(playerBox.maxZ) + 1;

        return getAllInBox(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ))
                .stream()
                .filter(blockPos -> !playerBox.intersectsWith(new AxisAlignedBB(
                        blockPos.getX(), blockPos.getY(), blockPos.getZ(),
                        blockPos.getX() + 1, blockPos.getY() + 1, blockPos.getZ() + 1
                )))
                .filter(blockPos -> !((blockPos.getX() == minX || blockPos.getX() == maxX)
                        && (blockPos.getZ() == minZ || blockPos.getZ() == maxZ)))
//                .filter(blockPos -> !((blockPos.getY() == minY || blockPos.getY() == maxY)
//                        && (blockPos.getX() == minX || blockPos.getX() == maxX || blockPos.getZ() == minZ || blockPos.getZ() == maxZ)))
                .collect(Collectors.toSet());
    }

    public static boolean insideBlock() {
        if (mc.thePlayer.ticksExisted < 5) {
            return false;
        }

        return insideBlock(mc.thePlayer.getEntityBoundingBox());
    }

    public static boolean insideBlock(@NotNull final AxisAlignedBB bb) {
        final WorldClient world = mc.theWorld;
        for (int x = MathHelper.floor_double(bb.minX); x < MathHelper.floor_double(bb.maxX) + 1; ++x) {
            for (int y = MathHelper.floor_double(bb.minY); y < MathHelper.floor_double(bb.maxY) + 1; ++y) {
                for (int z = MathHelper.floor_double(bb.minZ); z < MathHelper.floor_double(bb.maxZ) + 1; ++z) {
                    final Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
                    final AxisAlignedBB boundingBox;
                    if (block != null && !(block instanceof BlockAir) && (boundingBox = block.getCollisionBoundingBox(world, new BlockPos(x, y, z), world.getBlockState(new BlockPos(x, y, z)))) != null && bb.intersectsWith(boundingBox)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
