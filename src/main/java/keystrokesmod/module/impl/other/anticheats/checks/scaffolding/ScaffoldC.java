package keystrokesmod.module.impl.other.anticheats.checks.scaffolding;

import keystrokesmod.module.impl.other.Anticheat;
import keystrokesmod.module.impl.other.anticheats.Check;
import keystrokesmod.module.impl.other.anticheats.TRPlayer;

public class ScaffoldC extends Check {
    public ScaffoldC(TRPlayer player) {
        super("ScaffoldC", player);
    }

    @Override
    public void _onPlaceBlock() {
        if (player.currentRot.x == 85.0) {
            flag("Backwards rotation.");
        }
    }

    @Override
    public int getAlertBuffer() {
        return 2;
    }

    @Override
    public boolean isDisabled() {
        return !Anticheat.getScaffoldingCheck().isToggled() || !Anticheat.getScaffoldingCheckScaffoldC().isToggled();
    }
}
