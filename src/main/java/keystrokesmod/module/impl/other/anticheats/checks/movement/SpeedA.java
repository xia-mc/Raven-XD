package keystrokesmod.module.impl.other.anticheats.checks.movement;

import keystrokesmod.module.impl.other.Anticheat;
import keystrokesmod.module.impl.other.anticheats.Check;
import keystrokesmod.module.impl.other.anticheats.TRPlayer;
import keystrokesmod.module.impl.other.anticheats.config.AdvancedConfig;
import keystrokesmod.module.impl.other.anticheats.utils.world.PlayerMove;
import org.jetbrains.annotations.NotNull;

public class SpeedA extends Check {
    public boolean hasJumped = false;
    public short jumpTick = 0;

    public SpeedA(@NotNull TRPlayer player) {
        super("SpeedA", player);
    }

    @Override
    public void _onTick() {
        if (hasJumped && !player.jumping) {
            hasJumped = false;
            jumpTick = AdvancedConfig.getSpeedAAfterJumpJumpTick();
            return;
        }

        if (jumpTick > 0) jumpTick--;

        // check if player is on ground (not in liquid or in water)
        if (player.lastPos == null || player.hasSetback || !player.currentOnGround || !player.lastOnGround || player.fabricPlayer.isInWater())
            return;

        double maxSecSpeed;
        if (jumpTick > 0)
            maxSecSpeed = AdvancedConfig.speedAAfterJumpSpeed;
        else if (player.fabricPlayer.isSprinting())
            maxSecSpeed = AdvancedConfig.speedASprintingSpeed;
        else if (player.fabricPlayer.isSilent())
            maxSecSpeed = AdvancedConfig.speedASilentSpeed;
        else  // walking
            maxSecSpeed = AdvancedConfig.speedAWalkSpeed;

        final double speed = PlayerMove.getXzSecSpeed(player.lastPos, player.currentPos);
        final double possibleSpeed = maxSecSpeed * player.speedMul + Anticheat.getThreshold().getInput();
        if (speed > possibleSpeed) {
            flag(String.format("Current: %.2f  Max: %.2f", speed, possibleSpeed));
        }
    }

    @Override
    public void _onJump() {
        hasJumped = true;
    }

    @Override
    public int getAlertBuffer() {
        return AdvancedConfig.speedAAlertBuffer;
    }

    @Override
    public boolean isDisabled() {
        return !Anticheat.getMovementCheck().isToggled();
    }
}
