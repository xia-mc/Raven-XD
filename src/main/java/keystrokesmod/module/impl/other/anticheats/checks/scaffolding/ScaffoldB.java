package keystrokesmod.module.impl.other.anticheats.checks.scaffolding;

import keystrokesmod.module.impl.other.Anticheat;
import keystrokesmod.module.impl.other.anticheats.Check;
import keystrokesmod.module.impl.other.anticheats.TRPlayer;
import keystrokesmod.module.impl.other.anticheats.config.AdvancedConfig;
import keystrokesmod.script.classes.ItemStack;
import keystrokesmod.utility.BlockUtils;
import net.minecraft.block.BlockAir;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.NotNull;

public class ScaffoldB extends Check {
    public ScaffoldB(@NotNull TRPlayer player) {
        super("ScaffoldB", player);
    }

    @Override
    public void _onTick() {
        if (player.currentSwing && player.currentRot.x >= 70.0f && player.currentMainHead != ItemStack.EMPTY
                && player.currentMainHead.isBlock && player.compatPlayerData.fastTick >= 20
                && player.fabricPlayer.ticksExisted - player.compatPlayerData.lastSneakTick >= 30
                && player.fabricPlayer.ticksExisted - player.compatPlayerData.aboveVoidTicks >= 20) {
            boolean overAir = true;
            BlockPos blockPos = player.fabricPlayer.getPosition().down(2);
            for (int i = 0; i < 4; ++i) {
                if (!(BlockUtils.getBlock(blockPos) instanceof BlockAir)) {
                    overAir = false;
                    break;
                }
                blockPos = blockPos.down();
            }
            if (overAir) {
                flag();
            }
        }
    }

    @Override
    public int getAlertBuffer() {
        return AdvancedConfig.scaffoldBAlertBuffer;
    }

    @Override
    public boolean isDisabled() {
        return !Anticheat.getScaffoldingCheck().isToggled();
    }
}
