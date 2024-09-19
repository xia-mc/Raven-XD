package keystrokesmod.module.impl.world.scaffold.sprint;

import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.impl.world.scaffold.IScaffoldSprint;
import org.jetbrains.annotations.NotNull;

public class DisabledSprint extends IScaffoldSprint {
    public DisabledSprint(String name, @NotNull Scaffold parent) {
        super(name, parent);
    }

    @Override
    public boolean isSprint() {
        return false;
    }
}
