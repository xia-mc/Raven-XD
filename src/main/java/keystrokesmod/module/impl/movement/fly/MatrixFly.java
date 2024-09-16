package keystrokesmod.module.impl.movement.fly;

import keystrokesmod.event.BlockAABBEvent;
import keystrokesmod.event.MoveInputEvent;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.impl.movement.Fly;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class MatrixFly extends SubMode<Fly> {
    private int startY;

    public MatrixFly(String name, @NotNull Fly parent) {
        super(name, parent);
    }

    @Override
    public void onEnable() throws Throwable {
        startY = (int) mc.thePlayer.posY;
        Utils.sendMessage("Break the block under your feet to disable anti-cheat.");
    }

    @SubscribeEvent
    public void onMoveInput(MoveInputEvent event) {
        if (MoveUtil.isMoving())
            event.setJump(true);
    }

    @SubscribeEvent
    public void onBlockAABB(@NotNull BlockAABBEvent event) {
        if (BlockUtils.replaceable(event.getBlockPos())) {
            final double x = event.getBlockPos().getX(), y = event.getBlockPos().getY(), z = event.getBlockPos().getZ();

            if (y < startY) {
                event.setBoundingBox(AxisAlignedBB.fromBounds(-15, -1, -15, 15, 1, 15).offset(x, y, z));
            }
        }
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            Utils.sendMessage("Anti-cheat flagged.");
            parent.disable();
        }
    }
}
