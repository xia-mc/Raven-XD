package keystrokesmod.module.impl.movement;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.movement.jesus.KarhuJesus;
import keystrokesmod.module.impl.movement.jesus.OldNCPJesus;
import keystrokesmod.module.impl.movement.jesus.VulcanJesus;
import keystrokesmod.module.setting.impl.ModeValue;

public class Jesus extends Module {
    private final ModeValue mode;

    public Jesus() {
        super("Jesus", category.movement);
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new KarhuJesus("Karhu", this))
                .add(new OldNCPJesus("Old NCP", this))
                .add(new VulcanJesus("Vulcan", this))
                .setDefaultValue("Karhu")
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

    @Override
    public String getInfo() {
        return mode.getSubModeValues().get((int) mode.getInput()).getPrettyName();
    }
}
