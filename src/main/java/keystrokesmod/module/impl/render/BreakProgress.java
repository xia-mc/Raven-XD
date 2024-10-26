package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.Reflection;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.render.Animation;
import keystrokesmod.utility.render.Easing;
import keystrokesmod.utility.render.progress.Progress;
import keystrokesmod.utility.render.progress.ProgressManager;
import net.minecraft.block.BlockBed;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class BreakProgress extends Module {
    private final ModeSetting mode;
    private final ButtonSetting manual;
    private final ButtonSetting bedAura;
    private final ButtonSetting progressBar;
    private double progress;
    private BlockPos block;
    private String progressStr;
    private final Animation progressAnimation = new Animation(Easing.EASE_OUT_CIRC, 200);
    private final Progress progressObj = new Progress("BedAura");

    public BreakProgress() {
        super("BreakProgress", category.render);
        this.registerSetting(mode = new ModeSetting("Mode", new String[]{"Percentage", "Second", "Decimal"}, 0));
        this.registerSetting(manual = new ButtonSetting("Show manual", true));
        this.registerSetting(bedAura = new ButtonSetting("Show BedAura", true));
        this.registerSetting(progressBar = new ButtonSetting("Progress bar", false, bedAura::isToggled));
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event) {
        progressAnimation.run(progress);
        progressObj.setProgress(progressAnimation.getValue());
        progressObj.setText("BedAura " + progressStr);
        if (progressBar.isToggled() && progress > 0)
            ProgressManager.add(progressObj);
        else
            ProgressManager.remove(progressObj);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent e) {
        if (this.progress == 0.0f || this.block == null || !Utils.nullCheck()) {
            return;
        }
        final double n = this.block.getX() + 0.5 - mc.getRenderManager().viewerPosX;
        final double n2 = this.block.getY() + 0.5 - mc.getRenderManager().viewerPosY;
        final double n3 = this.block.getZ() + 0.5 - mc.getRenderManager().viewerPosZ;
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) n, (float) n2, (float) n3);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0f, 0.0f, 0.0f);
        GlStateManager.scale(-0.02266667f, -0.02266667f, -0.02266667f);
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        mc.fontRendererObj.drawString(this.progressStr, (float) (-mc.fontRendererObj.getStringWidth(this.progressStr) / 2), -3.0f, -1, true);
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.popMatrix();
    }

    private void setProgress() {
        switch ((int) mode.getInput()) {
            case 0: {
                this.progressStr = (int) (100.0 * (this.progress / 1.0)) + "%";
                break;
            }
            case 1: {
                double timeLeft = Utils.rnd((double) ((1.0f - this.progress) / BlockUtils.getBlockHardness(BlockUtils.getBlock(this.block), mc.thePlayer.getHeldItem(), false, false)) / 20.0, 1);
                this.progressStr = timeLeft == 0 ? "0" : timeLeft + "s";
                break;
            }
            case 2: {
                this.progressStr = String.valueOf(Utils.rnd(this.progress, 2));
                break;
            }
        }
    }

    public void onUpdate() {
        if (mc.thePlayer.capabilities.isCreativeMode || !mc.thePlayer.capabilities.allowEdit) {
            this.resetVariables();
            return;
        }
        if (bedAura.isToggled() && ModuleManager.bedAura != null && ModuleManager.bedAura.isEnabled() && ModuleManager.bedAura.breakProgress != 0.0f && ModuleManager.bedAura.currentBlock != null && !(BlockUtils.getBlock(ModuleManager.bedAura.currentBlock) instanceof BlockBed)) {
            this.progress = Math.min(1.0, ModuleManager.bedAura.breakProgress);
            this.block = ModuleManager.bedAura.currentBlock;
            if (this.block == null) {
                return;
            }
            this.setProgress();
            return;
        }
        if (!manual.isToggled() || mc.objectMouseOver == null || mc.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) {
            this.resetVariables();
            return;
        }
        try {
            this.progress = Reflection.curBlockDamageMP.getFloat(mc.playerController);
            if (this.progress == 0.0f) {
                this.resetVariables();
                return;
            }
            this.block = mc.objectMouseOver.getBlockPos();
            this.setProgress();
        } catch (IllegalAccessException ignored) {
        }
    }

    public void onDisable() {
        this.resetVariables();
    }

    private void resetVariables() {
        this.progress = 0.0f;
        this.block = null;
        this.progressStr = "";
    }
}
