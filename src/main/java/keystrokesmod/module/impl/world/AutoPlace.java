package keystrokesmod.module.impl.world;

import keystrokesmod.event.PostUpdateEvent;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Reflection;
import keystrokesmod.utility.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

public class AutoPlace extends Module {
    private final SliderSetting frameDelay;
    private final SliderSetting minPlaceDelay;
    private final ButtonSetting disableLeft;
    private final ButtonSetting holdRight;
    private final ButtonSetting fastPlaceJump;
    private final ButtonSetting pitchCheck;
    private final ButtonSetting postPlace;

    private double fDelay = 0.0D;
    private long l = 0L;
    private int f = 0;
    private MovingObjectPosition lm = null;
    private BlockPos lp = null;

    public AutoPlace() {
        super("AutoPlace", category.world, 0);
        this.registerSetting(new DescriptionSetting("Best with safewalk."));
        this.registerSetting(frameDelay = new SliderSetting("Frame delay", 8.0D, 0.0D, 30.0D, 1.0D));
        this.registerSetting(minPlaceDelay = new SliderSetting("Min place delay", 60.0, 1.0, 500.0, 5.0));
        this.registerSetting(disableLeft = new ButtonSetting("Disable left", false));
        this.registerSetting(holdRight = new ButtonSetting("Hold right", true));
        this.registerSetting(fastPlaceJump = new ButtonSetting("Fast place on jump", true));
        this.registerSetting(pitchCheck = new ButtonSetting("Pitch check", false));
        this.registerSetting(postPlace = new ButtonSetting("Post place", false, "Place block on PostUpdate event"));
    }

    public void guiUpdate() {
        if (this.fDelay != frameDelay.getInput()) {
            this.resetVariables();
        }

        this.fDelay = frameDelay.getInput();
    }

    public void onDisable() {
        if (holdRight.isToggled()) {
            this.rd(4);
        }

        this.resetVariables();
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        if (!postPlace.isToggled())
            action();
    }

    @SubscribeEvent
    public void onPostUpdate(PostUpdateEvent event) {
        if (postPlace.isToggled())
            action();
    }

    private void action() {
        if (mc.currentScreen != null || mc.thePlayer.capabilities.isFlying) {
            return;
        }
        final ItemStack getHeldItem = SlotHandler.getHeldItem();
        if (getHeldItem == null || !(getHeldItem.getItem() instanceof ItemBlock)) {
            return;
        }
        if (fastPlaceJump.isToggled() && holdRight.isToggled() && !ModuleManager.fastPlace.isEnabled() && Mouse.isButtonDown(1)) {
            if (mc.thePlayer.motionY > 0.0) {
                this.rd(1);
            } else if (!pitchCheck.isToggled()) {
                this.rd(1000);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void bh(DrawBlockHighlightEvent ev) {
        if (Utils.nullCheck()) {
            if (mc.currentScreen == null && !mc.thePlayer.capabilities.isFlying) {
                ItemStack i = mc.thePlayer.getHeldItem();
                if (i != null && i.getItem() instanceof ItemBlock) {
                    MovingObjectPosition m = mc.objectMouseOver;
                    if (disableLeft.isToggled() && Mouse.isButtonDown(0)) {
                        return;
                    }
                    if (m != null && m.typeOfHit == MovingObjectType.BLOCK && m.sideHit != EnumFacing.UP && m.sideHit != EnumFacing.DOWN) {
                        if (this.lm != null && (double) this.f < frameDelay.getInput()) {
                            ++this.f;
                        } else {
                            this.lm = m;
                            BlockPos pos = m.getBlockPos();
                            if (this.lp == null || pos.getX() != this.lp.getX() || pos.getY() != this.lp.getY() || pos.getZ() != this.lp.getZ()) {
                                Block b = mc.theWorld.getBlockState(pos).getBlock();
                                if (b != null && b != Blocks.air && !(b instanceof BlockLiquid)) {
                                    if (!holdRight.isToggled() || Mouse.isButtonDown(1)) {
                                        long n = System.currentTimeMillis();
                                        if (n - this.l >= minPlaceDelay.getInput()) {
                                            this.l = n;
                                            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, i, pos, m.sideHit, m.hitVec)) {
                                                Reflection.setButton(1, true);
                                                mc.thePlayer.swingItem();
                                                mc.getItemRenderer().resetEquippedProgress();
                                                Reflection.setButton(1, false);
                                                this.lp = pos;
                                                this.f = 0;
                                            }

                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void rd(int i) {
        try {
            if (Reflection.rightClickDelayTimerField != null) {
                Reflection.rightClickDelayTimerField.set(mc, i);
            }
        } catch (IllegalAccessException | IndexOutOfBoundsException ignored) {
        }
    }

    private void resetVariables() {
        this.lp = null;
        this.lm = null;
        this.f = 0;
    }
}
