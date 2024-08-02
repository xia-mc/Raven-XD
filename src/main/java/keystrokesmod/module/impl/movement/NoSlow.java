package keystrokesmod.module.impl.movement;

import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.movement.noslow.*;
import keystrokesmod.module.setting.impl.ModeValue;

public class NoSlow extends Module {
    private final ModeValue mode;

    public NoSlow() {
        super("NoSlow", category.movement);
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new VanillaNoSlow("Vanilla", this))
                .add(new HypixelNoSlow("Hypixel", this))
                .add(new NCPNoSlow("NCP", this))
                .add(new IntaveNoSlow("Intave", this))
                .add(new OldIntaveNoSlow("Old Intave", this))
                .add(new OldGrimNoSlow("Old Grim", this))
                .add(new CustomNoSlow("Custom", this))
                .setDefaultValue("Hypixel")
        );
    }

    @Override
    public void onEnable() {
        mode.enable();
    }

    @Override
    public void onDisable() {
        mode.disable();
    }

    public static float getForwardSlowed() {
        if (!mc.thePlayer.isUsingItem()) return 1;
        if (!ModuleManager.noSlow.isEnabled()) return 0.2f;
        return ((INoSlow) ModuleManager.noSlow.mode.getSubModeValues().get((int) ModuleManager.noSlow.mode.getInput())).getSlowdown();
    }

    public static float getStrafeSlowed() {
        if (!mc.thePlayer.isUsingItem()) return 1;
        if (!ModuleManager.noSlow.isEnabled()) return 0.2f;
        return ((INoSlow) ModuleManager.noSlow.mode.getSubModeValues().get((int) ModuleManager.noSlow.mode.getInput())).getStrafeSlowdown();
    }
}
