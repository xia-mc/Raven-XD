package keystrokesmod.module.impl.world.scaffold.sprint;

import keystrokesmod.event.ScaffoldPlaceEvent;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.impl.world.scaffold.IScaffoldSprint;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.RotationUtils;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.movement.MoveCorrect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Optional;

public class SideSprint extends IScaffoldSprint {
    private final MoveCorrect moveCorrect = new MoveCorrect(0.3, MoveCorrect.Mode.POSITION);
    private boolean xSide = false;

    private int bridged = 1;

    public SideSprint(String name, @NotNull Scaffold parent) {
        super(name, parent);
    }

    @Override
    public void onEnable() throws Throwable {
        bridged = 1;
        xSide = false;
    }

    @SubscribeEvent
    public void onScaffold(ScaffoldPlaceEvent event) {
        bridged++;
        Utils.sendMessage(String.valueOf(bridged));
    }

    @Override
    public boolean onPreSchedulePlace() {
        if (!Utils.jumpDown()) {
            EnumFacing side = getSide();
            BlockPos from = parent.placeBlock.getBlockPos();
            BlockPos pos = from.offset(side);

            xSide = from.getX() != pos.getX();
            if (xSide) {
                moveCorrect.moveX(true);
            } else {
                moveCorrect.moveZ(true);
            }

            if (bridged % 2 == 0 && parent.placeBlock != null) {
                Optional<Triple<BlockPos, EnumFacing, Vec3>> optionalPlaceSide = RotationUtils.getPlaceSide(pos, Collections.singleton(side));
                if (!optionalPlaceSide.isPresent()) return true;
                Triple<BlockPos, EnumFacing, Vec3> placeSide = optionalPlaceSide.get();

                parent.place(new MovingObjectPosition(MovingObjectPosition.MovingObjectType.BLOCK, placeSide.getRight().toVec3(), placeSide.getMiddle(), placeSide.getLeft()), false);
                bridged = 0;
            }
        }
        return true;
    }

    private static EnumFacing getSide() {
        switch (EnumFacing.fromAngle(RotationHandler.getRotationYaw())) {
            case WEST:
                return EnumFacing.NORTH;
            case NORTH:
                return EnumFacing.EAST;
            case EAST:
                return EnumFacing.SOUTH;
            default:
            case SOUTH:
                return EnumFacing.WEST;
        }
    }

    @Override
    public boolean isSprint() {
        return xSide ? moveCorrect.isDoneX() : moveCorrect.isDoneZ();
    }
}
