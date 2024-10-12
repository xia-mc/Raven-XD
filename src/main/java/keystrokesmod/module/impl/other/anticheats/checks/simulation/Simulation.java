package keystrokesmod.module.impl.other.anticheats.checks.simulation;

import keystrokesmod.module.impl.other.anticheats.Check;
import keystrokesmod.module.impl.other.anticheats.TRPlayer;
import org.jetbrains.annotations.NotNull;

public class Simulation extends Check {
    public Simulation(String checkName, @NotNull TRPlayer player) {
        super(checkName, player);
    }

    @Override
    public int getAlertBuffer() {
        return 10;
    }

    @Override
    public boolean isDisabled() {
        return false;
    }
}
