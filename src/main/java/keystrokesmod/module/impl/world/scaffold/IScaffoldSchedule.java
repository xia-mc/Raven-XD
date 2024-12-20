package keystrokesmod.module.impl.world.scaffold;

import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.setting.impl.SubMode;
import org.jetbrains.annotations.NotNull;

public abstract class IScaffoldSchedule extends SubMode<Scaffold> {
    public IScaffoldSchedule(String name, @NotNull Scaffold parent) {
        super(name, parent);
    }

    /**
     * @return true means runs normally, false means stop scheduling.
     */
    public abstract boolean noPlace();

    public abstract boolean noRotation();
}
