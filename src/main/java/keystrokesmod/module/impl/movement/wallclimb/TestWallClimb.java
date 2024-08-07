package keystrokesmod.module.impl.movement.wallclimb;

import keystrokesmod.event.BlockAABBEvent;
import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.PushOutOfBlockEvent;
import keystrokesmod.mixins.impl.client.KeyBindingAccessor;
import keystrokesmod.module.impl.movement.WallClimb;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.MoveUtil;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class TestWallClimb extends SubMode<WallClimb> {
    public TestWallClimb(String name, @NotNull WallClimb parent) {
        super(name, parent);
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer.isCollidedHorizontally && !BlockUtils.insideBlock()) {
            double yaw = MoveUtil.direction();
            mc.thePlayer.setPosition(
                    mc.thePlayer.posX + -MathHelper.sin((float) yaw) * 0.05,
                    mc.thePlayer.posY,
                    mc.thePlayer.posZ + MathHelper.cos((float) yaw) * 0.05
            );
            MoveUtil.stop();
            ((KeyBindingAccessor) mc.gameSettings.keyBindForward).setPressed(false);
        }
    }

    @SubscribeEvent
    public void onPushOutOfBlock(@NotNull PushOutOfBlockEvent event) {
        if (BlockUtils.insideBlock())
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onAABB(BlockAABBEvent event) {
        if (BlockUtils.insideBlock()) {
            BlockPos playerPos = new BlockPos(mc.thePlayer);
            BlockPos blockPos = event.getBlockPos();
            if (blockPos.getY() > playerPos.getY())
                event.setBoundingBox(null);
        }
    }
}
