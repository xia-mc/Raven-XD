package keystrokesmod.module.impl.movement;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.movement.step.HypixelStep;
import keystrokesmod.module.setting.impl.ModeValue;

public class Step extends Module {
    private final ModeValue mode;

    public Step() {
        super("Step", category.movement);
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new HypixelStep("Hypixel", this))
        );
    }

    @Override
    public String getInfo() {
        return mode.getSubModeValues().get((int) mode.getInput()).getPrettyName();
    }
}
