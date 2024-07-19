package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.CoolDown;
import keystrokesmod.utility.ShaderUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.block.BlockBed;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.util.EnumFacing;
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
    private BlockPos[] bed = null;
    private SliderSetting range;
    private final List<BlockPos> beds = new ArrayList<>();
    private ButtonSetting firstBed;
    private final List<List<Block>> bedBlocks = new ArrayList<>();
    public BedPlates() {
        super("Bed Plates", category.render);
        this.registerSetting(yShift = new SliderSetting("Y-shift", 2, -5, 10, 1));
        this.registerSetting(updateRate = new SliderSetting("Update rate (ms)", 1000, 250, 5000, 250));
        this.registerSetting(range = new SliderSetting("Range", 10.0, 2.0, 30.0, 1.0));
        this.registerSetting(layers = new SliderSetting("Layers", 3, 3, 10, 1));
        this.registerSetting(firstBed = new ButtonSetting("Only render first bed", false));
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
                        if (getBlockState.getBlock() == Blocks.bed && getBlockState.getValue((IProperty) BlockBed.PART) == BlockBed.EnumPartType.FOOT) {
                            if (firstBed.isToggled()) {
                                if (this.bed != null && BlockUtils.isSamePos(blockPos, this.bed[0])) {
                                    return;
                                }
                                this.bed = new BlockPos[]{blockPos, blockPos.offset((EnumFacing) getBlockState.getValue((IProperty) BlockBed.FACING))};
                                return;
                            } else {
                                for (int l = 0; l < this.beds.size(); ++l) {
                                    if (BlockUtils.isSamePos(blockPos, ((BlockPos) this.beds.get(l)))) {
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
        glTranslatef((float) (blockPos.getX() - mc.getRenderManager().viewerPosX + 0.5), (float) (blockPos.getY() - mc.getRenderManager().viewerPosY + yShift.getInput() + 1), (float) (blockPos.getZ() - mc.getRenderManager().viewerPosZ + 0.5));
        glNormal3f(0.0F, 1.0F, 0.0F);
        glRotatef(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        glRotatef(mc.getRenderManager().playerViewX, rotateX, 0.0F, 0.0F);
        glScaled(-0.01666666753590107D * Math.sqrt(mc.thePlayer.getDistance(blockPos.getX(), blockPos.getY(), blockPos.getZ())), -0.01666666753590107D * Math.sqrt(mc.thePlayer.getDistance(blockPos.getX(), blockPos.getY(), blockPos.getZ())), 0.01666666753590107D * Math.sqrt(mc.thePlayer.getDistance(blockPos.getX(), blockPos.getY(), blockPos.getZ())));
        String dist = Math.round(mc.thePlayer.getDistance(blockPos.getX(), blockPos.getY(), blockPos.getZ())) + "m";
        drawRound(Math.max(17.5, bedBlocks.get(index).size() * 17.5) / -2, -0.5, Math.max(17.5, bedBlocks.get(index).size() * 17.5) - 2.5, 26.5, 3, new Color(0, 0, 0, 90));
        mc.fontRendererObj.drawString(dist, -mc.fontRendererObj.getStringWidth(dist) / 2, 0, new Color(255, 255, 255, 255).getRGB());
        double offset = (bedBlocks.get(index).size() * -17.5) / 2;
        List<Block> blocks = bedBlocks.get(index);
        for (int i = blocks.size() - 1; i >= 0; i--) {
            Block block = blocks.get(i);
            mc.getTextureManager().bindTexture(new ResourceLocation("keystrokesmod:images/" + block.getLocalizedName() + ".png"));
            Gui.drawModalRectWithCustomSizedTexture((int) offset, 10, 0, 0, 15, 15, 15, 15);
            offset += 17.5;
        }
        glEnable(GL_DEPTH_TEST);
        glPopMatrix();
    }

    private boolean findBed(double x, double y, double z, int index) {
        BlockPos bedPos = new BlockPos(x, y, z);
        Block Bed = Module.mc.theWorld.getBlockState(bedPos).getBlock();
        bedBlocks.get(index).clear();
        beds.set(index, null);
        if (beds.contains(bedPos)) {
            return false;
        }
        if (Bed.equals(Blocks.bed)) {
            for (int yi = 0; yi <= layers.getInput(); ++yi) {
                for (int xi = (int) -layers.getInput(); xi <= layers.getInput(); ++xi) {
                    for (int zi = (int) -layers.getInput(); zi <= layers.getInput(); ++zi) {
                        if (mc.theWorld.getBlockState(new BlockPos(bedPos.getX() + xi, bedPos.getY() + yi, bedPos.getZ() + zi)).getBlock().equals(Blocks.wool) && !bedBlocks.get(index).contains(Blocks.wool)) {
                            bedBlocks.get(index).add(Blocks.wool);
                        } else if (mc.theWorld.getBlockState(new BlockPos(bedPos.getX() + xi, bedPos.getY() + yi, bedPos.getZ() + zi)).getBlock().equals(Blocks.stained_hardened_clay) && !bedBlocks.get(index).contains(Blocks.stained_hardened_clay)) {
                            bedBlocks.get(index).add(Blocks.stained_hardened_clay);
                        } else if (mc.theWorld.getBlockState(new BlockPos(bedPos.getX() + xi, bedPos.getY() + yi, bedPos.getZ() + zi)).getBlock().equals(Blocks.stained_glass) && !bedBlocks.get(index).contains(Blocks.stained_glass)) {
                            bedBlocks.get(index).add(Blocks.stained_glass);
                        } else if (mc.theWorld.getBlockState(new BlockPos(bedPos.getX() + xi, bedPos.getY() + yi, bedPos.getZ() + zi)).getBlock().equals(Blocks.planks) && !bedBlocks.get(index).contains(Blocks.planks)) {
                            bedBlocks.get(index).add(Blocks.planks);
                        } else if (mc.theWorld.getBlockState(new BlockPos(bedPos.getX() + xi, bedPos.getY() + yi, bedPos.getZ() + zi)).getBlock().equals(Blocks.log) && !bedBlocks.get(index).contains(Blocks.planks)) {
                            bedBlocks.get(index).add(Blocks.planks);
                        } else if (mc.theWorld.getBlockState(new BlockPos(bedPos.getX() + xi, bedPos.getY() + yi, bedPos.getZ() + zi)).getBlock().equals(Blocks.log2) && !bedBlocks.get(index).contains(Blocks.planks)) {
                            bedBlocks.get(index).add(Blocks.planks);
                        } else if (mc.theWorld.getBlockState(new BlockPos(bedPos.getX() + xi, bedPos.getY() + yi, bedPos.getZ() + zi)).getBlock().equals(Blocks.end_stone) && !bedBlocks.get(index).contains(Blocks.end_stone)) {
                            bedBlocks.get(index).add(Blocks.end_stone);
                        } else if (mc.theWorld.getBlockState(new BlockPos(bedPos.getX() + xi, bedPos.getY() + yi, bedPos.getZ() + zi)).getBlock().equals(Blocks.obsidian) && !bedBlocks.get(index).contains(Blocks.obsidian)) {
                            bedBlocks.get(index).add(Blocks.obsidian);
                        } else if (mc.theWorld.getBlockState(new BlockPos(bedPos.getX() + xi, bedPos.getY() + yi, bedPos.getZ() + zi)).getBlock().equals(Blocks.water) && !bedBlocks.get(index).contains(Blocks.water)) {
                            bedBlocks.get(index).add(Blocks.water);
                        }
                    }
                }
            }
            if(!bedBlocks.get(index).contains(Blocks.bed)) {
                bedBlocks.get(index).add(Blocks.bed);
            }
            beds.set(index, bedPos);
            return true;
        }
        return false;
    }

    public static void drawRound(double x, double y, double width, double height, double radius, @NotNull Color color) {
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        roundedShader.init();

        setupRoundedRectUniforms(x, y, width, height, radius);
            roundedShader.setUniformf("color", color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);

        ShaderUtils.drawQuads(x - 1, y - 1, width + 2, height + 2);
        roundedShader.unload();
        GlStateManager.disableBlend();
    }

    private static void setupRoundedRectUniforms(double x, double y, double width, double height, double radius) {
        ScaledResolution sr = new ScaledResolution(mc);
        BedPlates.roundedShader.setUniformf("location", x * sr.getScaleFactor(), (mc.displayHeight - (height * sr.getScaleFactor())) - (y * sr.getScaleFactor()));
        BedPlates.roundedShader.setUniformf("rectSize", width * sr.getScaleFactor(), height * sr.getScaleFactor());
        BedPlates.roundedShader.setUniformf("radius", radius * sr.getScaleFactor());
    }
}