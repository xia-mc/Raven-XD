package keystrokesmod.module.impl.movement;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;

public class MotionModifier extends Module {
    private final ButtonSetting blockJumpInput;
    private final ButtonSetting onlyWhileMoving;
    private final ButtonSetting onlyWhileJumpKeyPressed;
    private final ButtonSetting onlyInWater;
    private final ButtonSetting onlyInWeb;
    private final ButtonSetting onlyAtLadder;
    private final ButtonSetting waitForDamage;


    public MotionModifier() {
        super("MotionModifier", category.movement);
        this.registerSetting(blockJumpInput = new ButtonSetting("Block jump input", false));
        this.registerSetting(onlyWhileMoving = new ButtonSetting("Only while moving", true));
        this.registerSetting(onlyWhileJumpKeyPressed = new ButtonSetting("Only while jump key pressed", false));
        this.registerSetting(onlyInWater = new ButtonSetting("Only in water", false));
        this.registerSetting(onlyInWeb = new ButtonSetting("Only in web", false));
        this.registerSetting(onlyAtLadder = new ButtonSetting("Only at ladder", false));
        this.registerSetting(waitForDamage = new ButtonSetting("Wait for damage", false));
    }
}
