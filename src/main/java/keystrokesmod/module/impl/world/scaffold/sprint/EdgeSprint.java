package keystrokesmod.module.impl.world.scaffold.sprint;

import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.impl.world.scaffold.IScaffoldSprint;
import keystrokesmod.utility.Utils;
import org.jetbrains.annotations.NotNull;

public class EdgeSprint extends IScaffoldSprint {
    public EdgeSprint(String name, @NotNull Scaffold parent) {
        super(name, parent);
    }

    @Override
    public boolean isSprint() {
        return Utils.onEdge() || Utils.overAir();
    }
}
