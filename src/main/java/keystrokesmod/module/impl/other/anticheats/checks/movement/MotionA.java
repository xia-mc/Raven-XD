package keystrokesmod.module.impl.other.anticheats.checks.movement;

import keystrokesmod.module.impl.other.anticheats.Check;
import keystrokesmod.module.impl.other.anticheats.TRPlayer;
import keystrokesmod.module.impl.other.anticheats.config.AdvancedConfig;
import keystrokesmod.module.impl.other.anticheats.utils.world.BlockUtils;
import keystrokesmod.module.impl.other.anticheats.utils.world.LevelUtils;
import keystrokesmod.module.impl.other.anticheats.utils.world.PlayerMove;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

public class MotionA extends Check {
    public static final List<Block> IGNORED_BLOCKS = new ArrayList<>();
    public static final List<Double> JUMP_MOTIONS = new ArrayList<>();
    public static final List<Double> JUMP_MOTIONS_1 = new ArrayList<>();
    public static final List<Double> JUMP_MOTIONS_2 = new ArrayList<>();

    private boolean readyToJump = false;
    private int disableTicks = 0;

    public MotionA(@NotNull TRPlayer player) {
        super("MotionA", player);
        IGNORED_BLOCKS.add(Blocks.bed);
        IGNORED_BLOCKS.add(Blocks.slime_block);

        JUMP_MOTIONS.add(0.41159999516010254);
        JUMP_MOTIONS.add(-0.08506399504327788);
        JUMP_MOTIONS.add(-0.08336271676487925);
        JUMP_MOTIONS.add(-0.0816954640195993);
        JUMP_MOTIONS.add(-0.08006155629742463);
        JUMP_MOTIONS.add(-0.07846032669852913);
        JUMP_MOTIONS.add(-0.07689112166107052);
        JUMP_MOTIONS.add(-0.07535330069443089);
        JUMP_MOTIONS.add(-0.07384623611779237);
        JUMP_MOTIONS.add(-0.07236931280394177);
        JUMP_MOTIONS.add(-0.07092192792819801);

        JUMP_MOTIONS_1.add(0.5095999912261959);
        JUMP_MOTIONS_1.add(-0.08702399309539816);
        JUMP_MOTIONS_1.add(-0.08528351489334113);
        JUMP_MOTIONS_1.add(-0.08357784622212827);
        JUMP_MOTIONS_1.add(-0.0819062908918066);
        JUMP_MOTIONS_1.add(-0.08026816663620898);
        JUMP_MOTIONS_1.add(-0.07866280483447859);
        JUMP_MOTIONS_1.add(-0.07708955023816295);
        JUMP_MOTIONS_1.add(-0.07554776070376615);
        JUMP_MOTIONS_1.add(-0.07403680693065001);
        JUMP_MOTIONS_1.add(-0.07255607220417704);
        JUMP_MOTIONS_1.add(-0.07110495214399076);
        JUMP_MOTIONS_1.add(-0.0696828544573303);

        JUMP_MOTIONS_2.add(0.6076000164985658);
        JUMP_MOTIONS_2.add(-0.0889839917316434);
        JUMP_MOTIONS_2.add(-0.08720431359424546);
        JUMP_MOTIONS_2.add(-0.08546022898565087);
        JUMP_MOTIONS_2.add(-0.08375102603596235);
        JUMP_MOTIONS_2.add(-0.08207600711266715);
        JUMP_MOTIONS_2.add(-0.0804344885358894);
        JUMP_MOTIONS_2.add(-0.07882580029933772);
        JUMP_MOTIONS_2.add(-0.07724928579683382);
        JUMP_MOTIONS_2.add(-0.07570430155431032);
        JUMP_MOTIONS_2.add(-0.0741902169671691);
        JUMP_MOTIONS_2.add(-0.0727064140428918);
        JUMP_MOTIONS_2.add(-0.07125228714879878);
        JUMP_MOTIONS_2.add(-0.06982724276485225);
        JUMP_MOTIONS_2.add(-0.06843069924140427);
    }

    public static @Nullable List<Double> getPossibleMotions(@NotNull TRPlayer player) {
        try {
            switch (player.fabricPlayer.getActivePotionEffects().stream()
                    .filter(effect -> effect.getEffectName().toLowerCase().contains("jump"))
                    .findAny()
                    .orElseThrow(NoSuchElementException::new).getAmplifier()) {
                case 0: {
                    return JUMP_MOTIONS_1;
                }
                case 1: {
                    return JUMP_MOTIONS_2;
                }
                default: {
                    return null;
                }
            }
        } catch (NoSuchElementException e) {
            return JUMP_MOTIONS;
        }
    }

    @Override
    public void _onTick() {
        if (disableTicks > 0) {
            disableTicks--;
            readyToJump = false;
            return;
        }

        if (!check()) {
            readyToJump = false;
            return;
        }

        if (player.currentOnGround && !readyToJump) {
            readyToJump = true;
        }

        if (!readyToJump) return;

        try {
            int tick = player.offGroundTicks - 1;
            if (tick >= 0) {
                List<Double> possibleMotion = Objects.requireNonNull(getPossibleMotions(player));

                double should = possibleMotion.get(tick);
                double current = player.currentMotion.y();
                if (Math.abs(current - should) > 0.01) {
                    flag(String.format("Invalid jump motion at tick %s. should: %.2f  current: %.2f", tick, should, current));
                }
            }
        } catch (IndexOutOfBoundsException | NullPointerException ignored) {
            readyToJump = false;
        }
    }

    private boolean check() {
        if (PlayerMove.isInvalidMotion(player.currentMotion)) return false;

        return !player.fabricPlayer.isInWater()
                && !player.fabricPlayer.isOnLadder() && player.fabricPlayer.hurtTime <= 0
                && BlockUtils.isFullBlock(LevelUtils.getClientLevel().getBlockState(new BlockPos(player.fabricPlayer)))
                && IGNORED_BLOCKS.stream().noneMatch(block -> LevelUtils.getClientLevel().getBlockState(new BlockPos(player.fabricPlayer)).getBlock().equals(block));
    }

    @Override
    public int getAlertBuffer() {
        return AdvancedConfig.motionAAlertBuffer;
    }

    @Override
    public boolean isDisabled() {
        return true;
    }
}
