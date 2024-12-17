package keystrokesmod.module.impl.other.anticheats.checks.combat;

import keystrokesmod.module.impl.other.Anticheat;
import keystrokesmod.module.impl.other.anticheats.Check;
import keystrokesmod.module.impl.other.anticheats.TRPlayer;
import keystrokesmod.module.impl.other.anticheats.config.AdvancedConfig;
import keystrokesmod.module.impl.other.anticheats.utils.world.LevelUtils;
import keystrokesmod.script.classes.Vec3;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;

import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

public class ReachA extends Check {
    public ReachA(@NotNull TRPlayer player) {
        super("*ReachA*", player);
    }

    @Override
    public void _onTick() {
        if (player.currentSwing && !player.lastSwing) {  // 第1个挥手tick
            onSwing();
        }
    }

    private void onSwing() {
        if (isDisabled()) return;

        player.timeTask.schedule(() -> {
            try {
                EntityLivingBase possibleTarget = LevelUtils.getEntities().stream()
                        .filter(entity -> !(entity == player.fabricPlayer))
                        .filter(entity -> entity.hurtTime >= 10 - AdvancedConfig.reachACheckDelay)
                        .min((e1, e2) -> (int) ((new Vec3(e1).distanceTo(player.fabricPlayer) - new Vec3(e2).distanceTo(player.fabricPlayer)) * 100))
                        .orElseThrow(NoSuchElementException::new);
                double distance = new Vec3(possibleTarget).distanceTo(player.fabricPlayer);
                if (distance < 6 && distance > AdvancedConfig.reachADefaultReach) {  // 满足标记条件
                    flag(String.format("target: %s  distance: %.2f", possibleTarget.getName(), distance));
                }
            } catch (NoSuchElementException ignored) {
            }
        }, AdvancedConfig.reachACheckDelay * 50L, TimeUnit.MILLISECONDS);

    }

    @Override
    public int getAlertBuffer() {
        return AdvancedConfig.reachAAlertBuffer;
    }

    @Override
    public boolean isDisabled() {
        return !Anticheat.getCombatCheck().isToggled() || !Anticheat.getCombatCheckReachA().isToggled() || !Anticheat.getExperimentalMode().isToggled();
    }
}
