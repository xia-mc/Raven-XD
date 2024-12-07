package keystrokesmod.module.impl.other.anticheats.checks.movement;

import keystrokesmod.module.impl.other.Anticheat;
import keystrokesmod.module.impl.other.anticheats.Check;
import keystrokesmod.module.impl.other.anticheats.TRPlayer;
import keystrokesmod.module.impl.other.anticheats.config.AdvancedConfig;
import org.jetbrains.annotations.NotNull;

public class SpeedB extends Check {
    public SpeedB(@NotNull TRPlayer player) {
        super("SpeedB", player);
    }

    @Override
    public void _onTick() {
        if (player.fabricPlayer.isSprinting() && player.fabricPlayer.getFoodStats().getFoodLevel() <= 6) {
            flag();
        }
    }

    @Override
    public int getAlertBuffer() {
        return AdvancedConfig.speedBAlertBuffer;
    }

    @Override
    public boolean isDisabled() {
        return !Anticheat.getMovementCheck().isToggled() || !Anticheat.getMovementCheckSpeedB().isToggled();
    }
}
