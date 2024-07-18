package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.CoolDown;
import keystrokesmod.utility.ShaderUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;

public class BedPlates extends Module {
    public static final ShaderUtils roundedShader = new ShaderUtils("keystrokesmod:shaders/rrect.frag");

    public static SliderSetting updateRate, yShift, layers;
    private final CoolDown updateCooldown = new CoolDown(0);
    private final List<BlockPos> beds = new ArrayList<>();
    private final List<List<Block>> bedBlocks = new ArrayList<>();
    private BlockPos ownBed;
    public BedPlates() {
        super("Bed Plates", category.render);
        this.registerSetting(yShift = new SliderSetting("Y-shift", 2, -5, 10, 1));
        this.registerSetting(updateRate = new SliderSetting("Update rate (ms)", 1000, 250, 5000, 250));
        this.registerSetting(layers = new SliderSetting("Layers", 3, 3, 10, 1));
    }

    public void onEnable() {
        for (int i = 0; i < 8; i++) {
            this.beds.add(null);
            this.bedBlocks.add(new ArrayList<>());
        }
    }

    public void onDisable() {
        this.beds.clear();
        this.bedBlocks.clear();
        this.ownBed = null;
    }

    public void updateBeds() {
        if (Utils.nullCheck()) {
            findOwnBed();
            if (ownBed != null) {
                findBed(ownBed.getZ(), ownBed.getY(), ownBed.getX(), 1);
                findBed(-ownBed.getZ(), ownBed.getY(), ownBed.getX(), 2);
                findBed(ownBed.getZ(), ownBed.getY(), -ownBed.getX(), 3);
                findBed(-ownBed.getZ(), ownBed.getY(), -ownBed.getX(), 4);
                findBed(-ownBed.getX(), ownBed.getY(), ownBed.getZ(), 5);
                findBed(ownBed.getX(), ownBed.getY(), -ownBed.getZ(), 6);
                findBed(-ownBed.getX(), ownBed.getY(), -ownBed.getZ(), 7);
            }
        }
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinWorldEvent e) {
        if (e.entity == mc.thePlayer) {
            this.onDisable();
            this.onEnable();
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent e) {
        if (Utils.nullCheck() && !this.beds.isEmpty()) {
            int index = 0;
            if (updateCooldown.hasFinished()) {
                updateBeds();
                updateCooldown.setCooldown((long) updateRate.getInput());
                updateCooldown.start();
            }
            for (BlockPos blockPos : this.beds) {
                if (beds.get(index) != null) {
                    this.drawPlate(blockPos, index);
                    index++;
                }
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

    private void findOwnBed() {
        if (this.ownBed == null) {
            for (int y = 2; y >= -2; --y) {
                for (int x = 0; x <= 20; ++x) {
                    for (int z = 0; z <= 20; ++z) {
                        if (findBed(Module.mc.thePlayer.posX - Module.mc.thePlayer.posX / Math.abs(Module.mc.thePlayer.posX) * x,
                                Module.mc.thePlayer.posY + (double) y,
                                Module.mc.thePlayer.posZ - Module.mc.thePlayer.posZ / Math.abs(Module.mc.thePlayer.posZ) * z, 0)) {
                            this.ownBed = this.beds.get(0);
                            return;
                        }
                    }
                }
            }
        } else {
            findBed(ownBed.getX(), ownBed.getY(), ownBed.getZ(), 0);
        }
    }

    private boolean findBed(double x, double y, double z, int index) {
        BlockPos bedPos = new BlockPos(x, y, z);
        Block bed = Module.mc.theWorld.getBlockState(bedPos).getBlock();
        bedBlocks.get(index).clear();
        beds.set(index, null);
        if (beds.contains(bedPos)) {
            return false;
        }
        if (bed.equals(Blocks.bed)) {
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
