package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.render.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Xray extends Module {
    private final SliderSetting range;
    private final SliderSetting rate;
    private final ButtonSetting caveOnly;
    private final ButtonSetting iron;
    private final ButtonSetting gold;
    private final ButtonSetting diamond;
    private final ButtonSetting emerald;
    private final ButtonSetting lapis;
    private final ButtonSetting redstone;
    private final ButtonSetting coal;
    private final ButtonSetting spawner;
    private final ButtonSetting obsidian;
    private final List<BlockPos> blocks = new ArrayList<>();
    private long lastCheck = 0;

    public Xray() {
        super("Xray", category.render);
        this.registerSetting(range = new SliderSetting("Range", 20, 5, 50, 1));
        this.registerSetting(rate = new SliderSetting("Rate", 0.5, 0.1, 3.0, 0.1, " second"));
        this.registerSetting(caveOnly = new ButtonSetting("Cave Only (bypass Anti-Xray)", false));
        this.registerSetting(coal = new ButtonSetting("Coal", true));
        this.registerSetting(diamond = new ButtonSetting("Diamond", true));
        this.registerSetting(emerald = new ButtonSetting("Emerald", true));
        this.registerSetting(gold = new ButtonSetting("Gold", true));
        this.registerSetting(iron = new ButtonSetting("Iron", true));
        this.registerSetting(lapis = new ButtonSetting("Lapis", true));
        this.registerSetting(obsidian = new ButtonSetting("Obsidian", true));
        this.registerSetting(redstone = new ButtonSetting("Redstone", true));
        this.registerSetting(spawner = new ButtonSetting("Spawner", true));
    }

    public void onDisable() {
        this.blocks.clear();
    }

    public void onUpdate() {
        if (System.currentTimeMillis() - lastCheck < rate.getInput() * 1000) {
            return;
        }
        lastCheck = System.currentTimeMillis();
        int i;
        for (int n = i = (int) range.getInput(); i >= -n; --i) {
            for (int j = -n; j <= n; ++j) {
                for (int k = -n; k <= n; ++k) {
                    BlockPos blockPos = new BlockPos(mc.thePlayer.posX + j, mc.thePlayer.posY + i, mc.thePlayer.posZ + k);
                    if (blocks.contains(blockPos)) {
                        continue;
                    }
                    Block blockState = BlockUtils.getBlock(blockPos);
                    if (blockState != null && canBreak(blockState)) {
                        if (caveOnly.isToggled() && !isNextToAir(blockPos)) continue;
                        blocks.add(blockPos);
                    }
                }
            }
        }
    }

    private boolean isNextToAir(BlockPos blockPos) {
        BlockPos[] neighbors = new BlockPos[]{
                blockPos.up(),
                blockPos.down(),
                blockPos.north(),
                blockPos.south(),
                blockPos.east(),
                blockPos.west()
        };
        for (BlockPos neighbor : neighbors) {
            Block neighborBlock = BlockUtils.getBlock(neighbor);
            if (neighborBlock == null || neighborBlock.equals(Blocks.air)) {
                return true;
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinWorldEvent e) {
        if (e.entity == mc.thePlayer) {
            this.blocks.clear();
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent ev) {
        if (!Utils.nullCheck()) {
            return;
        }
        if (!this.blocks.isEmpty()) {
            Iterator iterator = blocks.iterator();
            while (iterator.hasNext()) {
                BlockPos blockPos = (BlockPos) iterator.next();
                Block block = BlockUtils.getBlock(blockPos);
                if (block == null || !canBreak(block)) {
                    iterator.remove();
                    continue;
                }
                this.drawBox(blockPos);
            }
        }
    }

    private void drawBox(BlockPos p) {
        if (p == null) {
            return;
        }
        int[] rgb = this.getColor(BlockUtils.getBlock(p));
        if (rgb[0] + rgb[1] + rgb[2] != 0) {
            RenderUtils.renderBlock(p, (new Color(rgb[0], rgb[1], rgb[2])).getRGB(), false, true);
        }
    }

    private int[] getColor(Block b) {
        int red = 0;
        int green = 0;
        int blue = 0;
        if (b.equals(Blocks.iron_ore)) {
            red = 255;
            green = 255;
            blue = 255;
        } else if (b.equals(Blocks.gold_ore)) {
            red = 255;
            green = 255;
        } else if (b.equals(Blocks.diamond_ore)) {
            green = 220;
            blue = 255;
        } else if (b.equals(Blocks.emerald_ore)) {
            red = 35;
            green = 255;
        } else if (b.equals(Blocks.lapis_ore)) {
            green = 50;
            blue = 255;
        } else if (b.equals(Blocks.redstone_ore)) {
            red = 255;
        } else if (b.equals(Blocks.mob_spawner)) {
            red = 30;
            blue = 135;
        }

        return new int[]{red, green, blue};
    }

    public boolean canBreak(Block block) {
        return (iron.isToggled() && block.equals(Blocks.iron_ore)) ||
                (gold.isToggled() && block.equals(Blocks.gold_ore)) ||
                (diamond.isToggled() && block.equals(Blocks.diamond_ore)) ||
                (emerald.isToggled() && block.equals(Blocks.emerald_ore)) ||
                (lapis.isToggled() && block.equals(Blocks.lapis_ore)) ||
                (redstone.isToggled() && block.equals(Blocks.redstone_ore)) ||
                (coal.isToggled() && block.equals(Blocks.coal_ore)) ||
                (spawner.isToggled() && block.equals(Blocks.mob_spawner)) ||
                (obsidian.isToggled() && block.equals(Blocks.obsidian));
    }
}