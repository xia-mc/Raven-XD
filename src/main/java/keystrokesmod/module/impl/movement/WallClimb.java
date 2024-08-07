package keystrokesmod.module.impl.movement;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.movement.wallclimb.TestWallClimb;
import keystrokesmod.module.impl.movement.wallclimb.IntaveWallClimb;
import keystrokesmod.module.impl.movement.wallclimb.VulcanWallClimb;
import keystrokesmod.module.setting.impl.ModeValue;

public class WallClimb extends Module {
    private final ModeValue mode;

    public WallClimb() {
        super("WallClimb", category.movement);
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new IntaveWallClimb("Intave", this))
                .add(new VulcanWallClimb("Vulcan", this))
                .add(new TestWallClimb("Test", this))
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
