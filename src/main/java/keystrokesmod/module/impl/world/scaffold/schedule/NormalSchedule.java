package keystrokesmod.module.impl.world.scaffold.schedule;

import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.impl.world.scaffold.IScaffoldSchedule;
import org.jetbrains.annotations.NotNull;

public class NormalSchedule extends IScaffoldSchedule {
    public NormalSchedule(String name, @NotNull Scaffold parent) {
        super(name, parent);
    }

    @Override
    public boolean noPlace() {
        return false;
    }

    @Override
    public boolean noRotation() {
        return false;
    }
}
