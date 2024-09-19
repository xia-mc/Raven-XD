package keystrokesmod.module.impl.world.scaffold;

import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.setting.impl.SubMode;
import org.jetbrains.annotations.NotNull;

public abstract class IScaffoldSprint extends SubMode<Scaffold> {
    public IScaffoldSprint(String name, @NotNull Scaffold parent) {
        super(name, parent);
    }

    /**
     * Should we sprint?
     */
    public abstract boolean isSprint();

    /**
     * @return true means runs normally, false means stop scheduling.
     */
    public boolean onPreSchedulePlace() {
        return true;
    }
}
