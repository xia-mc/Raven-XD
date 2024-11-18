package keystrokesmod.module.impl.other.anticheats.checks.combat;

import keystrokesmod.module.impl.other.Anticheat;
import keystrokesmod.module.impl.other.anticheats.Check;
import keystrokesmod.module.impl.other.anticheats.TRPlayer;
import keystrokesmod.module.impl.other.anticheats.config.AdvancedConfig;
import keystrokesmod.module.impl.other.anticheats.utils.world.PlayerMove;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NoSlowA extends Check {
    public static final List<Double> SLOW_SPEED = new ArrayList<>();
    public short itemUseTick = 0;
    public short disableTick = 0;  // 跳跃弱检测

    public NoSlowA(@NotNull TRPlayer player) {
        super("NoSlowA", player);
        SLOW_SPEED.add(2.56);
        SLOW_SPEED.add(1.92);
        SLOW_SPEED.add(1.6);
        SLOW_SPEED.add(1.4);
        SLOW_SPEED.add(1.36);
        SLOW_SPEED.add(1.26);
        SLOW_SPEED.add(1.18);
        SLOW_SPEED.add(1.16);
    }

    @Override
    public void _onTick() {
        if (!player.fabricPlayer.isUsingItem() || !player.lastUsingItem) {
            itemUseTick = 0;
            return;  // 当连续两个tick使用物品才检查
        }
        if (player.jumping) {
            disableTick = AdvancedConfig.getNoSlowAInJumpDisableTick();
            return;
        }
        if (disableTick > 0) {
            disableTick--;
            return;
        }

        final double secSpeed = PlayerMove.getXzSecSpeed(player.lastPos, player.currentPos);
        final double possibleSpeed = SLOW_SPEED.get(itemUseTick) * player.speedMul + Anticheat.getThreshold().getInput();
        if (secSpeed > possibleSpeed) {
            flag(String.format("Current: %.2f  Max: %.2f", secSpeed, possibleSpeed));
        }
        if (itemUseTick < SLOW_SPEED.size() - 1) itemUseTick++;
    }

    @Override
    public int getAlertBuffer() {
        return AdvancedConfig.noSlowAAlertBuffer;
    }

    @Override
    public boolean isDisabled() {
        return !Anticheat.getCombatCheck().isToggled();
    }
}
