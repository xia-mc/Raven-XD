package keystrokesmod.module.impl.other.anticheats.checks.scaffolding;

import keystrokesmod.module.impl.other.Anticheat;
import keystrokesmod.module.impl.other.anticheats.Check;
import keystrokesmod.module.impl.other.anticheats.TRPlayer;
import keystrokesmod.module.impl.other.anticheats.config.AdvancedConfig;
import org.jetbrains.annotations.NotNull;

public class ScaffoldA extends Check {
    public ScaffoldA(@NotNull TRPlayer player) {
        super("*ScaffoldA*", player);
    }

    @Override
    public void _onPlaceBlock() {
        if (!player.currentSwing) {
            flag("no valid swing.");
        }
    }

    @Override
    public int getAlertBuffer() {
        return 10;
    }

    @Override
    public boolean isDisabled() {
        return !Anticheat.getScaffoldingCheck().isToggled() || !Anticheat.getExperimentalMode().isToggled();
    }
}
