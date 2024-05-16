package keystrokesmod.module.impl.player;

import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class SafeWalk extends Module {
    private final SliderSetting shiftDelay;
    private final SliderSetting motion;
    public static ButtonSetting shift, blocksOnly, pitchCheck, disableOnForward;
    private boolean isSneaking;
    private long b = 0L;

    public SafeWalk() {
        super("SafeWalk", Module.category.player, 0);
        this.registerSetting(shiftDelay = new SliderSetting("Delay until next shift", 0.0, 0.0, 800.0, 10.0));
        this.registerSetting(motion = new SliderSetting("Motion", 1.0, 0.5, 1.2, 0.01));
        this.registerSetting(blocksOnly = new ButtonSetting("Blocks only", true));
        this.registerSetting(disableOnForward = new ButtonSetting("Disable on forward", false));
        this.registerSetting(pitchCheck = new ButtonSetting("Pitch check", false));
        this.registerSetting(shift = new ButtonSetting("Shift", false));
    }

    public void onDisable() {
        if (shift.isToggled() && Utils.overAir()) {
            this.setSneakState(false);
        }
        isSneaking = false;
    }

    public void onUpdate() {
        if (motion.getInput() != 1.0 && mc.thePlayer.onGround && Utils.isMoving() && (!pitchCheck.isToggled() || mc.thePlayer.rotationPitch >= 70.0f)) {
            Utils.setSpeed(Utils.getHorizontalSpeed() * motion.getInput());
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END) {
            return;
        }
        if (!shift.isToggled() || !Utils.nullCheck()) {
            return;
        }
        if (mc.thePlayer.onGround && Utils.overAir()) {
            if (blocksOnly.isToggled()) {
                final ItemStack getHeldItem = mc.thePlayer.getHeldItem();
                if (getHeldItem == null || !(getHeldItem.getItem() instanceof ItemBlock)) {
                    this.setSneakState(false);
                    return;
                }
            }
            if (disableOnForward.isToggled() && Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode())) {
                this.setSneakState(false);
                return;
            }
            if (pitchCheck.isToggled() && mc.thePlayer.rotationPitch < 70.0f) {
                this.setSneakState(false);
                return;
            }
            this.setSneakState(true);
        } else if (this.isSneaking) {
            this.setSneakState(false);
        }
        if (this.isSneaking && mc.thePlayer.capabilities.isFlying) {
            this.setSneakState(false);
        }
    }

    @SubscribeEvent
    public void onGuiOpen(final GuiOpenEvent guiOpenEvent) {
        if (shift.isToggled() && guiOpenEvent.gui == null) {
            this.isSneaking = mc.thePlayer.isSneaking();
        }
    }

    private void setSneakState(boolean down) {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), down);

        if (this.isSneaking) {
            if (down) {
                return;
            }
        }
        else if (!down) {
            return;
        }
        if (down) {
            final long n = (long) shiftDelay.getInput();
            if (n != 0L) {
                if (Utils.getDifference(this.b, System.currentTimeMillis()) < n) {
                    return;
                }
                this.b = System.currentTimeMillis();
            }
        }
        else {
            if (Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())) {
                return;
            }
            this.b = System.currentTimeMillis();
        }
        final int getKeyCode = mc.gameSettings.keyBindSneak.getKeyCode();
        this.isSneaking = down;
        KeyBinding.setKeyBindState(getKeyCode, down);
    }

    public static boolean canSafeWalk() {
        if (ModuleManager.safeWalk != null && ModuleManager.safeWalk.isEnabled()) {
            if (disableOnForward.isToggled() && Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode())) {
                return false;
            }
            if (pitchCheck.isToggled() && mc.thePlayer.rotationPitch < 70) {
                return false;
            }
            return !blocksOnly.isToggled() || (mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock);
        }
        return false;
    }
}
