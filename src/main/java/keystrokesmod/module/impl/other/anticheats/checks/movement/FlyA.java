package keystrokesmod.module.impl.other.anticheats.checks.movement;

import keystrokesmod.module.impl.other.Anticheat;
import keystrokesmod.module.impl.other.anticheats.Check;
import keystrokesmod.module.impl.other.anticheats.TRPlayer;
import keystrokesmod.module.impl.other.anticheats.config.AdvancedConfig;
import keystrokesmod.module.impl.other.anticheats.utils.world.PlayerMove;
import org.jetbrains.annotations.NotNull;

public class FlyA extends Check {
    public FlyA(@NotNull TRPlayer player) {
        super("FlyA", player);
    }

    @Override
    public void _onTick() {
        if (PlayerMove.isInvalidMotion(player.currentMotion)) return;
        if (PlayerMove.isNoMove(player.currentMotion) || player.currentOnGround) return;

        if (player.lastMotion.y() == 0 && player.currentMotion.y() == 0) {
            flag(String.format("Invalid Y-motion: %.2f  onGround=%s", player.currentMotion.y(), player.currentOnGround));
        }
    }

    @Override
    public int getAlertBuffer() {
        return AdvancedConfig.flyAAlertBuffer;
    }

    @Override
    public boolean isDisabled() {
        return !Anticheat.getMovementCheck().isToggled();
    }
}
