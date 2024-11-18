package keystrokesmod.module.impl.player;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Reflection;
import keystrokesmod.utility.RotationUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;

public class AntiAFK extends Module {
    public boolean stop = false;
    private final ModeSetting afk;
    private final ButtonSetting jump;
    private final ButtonSetting jumpWhenCollided;
    private final ButtonSetting randomClicks;
    private final ButtonSetting swapItem;
    private final ModeSetting spin;
    private final ButtonSetting randomizeDelta;
    private final ButtonSetting randomizePitch;
    private final SliderSetting minDelay;
    private final SliderSetting maxDelay;
    private final String[] afkModes = new String[]{"None", "Wander", "Lateral shuffle", "Forward", "Backward"};
    private final String[] spinModes = new String[]{"None", "Random", "Right", "Left"};
    private int ticks;
    private boolean c;

    public AntiAFK() {
        super("AntiAFK", category.player);
        this.registerSetting(afk = new ModeSetting("AFK", afkModes, 0));
        this.registerSetting(jump = new ButtonSetting("Jump", false));
        this.registerSetting(jumpWhenCollided = new ButtonSetting("Jump only when collided", false));
        this.registerSetting(randomClicks = new ButtonSetting("Random clicks", false));
        this.registerSetting(swapItem = new ButtonSetting("Swap item", false));
        this.registerSetting(spin = new ModeSetting("Spin", spinModes, 0));
        this.registerSetting(randomizeDelta = new ButtonSetting("Randomize delta", true));
        this.registerSetting(randomizePitch = new ButtonSetting("Randomize pitch", true));
        this.registerSetting(minDelay = new SliderSetting("Minimum delay ticks", 10.0, 4.0, 160.0, 2.0));
        this.registerSetting(maxDelay = new SliderSetting("Maximum delay ticks", 80.0, 4.0, 160.0, 2.0));
    }

    public void onEnable() {
        this.ticks = this.h();
        this.c = Utils.getRandom().nextBoolean();
    }

    public void onUpdate() {
        if (stop) {
            return;
        }
        if (mc.currentScreen != null && !(mc.currentScreen instanceof GuiChat)) {
            return;
        }
        --this.ticks;
        switch ((int) afk.getInput()) {
            case 1: {
                if (this.c) {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), Utils.getRandom().nextBoolean());
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), Utils.getRandom().nextBoolean());
                    break;
                }
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), Utils.getRandom().nextBoolean());
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), Utils.getRandom().nextBoolean());
                break;
            }
            case 2: {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), this.c);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), !this.c);
                break;
            }
            case 3: {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);
                break;
            }
            case 4: {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), true);
                break;
            }
        }
        switch ((int) spin.getInput()) {
            case 1: {
                mc.thePlayer.rotationYaw += this.c(this.c);
                this.d();
                break;
            }
            case 2: {
                mc.thePlayer.rotationYaw += this.c(true);
                this.d();
                break;
            }
            case 3: {
                mc.thePlayer.rotationYaw += this.c(false);
                this.d();
                break;
            }
        }
        if (jump.isToggled() && mc.thePlayer.onGround && (!jumpWhenCollided.isToggled() || mc.thePlayer.isCollidedHorizontally)) {
            mc.thePlayer.jump();
        }
        if (this.ticks == 0) {
            if (swapItem.isToggled()) {
                mc.thePlayer.inventory.currentItem = Utils.randomizeInt(0, 8);
            }
            if (randomClicks.isToggled()) {
                Reflection.clickMouse();
            }
            this.ticks = this.h();
            this.c = !this.c;
        }
    }

    private double a() {
        final int n = Utils.getRandom().nextBoolean() ? 1 : -1;
        if (!randomizeDelta.isToggled()) {
            return 2 * n;
        }
        double n2 = Utils.randomizeInt(100, 500) / 100.0;
        if (n2 % 1.0 == 0.0) {
            n2 += Utils.randomizeInt(1, 10) / 10.0 * n;
        }
        return n2 * n;
    }

    public void onDisable() {
        this.b(0);
        stop = false;
    }

    private void b(final int n) {
        switch (n) {
            case 1: {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), false);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
                break;
            }
            case 2: {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
                break;
            }
            case 3: {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
                break;
            }
            case 4: {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), false);
                break;
            }
        }
    }

    private int h() {
        if (minDelay.getInput() == maxDelay.getInput()) {
            return (int) minDelay.getInput();
        }
        return Utils.randomizeInt((int) minDelay.getInput(), (int) maxDelay.getInput());
    }

    private void d() {
        if (randomizePitch.isToggled()) {
            mc.thePlayer.rotationPitch = RotationUtils.clampTo90((float) (mc.thePlayer.rotationPitch + this.a()));
        }
    }

    private double c(final boolean b) {
        final int n = b ? 1 : -1;
        if (!randomizeDelta.isToggled()) {
            return 3 * n;
        }
        double n2 = Utils.randomizeInt(100, 1000) / 100.0;
        if (n2 % 1.0 == 0.0) {
            n2 += Utils.randomizeInt(1, 10) / 10.0 * n;
        }
        return n2 * n;
    }
}
