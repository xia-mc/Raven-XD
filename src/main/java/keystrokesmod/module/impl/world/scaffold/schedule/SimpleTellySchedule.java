package keystrokesmod.module.impl.world.scaffold.schedule;

import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.impl.world.scaffold.IScaffoldSchedule;
import keystrokesmod.module.setting.impl.SliderSetting;
import org.jetbrains.annotations.NotNull;

public class SimpleTellySchedule extends IScaffoldSchedule {
    private final SliderSetting startTellyTick;
    private final SliderSetting stopTellyTick;

    public SimpleTellySchedule(String name, @NotNull Scaffold parent) {
        super(name, parent);
        this.registerSetting(startTellyTick = new SliderSetting("Start telly tick", 1, 1, 8, 1));
        this.registerSetting(stopTellyTick = new SliderSetting("Stop telly tick", 6, 1, 8, 1));
    }

    @Override
    public boolean noRotation() {
        return noPlace();
    }

    @Override
    public boolean noPlace() {
        return parent.offGroundTicks >= startTellyTick.getInput() && parent.offGroundTicks < stopTellyTick.getInput();
    }
}
