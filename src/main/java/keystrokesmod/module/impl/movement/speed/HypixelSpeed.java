package keystrokesmod.module.impl.movement.speed;

import keystrokesmod.module.impl.movement.Speed;
import keystrokesmod.module.impl.movement.speed.hypixel.*;
import keystrokesmod.module.setting.impl.ModeValue;
import keystrokesmod.module.setting.impl.SubMode;
import org.jetbrains.annotations.NotNull;

public class HypixelSpeed extends SubMode<Speed> {
    private final ModeValue mode;

    public HypixelSpeed(String name, @NotNull Speed parent) {
        super(name, parent);
        this.registerSetting(mode = new ModeValue("Hypixel mode", this)
                .add(new GroundStrafeSpeed("GroundStrafe", this))
                .add(new RiseWatchdogSpeed("Rise", this))
                .add(new HypixelGroundSpeed("Ground", this))
                .add(new HypixelLowHopSpeed("Disabler", this))
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
}
