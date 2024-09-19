package keystrokesmod.module.impl.world.scaffold.rotation;

import keystrokesmod.event.RotationEvent;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.impl.world.scaffold.IScaffoldRotation;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.aim.RotationData;
import org.jetbrains.annotations.NotNull;

public class SimpleTellyRotation extends IScaffoldRotation {
    private final SliderSetting startTellyTick;
    private final SliderSetting stopTellyTick;

    public SimpleTellyRotation(String name, @NotNull Scaffold parent) {
        super(name, parent);
        this.registerSetting(startTellyTick = new SliderSetting("Start telly tick", 1, 1, 8, 1));
        this.registerSetting(stopTellyTick = new SliderSetting("Stop telly tick", 6, 1, 8, 1));
    }

    @Override
    public @NotNull RotationData onRotation(float placeYaw, float placePitch, boolean forceStrict, @NotNull RotationEvent event) {
        if (noPlace()) {
            return new RotationData(event.getYaw(), event.getPitch());
        } else {
            return new RotationData(placeYaw, placePitch);
        }
    }

    private boolean noPlace() {
        return parent.offGroundTicks >= startTellyTick.getInput() && parent.offGroundTicks < stopTellyTick.getInput();
    }

    @Override
    public boolean onPreSchedulePlace() {
        return !noPlace();
    }
}
