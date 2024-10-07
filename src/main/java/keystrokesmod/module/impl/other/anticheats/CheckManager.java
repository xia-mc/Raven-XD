package keystrokesmod.module.impl.other.anticheats;

import keystrokesmod.module.impl.other.anticheats.utils.world.EntityUtils;
import keystrokesmod.module.impl.other.anticheats.checks.aim.*;
import keystrokesmod.module.impl.other.anticheats.checks.combat.*;
import keystrokesmod.module.impl.other.anticheats.checks.movement.*;
import keystrokesmod.module.impl.other.anticheats.checks.scaffolding.*;
import net.minecraft.world.WorldSettings;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CheckManager {
    private final TRPlayer player;
    private final @NotNull Map<Class<? extends Check>, Check> preChecks;
    private final @NotNull Map<Class<? extends Check>, Check> normalChecks;
    private final @NotNull Map<Class<? extends Check>, Check> postChecks;
    public short disableTick;
    public CheckManager(@NotNull Map<Class<? extends Check>, Check> preChecks,
                        @NotNull Map<Class<? extends Check>, Check> normalChecks,
                        @NotNull Map<Class<? extends Check>, Check> postChecks, TRPlayer player) {
        this.player = player;
        this.preChecks = new HashMap<>(preChecks);
        this.normalChecks = new HashMap<>(normalChecks);
        this.postChecks = new HashMap<>(postChecks);
        this.disableTick = 30;
    }

    @Contract("_ -> new")
    public static @NotNull CheckManager create(@NotNull TRPlayer player) {
        final Map<Class<? extends Check>, Check> pre = new HashMap<>();
        final Map<Class<? extends Check>, Check> normal = new HashMap<>();
        final Map<Class<? extends Check>, Check> post = new HashMap<>();
        pre.put(GroundSpoofA.class, new GroundSpoofA(player));
        pre.put(GroundSpoofB.class, new GroundSpoofB(player));
        normal.put(FlyA.class, new FlyA(player));
        normal.put(BlinkA.class, new BlinkA(player));
        normal.put(SpeedA.class, new SpeedA(player));
        normal.put(SpeedB.class, new SpeedB(player));
        normal.put(NoSlowA.class, new NoSlowA(player));
        normal.put(AutoBlockA.class, new AutoBlockA(player));
        normal.put(MotionA.class, new MotionA(player));
        normal.put(ReachA.class, new ReachA(player));
        normal.put(AimA.class, new AimA(player));
        normal.put(AimB.class, new AimB(player));
        normal.put(AimC.class, new AimC(player));
        normal.put(ScaffoldA.class, new ScaffoldA(player));
        normal.put(ScaffoldB.class, new ScaffoldB(player));
        normal.put(NoFallA.class, new NoFallA(player));
        normal.put(AutoClickerA.class, new AutoClickerA(player));

        return new CheckManager(pre, normal, post, player);
    }

    public static @NotNull CheckManager createSelf(@NotNull TRSelf player) {
        return create(player);
    }

    public void update() {
        if (disableTick > 0) {
            disableTick--;
        }
        if (player.currentGameType != player.lastGameType) {
            for (Check check : preChecks.values()) check._onGameTypeChange();
            for (Check check : normalChecks.values()) check._onGameTypeChange();
            for (Check check : postChecks.values()) check._onGameTypeChange();
        }

        if (player.currentGameType == WorldSettings.GameType.CREATIVE || player.currentGameType == WorldSettings.GameType.SPECTATOR) return;
        if (player.fabricPlayer.capabilities.isFlying) return;  // bro 1.8.9 is soooooooo special
        if (player.lastOnGround && !player.currentOnGround) onJump();

        if (EntityUtils.isOnPlaceBlock(player))
            onCustomAction(Check::_onPlaceBlock);

        for (Check check : preChecks.values()) check._onTick();
        for (Check check : normalChecks.values()) check._onTick();
        for (Check check : postChecks.values()) check._onTick();
    }

    public void onJump() {
        player.jumping = true;
        for (Check check : preChecks.values()) check._onJump();
        for (Check check : normalChecks.values()) check._onJump();
        for (Check check : postChecks.values()) check._onJump();
    }

    public void onCustomAction(Consumer<Check> action) {
        if (player == null) return;
        for (Check check : preChecks.values()) action.accept(check);
        for (Check check : normalChecks.values()) action.accept(check);
        for (Check check : postChecks.values()) action.accept(check);
    }
}
