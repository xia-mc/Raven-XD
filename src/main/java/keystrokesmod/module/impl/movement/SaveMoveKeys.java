package keystrokesmod.module.impl.movement;

import keystrokesmod.Raven;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

import java.util.concurrent.TimeUnit;

public class SaveMoveKeys extends Module {
    private final SliderSetting delay;
    private boolean lastInGUI = false;
    public SaveMoveKeys() {
        super("SaveMoveKeys", category.movement);
        this.registerSetting(new DescriptionSetting("re-press movement keys when close gui."));
        this.registerSetting(delay = new SliderSetting("Delay", 0, 0, 1000, 10));
    }

    @Override
    public void onUpdate() {
        if (mc.currentScreen != null) {
            lastInGUI = true;
        } else {
            if (lastInGUI) {
                Raven.getExecutor().schedule(() -> {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()));
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode()));
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode()));
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode()));
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindSprint.getKeyCode()));
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode()));
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), Utils.jumpDown());
                    KeyBinding.onTick(mc.gameSettings.keyBindForward.getKeyCode());
                    KeyBinding.onTick(mc.gameSettings.keyBindBack.getKeyCode());
                    KeyBinding.onTick(mc.gameSettings.keyBindLeft.getKeyCode());
                    KeyBinding.onTick(mc.gameSettings.keyBindRight.getKeyCode());
                    KeyBinding.onTick(mc.gameSettings.keyBindSprint.getKeyCode());
                    KeyBinding.onTick(mc.gameSettings.keyBindSneak.getKeyCode());
                    KeyBinding.onTick(mc.gameSettings.keyBindJump.getKeyCode());
                }, (long) delay.getInput(), TimeUnit.MILLISECONDS);
            }

            lastInGUI = false;
        }
    }
}
