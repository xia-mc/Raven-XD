package keystrokesmod.module.impl.other.anticheats.checks.movement;

import keystrokesmod.module.impl.other.Anticheat;
import keystrokesmod.module.impl.other.anticheats.Check;
import keystrokesmod.module.impl.other.anticheats.TRPlayer;
import keystrokesmod.module.impl.other.anticheats.config.AdvancedConfig;
import keystrokesmod.module.impl.other.anticheats.utils.world.LevelUtils;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class GroundSpoofA extends Check {
    public GroundSpoofA(@NotNull TRPlayer player) {
        super("GroundSpoofA", player);
    }

    public static boolean check(@NotNull WorldClient level, @NotNull BlockPos groundPos) {
        if (!level.getBlockState(groundPos).getBlock().isAir(level, groundPos) || !level.getBlockState(groundPos.down()).getBlock().isAir(level, groundPos.down()))
            return false;

        short count = 0;
        final List<BlockPos> blocks = new ArrayList<>();
        blocks.add(groundPos.east());
        blocks.add(groundPos.east().north());
        blocks.add(groundPos.west());
        blocks.add(groundPos.west().south());
        blocks.add(groundPos.north());
        blocks.add(groundPos.north().west());
        blocks.add(groundPos.south());
        blocks.add(groundPos.south().east());

        for (BlockPos blockPos : blocks) {
            if (level.getBlockState(blockPos).getBlock().isAir(level, blockPos)) {
                count++;
            }
        }

        return count >= 8;
    }

    @Override
    public void _onTick() {
        if (player.lastOnGround2 && player.lastOnGround && player.currentOnGround) {  // check if it's legit
            final BlockPos groundPos = new BlockPos(player.fabricPlayer).down();

            if (check(LevelUtils.getClientLevel(), groundPos)) {
                flag("spoof onGround=true");
                setback();
            }
        }
    }

    public void setback() {
        player.currentOnGround = false;
    }

    @Override
    public boolean isDisabled() {
        return !Anticheat.getMovementCheck().isToggled();
    }

    @Override
    public int getAlertBuffer() {
        return AdvancedConfig.groundSpoofAAlertBuffer;
    }
}
