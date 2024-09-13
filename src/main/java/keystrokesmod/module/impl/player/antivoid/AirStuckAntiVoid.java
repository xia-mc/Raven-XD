package keystrokesmod.module.impl.player.antivoid;

import keystrokesmod.module.impl.movement.AirStuck;
import keystrokesmod.module.impl.player.AntiVoid;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.Utils;
import org.jetbrains.annotations.NotNull;

public class AirStuckAntiVoid extends SubMode<AntiVoid> {
    private final AirStuck airStuck = new AirStuck();
    private final SliderSetting distance;

    public AirStuckAntiVoid(String name, @NotNull AntiVoid parent) {
        super(name, parent);
        this.registerSetting(airStuck.getSettings());
        this.registerSetting(distance = new SliderSetting("Distance", 5, 0, 10, 1));
    }

    @Override
    public void onUpdate() throws Throwable {
        if (mc.thePlayer.fallDistance > distance.getInput() && Utils.overVoid() && !mc.thePlayer.onGround) {
            airStuck.enable();
        } else {
            airStuck.disable();
        }
    }

    @Override
    public void onDisable() throws Throwable {
        airStuck.disable();
    }
}
