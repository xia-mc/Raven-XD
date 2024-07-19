package keystrokesmod.module.impl.combat;


import keystrokesmod.module.Module;
import keystrokesmod.module.impl.combat.aimassist.*;
import keystrokesmod.module.setting.impl.ModeValue;

public class AimAssist extends Module {
    private final ModeValue mode;

    public AimAssist() {
        super("AimAssist", category.combat, 0);
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new OriginalAimAssist("Original", this))
                .add(new TejasAssist("TejasAssist", this))
                .setDefaultValue("Original"));
    }

    public String getInfo() {
        return mode.getSubModeValues().get((int) mode.getInput()).getName();
    }

    public void onEnable() {
        mode.enable();
    }

    public void onDisable() {
        mode.disable();
    }
}
