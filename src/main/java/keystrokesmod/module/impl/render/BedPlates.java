package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.CoolDown;
import keystrokesmod.utility.ShaderUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class BedPlates extends Module {
    public static final ShaderUtils roundedShader = new ShaderUtils("keystrokesmod:shaders/rrect.frag");

    public static SliderSetting updateRate, yShift, layers;
    private final CoolDown updateCooldown = new CoolDown(0);
    private final ButtonSetting showDistance;
    private final SliderSetting range;
    private final List<BlockPos> beds = new ArrayList<>();
    private final ButtonSetting firstBed;
    private final List<List<Block>> bedBlocks = new ArrayList<>();
    private BlockPos[] bed = null;

    public BedPlates() {
        super("Bed Plates", category.render);
        this.registerSetting(yShift = new SliderSetting("Y-shift", 2, -5, 10, 1));
        this.registerSetting(updateRate = new SliderSetting("Update rate (ms)", 1000, 250, 5000, 250));
        this.registerSetting(showDistance = new ButtonSetting("Show distance", true));
        this.registerSetting(range = new SliderSetting("Range", 10.0, 2.0, 30.0, 1.0));
        this.registerSetting(layers = new SliderSetting("Layers", 3, 1, 10, 1));
        this.registerSetting(firstBed = new ButtonSetting("Only render first bed", false));
    }

    public static void drawRound(double x, double y, double width, double height, double radius, @NotNull Color color) {
        GlStateManager.color(1, 1, 1, 1);
        roundedShader.init();

        setupRoundedRectUniforms(x, y, width, height, radius);
        roundedShader.setUniformf("color", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);

        ShaderUtils.drawQuads(x - 1, y - 1, width + 2, height + 2);
        roundedShader.unload();
    }

    private static void setupRoundedRectUniforms(double x, double y, double width, double height, double radius) {
        ScaledResolution sr = new ScaledResolution(mc);
        BedPlates.roundedShader.setUniformf("location", x * sr.getScaleFactor(), (mc.displayHeight - (height * sr.getScaleFactor())) - (y * sr.getScaleFactor()));
        BedPlates.roundedShader.setUniformf("rectSize", width * sr.getScaleFactor(), height * sr.getScaleFactor());
        BedPlates.roundedShader.setUniformf("radius", radius * sr.getScaleFactor());
    }

    public void onUpdate() {
        if (Utils.nullCheck()) {
            if (updateCooldown.hasFinished()) {
                updateCooldown.setCooldown((long) updateRate.getInput());
                updateCooldown.start();
            }
            int i;
            priorityLoop:
            for (int n = i = (int) range.getInput(); i >= -n; --i) {
                for (int j = -n; j <= n; ++j) {
                    for (int k = -n; k <= n; ++k) {
                        final BlockPos blockPos = new BlockPos(mc.thePlayer.posX + j, mc.thePlayer.posY + i, mc.thePlayer.posZ + k);
                        final IBlockState getBlockState = mc.theWorld.getBlockState(blockPos);
                        if (getBlockState.getBlock() == Blocks.bed && getBlockState.getValue(BlockBed.PART) == BlockBed.EnumPartType.FOOT) {
                            if (firstBed.isToggled()) {
                                if (this.bed != null && BlockUtils.isSamePos(blockPos, this.bed[0])) {
                                    return;
                                }
                                this.bed = new BlockPos[]{blockPos, blockPos.offset(getBlockState.getValue(BlockBed.FACING))};
                                return;
                            } else {
                                for (BlockPos pos : this.beds) {
                                    if (BlockUtils.isSamePos(blockPos, pos)) {
                                        continue priorityLoop;
                                    }
                                }
                                this.beds.add(blockPos);
                                this.bedBlocks.add(new ArrayList<>());
                            }
                        }
                    }
                }
            }
        }
    }

    public void onDisable() {
        this.beds.clear();
        this.bedBlocks.clear();
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinWorldEvent e) {
        if (e.entity == mc.thePlayer) {
            this.beds.clear();
            this.bedBlocks.clear();
            this.bed = null;
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent e) {
        if (Utils.nullCheck()) {
            if (firstBed.isToggled() && this.bed != null) {
                if (!(mc.theWorld.getBlockState(bed[0]).getBlock() instanceof BlockBed)) {
                    this.bed = null;
                    return;
                }
                findBed(bed[0].getX(), bed[0].getY(), bed[0].getZ(), 0);
                this.drawPlate(bed[0], 0);
            }
            if (this.beds.isEmpty()) {
                return;
            }
            Iterator<BlockPos> iterator = this.beds.iterator();
            while (iterator.hasNext()) {
                BlockPos blockPos = iterator.next();
                if (!(mc.theWorld.getBlockState(blockPos).getBlock() instanceof BlockBed)) {
                    iterator.remove();
                    continue;
                }
                findBed(blockPos.getX(), blockPos.getY(), blockPos.getZ(), this.beds.indexOf(blockPos));
                this.drawPlate(blockPos, this.beds.indexOf(blockPos));
            }
        }
    }

    private void drawPlate(BlockPos blockPos, int index) {
        float rotateX = mc.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F;
        glPushMatrix();
        glDisable(GL_DEPTH_TEST);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glTranslatef((float) (blockPos.getX() - mc.getRenderManager().viewerPosX + 0.5), (float) (blockPos.getY() - mc.getRenderManager().viewerPosY + yShift.getInput() + 1), (float) (blockPos.getZ() - mc.getRenderManager().viewerPosZ + 0.5));
        glNormal3f(0.0F, 1.0F, 0.0F);
        glRotatef(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        glRotatef(mc.getRenderManager().playerViewX, rotateX, 0.0F, 0.0F);
        glScaled(-0.01666666753590107D * Math.sqrt(mc.thePlayer.getDistance(blockPos.getX(), blockPos.getY(), blockPos.getZ())), -0.01666666753590107D * Math.sqrt(mc.thePlayer.getDistance(blockPos.getX(), blockPos.getY(), blockPos.getZ())), 0.01666666753590107D * Math.sqrt(mc.thePlayer.getDistance(blockPos.getX(), blockPos.getY(), blockPos.getZ())));
        List<Block> blocks = bedBlocks.get(index);
        drawRound(Math.max(17.5, blocks.size() * 17.5) / -2, -0.5, Math.max(17.5, blocks.size() * 17.5) - 2.5, 26.5, 3, new Color(0, 0, 0, 90));
        if (showDistance.isToggled()) {
            String dist = Math.round(mc.thePlayer.getDistance(blockPos.getX(), blockPos.getY(), blockPos.getZ())) + "m";
            mc.fontRendererObj.drawString(dist, -mc.fontRendererObj.getStringWidth(dist) / 2, 0, new Color(255, 255, 255, 255).getRGB());
        }
        double offset = (blocks.size() * -17.5) / 2;
        for (Block block : blocks) {
            mc.getTextureManager().bindTexture(new ResourceLocation("keystrokesmod:images/" + block.getLocalizedName() + ".png"));
            Gui.drawModalRectWithCustomSizedTexture((int) offset, 10, 0, 0, 15, 15, 15, 15);
            offset += 17.5;
        }
        GlStateManager.disableBlend();
        glEnable(GL_DEPTH_TEST);
        glPopMatrix();
    }

    private void findBed(double x, double y, double z, int index) {
        BlockPos bedPos = new BlockPos(x, y, z);
        Block bedBlock = Module.mc.theWorld.getBlockState(bedPos).getBlock();
        bedBlocks.get(index).clear();
        beds.set(index, null);

        if (beds.contains(bedPos) || !bedBlock.equals(Blocks.bed)) {
            return;
        }

        // Add bed to bedBlocks list
        bedBlocks.get(index).add(Blocks.bed);
        beds.set(index, bedPos);

        // Directions to check around the bed
        int[][] directions = {
                {0, 1, 0}, // Above
                {1, 0, 0}, // Right
                {-1, 0, 0}, // Left
                {0, 0, 1}, // Front
                {0, 0, -1}  // Back
        };

        int layersCount = (int) layers.getInput();

        for (int[] dir : directions) {
            for (int layer = 1; layer <= layersCount; layer++) {
                BlockPos currentPos = bedPos.add(dir[0] * layer, dir[1] * layer, dir[2] * layer);
                Block currentBlock = mc.theWorld.getBlockState(currentPos).getBlock();

                if (currentBlock.equals(Blocks.air)) {
                    break;
                }

                if (isValidBedBlock(currentBlock) && !bedBlocks.get(index).contains(currentBlock)) {
                    bedBlocks.get(index).add(currentBlock);
                }
            }
        }

    }

    private boolean isValidBedBlock(Block block) {
        return block.equals(Blocks.wool) || block.equals(Blocks.stained_hardened_clay) ||
                block.equals(Blocks.stained_glass) || block.equals(Blocks.planks) ||
                block.equals(Blocks.log) || block.equals(Blocks.log2) ||
                block.equals(Blocks.end_stone) || block.equals(Blocks.obsidian) ||
                block.equals(Blocks.water);
    }
}