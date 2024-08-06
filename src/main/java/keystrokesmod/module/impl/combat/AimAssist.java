package keystrokesmod.module.impl.combat;


import keystrokesmod.module.Module;
import keystrokesmod.module.impl.combat.aimassist.*;
import keystrokesmod.module.setting.impl.ModeValue;

public class AimAssist extends Module {
    private final ModeValue mode;

    public AimAssist() {
        super("AimAssist", category.combat, "Smoothly aims to closet valid target");
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new NormalAimAssist("Normal", this))
                .add(new TejasAssist("Tejas", this))
                .setDefaultValue("Original"));
    }

    public void onEnable() {
        mode.enable();
    }

    public void onDisable() {
        mode.disable();
    }
}
