package keystrokesmod.module.impl.other.anticheats.checks.movement;

import keystrokesmod.module.impl.other.Anticheat;
import keystrokesmod.module.impl.other.anticheats.Check;
import keystrokesmod.module.impl.other.anticheats.TRPlayer;
import keystrokesmod.module.impl.other.anticheats.config.AdvancedConfig;
import keystrokesmod.module.impl.other.anticheats.utils.world.PlayerMove;
import net.minecraft.util.MathHelper;
import org.jetbrains.annotations.NotNull;

public class SpeedC extends Check {
    public SpeedC(@NotNull TRPlayer player) {
        super("SpeedC", player);
    }

    @Override
    public void _onTick() {
        if (player.fabricPlayer.isSprinting()) {
            double speed = PlayerMove.getXzTickSpeed(
                    player.posHistory.get(Math.min(MathHelper.floor_float(player.latency / 50f) + 1, 19)), player.currentPos
            );
            if (speed == 0) {
                flag(String.format("MotionX:%.2f MotionZ:%.2f", player.currentMotion.x(), player.currentMotion.z()));
            }
        }
    }

    @Override
    public int getAlertBuffer() {
        return AdvancedConfig.speedCAlertBuffer;
    }

    @Override
    public boolean isDisabled() {
        return !Anticheat.getMovementCheck().isToggled() || !Anticheat.getMovementCheckSpeedC().isToggled();
    }
}
