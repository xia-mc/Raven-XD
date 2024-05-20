package keystrokesmod.module.impl.player;

import keystrokesmod.Raven;
import keystrokesmod.event.PostUpdateEvent;
import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.minigames.BedWars;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class BedAura extends Module {
    public SliderSetting mode;
    private SliderSetting breakSpeed;
    private SliderSetting fov;
    private SliderSetting range;
    private SliderSetting rate;
    public ButtonSetting allowAura;
    private ButtonSetting breakBlockAbove;
    public ButtonSetting groundSpoof;
    public ButtonSetting ignoreSlow;
    private ButtonSetting onlyWhileVisible;
    private ButtonSetting renderOutline;
    private ButtonSetting sendAnimations;
    private ButtonSetting silentSwing;
    private String[] modes = new String[]{"Legit", "Instant", "Swap"};
    private BlockPos[] bedPos;
    public float breakProgress;
    private int currentSlot = -1;
    private int lastSlot = -1;
    private boolean rotate;
    public BlockPos currentBlock;
    private long lastCheck = 0;
    public boolean stopAutoblock;
    private int outlineColor;
    private int breakTickDelay = 5;
    private int ticksAfterBreak = 0;
    private boolean delayStart;
    public double lastProgress;
    private int defaultOutlineColor = new Color(226, 65, 65).getRGB();

    public BedAura() {
        super("BedAura", category.player, 0);
        this.registerSetting(mode = new SliderSetting("Break mode", modes, 0));
        this.registerSetting(breakSpeed = new SliderSetting("Break speed", 1, 0.8, 2, 0.01, "x"));
        this.registerSetting(fov = new SliderSetting("FOV", 360.0, 30.0, 360.0, 4.0));
        this.registerSetting(range = new SliderSetting("Range", 4.5, 1.0, 8.0, 0.5));
        this.registerSetting(rate = new SliderSetting("Rate", 0.2, 0.05, 3.0, 0.05, " second"));
        this.registerSetting(allowAura = new ButtonSetting("Allow aura", true));
        this.registerSetting(breakBlockAbove = new ButtonSetting("Break block above", false));
        this.registerSetting(groundSpoof = new ButtonSetting("Ground spoof", false));
        this.registerSetting(ignoreSlow = new ButtonSetting("Ignore slow", false));
        this.registerSetting(onlyWhileVisible = new ButtonSetting("Only while visible", false));
        this.registerSetting(renderOutline = new ButtonSetting("Render block outline", true));
        this.registerSetting(sendAnimations = new ButtonSetting("Send animations", false));
        this.registerSetting(silentSwing = new ButtonSetting("Silent swing", false));
    }

    @Override
    public String getInfo() {
        return modes[(int) mode.getInput()];
    }

    @Override
    public void onDisable() {
        reset(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPreUpdate(PreUpdateEvent e) {
        if (!Utils.nullCheck()) {
            return;
        }
        if (ModuleManager.bedwars != null && ModuleManager.bedwars.isEnabled() && BedWars.whitelistOwnBed.isToggled() && !BedWars.outsideSpawn) {
            reset(true);
            return;
        }
        if (!mc.thePlayer.capabilities.allowEdit || mc.thePlayer.isSpectator()) {
            reset(true);
            return;
        }
        if (bedPos == null) {
            if (System.currentTimeMillis() - lastCheck >= rate.getInput() * 1000) {
                lastCheck = System.currentTimeMillis();
                bedPos = getBedPos();
            }
            if (bedPos == null) {
                reset(true);
                return;
            }
        }
        else {
            if (!(BlockUtils.getBlock(bedPos[0]) instanceof BlockBed) || (currentBlock != null && BlockUtils.replaceable(currentBlock))) {
                reset(true);
                return;
            }
        }
        if (delayStart) {
            resetSlot();
            if (ticksAfterBreak++ <= breakTickDelay) {
                return;
            }
            else {
                delayStart = false;
                ticksAfterBreak = 0;
            }
        }
        else {
            ticksAfterBreak = 0;
        }
        if (breakBlockAbove.isToggled() && isCovered(bedPos[0]) && isCovered(bedPos[1])) {
            breakBlock(bedPos[0].up());
        }
        else {
            resetSlot();
            breakBlock(bedPos[0]);
        }
    }

    @SubscribeEvent
    public void onPostUpdate(PostUpdateEvent e) {
        stopAutoblock = false;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPreMotion(PreMotionEvent e) {
        if ((rotate || breakProgress >= 1 || breakProgress == 0) && currentBlock != null) {
            float[] rotations = RotationUtils.getRotations(currentBlock, e.getYaw(), e.getPitch());
            if (!RotationUtils.inRange(currentBlock, range.getInput())) {
                return;
            }
            e.setYaw(rotations[0]);
            e.setPitch(rotations[1]);
            rotate = false;
            if (groundSpoof.isToggled() && !mc.thePlayer.isInWater()) {
                e.setOnGround(true);
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent renderWorldLastEvent) {
        if (!renderOutline.isToggled() || currentBlock == null || !Utils.nullCheck()) {
            return;
        }
        if (ModuleManager.bedESP != null && ModuleManager.bedESP.isEnabled()) {
            outlineColor = Theme.getGradient((int) ModuleManager.bedESP.theme.getInput(), 0);
        }
        else if (ModuleManager.hud != null && ModuleManager.hud.isEnabled()) {
            outlineColor = Theme.getGradient((int) ModuleManager.hud.theme.getInput(), 0);
        }
        else {
            outlineColor = defaultOutlineColor;
        }
        RenderUtils.renderBlock(currentBlock, outlineColor, true, false);
    }

    private void resetSlot() {
        if (currentSlot != -1 && currentSlot != mc.thePlayer.inventory.currentItem && mode.getInput() == 2) {
            setPacketSlot(mc.thePlayer.inventory.currentItem);
        }
        else if (lastSlot != -1) {
            lastSlot = mc.thePlayer.inventory.currentItem = lastSlot;
        }
    }

    private BlockPos[] getBedPos() {
        int range;
        priority:
        for (int n = range = (int) this.range.getInput(); range >= -n; --range) {
            for (int j = -n; j <= n; ++j) {
                for (int k = -n; k <= n; ++k) {
                    final BlockPos blockPos = new BlockPos(mc.thePlayer.posX + j, mc.thePlayer.posY + range, mc.thePlayer.posZ + k);
                    final IBlockState getBlockState = mc.theWorld.getBlockState(blockPos);
                    if (getBlockState.getBlock() == Blocks.bed && getBlockState.getValue((IProperty) BlockBed.PART) == BlockBed.EnumPartType.FOOT) {
                        float fov = (float) this.fov.getInput();
                        if (fov != 360 && !Utils.inFov(fov, blockPos)) {
                            continue priority;
                        }
                        return new BlockPos[]{blockPos, blockPos.offset((EnumFacing) getBlockState.getValue((IProperty) BlockBed.FACING))};
                    }
                }
            }
        }
        return null;
    }

    private void reset(boolean resetSlot) {
        if (resetSlot) {
            resetSlot();
            currentSlot = -1;
        }
        bedPos = null;
        breakProgress = 0;
        rotate = false;
        ticksAfterBreak = 0;
        currentBlock = null;
        lastSlot = -1;
        delayStart = false;
        stopAutoblock = false;
        lastProgress = 0;
    }

    public void setPacketSlot(int slot) {
        if (slot == currentSlot || slot == -1 || Raven.badPacketsHandler.playerSlot == slot) {
            return;
        }
        mc.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(slot));
        currentSlot = slot;
    }

    private void startBreak(BlockPos blockPos) {
        mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, blockPos, EnumFacing.UP));
    }

    private void stopBreak(BlockPos blockPos) {
        mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, blockPos, EnumFacing.UP));
    }

    private void swing() {
        if (!silentSwing.isToggled()) {
            mc.thePlayer.swingItem();
        }
        else {
            mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
        }
    }

    private void breakBlock(BlockPos blockPos) {
        if (blockPos == null) {
            return;
        }
        float fov = (float) this.fov.getInput();
        if (fov != 360 && !Utils.inFov(fov, blockPos)) {
            return;
        }
        if (!RotationUtils.inRange(blockPos, range.getInput())) {
            return;
        }
        if (onlyWhileVisible.isToggled() && (mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || !mc.objectMouseOver.getBlockPos().equals(blockPos))) {
            return;
        }
        if (BlockUtils.replaceable(currentBlock == null ? blockPos : currentBlock)) {
            reset(true);
            return;
        }
        currentBlock = blockPos;
        Block block = BlockUtils.getBlock(blockPos);
        if (mode.getInput() == 2 || mode.getInput() == 0) {
            if (breakProgress == 0) {
                resetSlot();
                stopAutoblock = true;
                rotate = true;
                if (mode.getInput() == 0) {
                    setSlot(Utils.getTool(block));
                }
                swing();
                startBreak(blockPos);
            }
            else if (breakProgress >= 1) {
                if (mode.getInput() == 2) {
                    ModuleManager.killAura.resetBlinkState(false);
                    setPacketSlot(Utils.getTool(block));
                }
                swing();
                stopBreak(blockPos);
                reset(false);
                stopAutoblock = true;
                delayStart = true;
                return;
            }
            else {
                if (mode.getInput() == 0) {
                    stopAutoblock = true;
                    rotate = true;
                    swing();
                }
            }
            double progress = BlockUtils.getBlockHardness(block, (mode.getInput() == 2 && Utils.getTool(block) != -1) ? mc.thePlayer.inventory.getStackInSlot(Utils.getTool(block)) : mc.thePlayer.getHeldItem(), false, ignoreSlow.isToggled() || groundSpoof.isToggled()) * breakSpeed.getInput();
            if (lastProgress != 0 && breakProgress >= lastProgress) {
                ModuleManager.killAura.resetBlinkState(false);
                stopAutoblock = true;
            }
            breakProgress += progress;
            if (sendAnimations.isToggled()) {
                mc.theWorld.sendBlockBreakProgress(mc.thePlayer.getEntityId(), blockPos, (int) ((breakProgress * 10) - 1));
            }
            lastProgress = 0;
            while (lastProgress + progress < 1) {
                lastProgress += progress;
            }
        }
        else if (mode.getInput() == 1) {
            stopAutoblock = true;
            rotate = true;
            swing();
            startBreak(blockPos);
            setSlot(Utils.getTool(block));
            swing();
            stopBreak(blockPos);
        }
    }

    private void setSlot(int slot) {
        if (slot == -1 || slot == mc.thePlayer.inventory.currentItem) {
            return;
        }
        if (lastSlot == -1) {
            lastSlot = mc.thePlayer.inventory.currentItem;
        }
        mc.thePlayer.inventory.currentItem = slot;
    }

    private boolean isCovered(BlockPos blockPos) {
        for (EnumFacing enumFacing : EnumFacing.values()) {
            BlockPos offset = blockPos.offset(enumFacing);
            if (BlockUtils.replaceable(offset) || BlockUtils.notFull(BlockUtils.getBlock(offset)) ) {
                return false;
            }
        }
        return true;
    }
}
