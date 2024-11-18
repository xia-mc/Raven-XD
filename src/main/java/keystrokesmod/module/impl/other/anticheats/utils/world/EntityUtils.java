package keystrokesmod.module.impl.other.anticheats.utils.world;

import keystrokesmod.module.impl.other.anticheats.TRPlayer;
import keystrokesmod.script.classes.ItemStack;
import keystrokesmod.script.classes.Vec3;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class EntityUtils {
    public static boolean isOnPlaceBlock(@NotNull TRPlayer player) {
        if (player.currentMainHead == ItemStack.EMPTY || player.lastMainHead == ItemStack.EMPTY)
            return false;  // not as same as 1.20.1

        if (!(player.lastMainHead.isBlock)) return false;
        //            BlockHitResult hitResult1 = RayCastUtils.blockRayCast(player.fabricPlayer, LevelUtils.getClientLevel(), 4.5);
        //            if (hitResult1.getType() != HitResult.Type.BLOCK) continue;
        // TODO IDK how to re-write raycast in 1.8
        //            return Optional.of(new Pair<>(hands.getC(), hitResult1.getBlockPos()));
        return player.currentMainHead.is(player.lastMainHead) && player.currentMainHead.getCount() == player.lastMainHead.getCount() - 1;
    }

    @Contract("_ -> new")
    public static @NotNull Vec3 getEyePosition(@NotNull Entity entity) {
        return new Vec3(entity.posX, entity.posY + (double) entity.getEyeHeight(), entity.posZ);
    }
}
