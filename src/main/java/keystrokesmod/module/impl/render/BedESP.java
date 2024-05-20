package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.RenderUtils;
import keystrokesmod.utility.Theme;
import keystrokesmod.utility.Utils;
import net.minecraft.block.BlockBed;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BedESP extends Module {
    public SliderSetting theme;
    private final SliderSetting range;
    private final SliderSetting rate;
    private final ButtonSetting firstBed;
    private BlockPos[] bed = null;
    private final List<BlockPos[]> beds = new ArrayList<>();
    private long lastCheck = 0;

    public BedESP() {
        super("BedESP", category.render);
        this.registerSetting(theme = new SliderSetting("Theme", Theme.themes, 0));
        this.registerSetting(range = new SliderSetting("Range", 10.0, 2.0, 30.0, 1.0));
        this.registerSetting(rate = new SliderSetting("Rate", 0.4, 0.1, 3.0, 0.1, " second"));
        this.registerSetting(firstBed = new ButtonSetting("Only render first bed", false));
    }

    public void onUpdate() {
        if (System.currentTimeMillis() - lastCheck < rate.getInput() * 1000) {
            return;
        }
        lastCheck = System.currentTimeMillis();
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
                                if (BlockUtils.isSamePos(blockPos, ((BlockPos[]) this.beds.get(l))[0])) {
                                    continue priorityLoop;
                                }
                            }
                            this.beds.add(new BlockPos[]{blockPos, blockPos.offset((EnumFacing) getBlockState.getValue((IProperty) BlockBed.FACING))});
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinWorldEvent e) {
        if (e.entity == mc.thePlayer) {
            this.beds.clear();
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
                renderBed(this.bed);
                return;
            }
            if (this.beds.isEmpty()) {
                return;
            }
            Iterator<BlockPos[]> iterator = this.beds.iterator();
            while (iterator.hasNext()) {
                BlockPos[] blockPos = iterator.next();
                if (!(mc.theWorld.getBlockState(blockPos[0]).getBlock() instanceof BlockBed)) {
                    iterator.remove();
                    continue;
                }
                renderBed(blockPos);
            }
        }
    }

    public void onDisable() {
        this.bed = null;
        this.beds.clear();
    }

    private void renderBed(final BlockPos[] array) {
        final double n = array[0].getX() - mc.getRenderManager().viewerPosX;
        final double n2 = array[0].getY() - mc.getRenderManager().viewerPosY;
        final double n3 = array[0].getZ() - mc.getRenderManager().viewerPosZ;
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glLineWidth(2.0f);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        final int e = Theme.getGradient((int) theme.getInput(), 0);
        final float n4 = (e >> 24 & 0xFF) / 255.0f;
        final float n5 = (e >> 16 & 0xFF) / 255.0f;
        final float n6 = (e >> 8 & 0xFF) / 255.0f;
        final float n7 = (e & 0xFF) / 255.0f;
        GL11.glColor4d(n5, n6, n7, n4);
        AxisAlignedBB axisAlignedBB;
        if (array[0].getX() != array[1].getX()) {
            if (array[0].getX() > array[1].getX()) {
                axisAlignedBB = new AxisAlignedBB(n - 1.0, n2, n3, n + 1.0, n2 + 0.5625F, n3 + 1.0);
            } else {
                axisAlignedBB = new AxisAlignedBB(n, n2, n3, n + 2.0, n2 + 0.5625F, n3 + 1.0);
            }
        } else if (array[0].getZ() > array[1].getZ()) {
            axisAlignedBB = new AxisAlignedBB(n, n2, n3 - 1.0, n + 1.0, n2 + 0.5625F, n3 + 1.0);
        } else {
            axisAlignedBB = new AxisAlignedBB(n, n2, n3, n + 1.0, n2 + 0.5625F, n3 + 2.0);
        }
        RenderUtils.drawBoundingBox(axisAlignedBB, n5, n6, n7);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
    }
}
