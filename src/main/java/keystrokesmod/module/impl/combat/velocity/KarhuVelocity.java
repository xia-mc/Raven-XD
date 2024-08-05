package keystrokesmod.module.impl.combat.velocity;

import keystrokesmod.event.BlockAABBEvent;
import keystrokesmod.event.PostVelocityEvent;
import keystrokesmod.module.impl.combat.Velocity;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.Utils;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class KarhuVelocity extends SubMode<Velocity> {
    public static final AxisAlignedBB BOUNDING_BOX = AxisAlignedBB.fromBounds(0, 0, 0, 1, 0, 1);
    private final SliderSetting startHurtTime;
    private final SliderSetting stopHurtTime;

    private final Set<BlockPos> needToBoundingPos = new HashSet<>(2);

    public KarhuVelocity(String name, @NotNull Velocity parent) {
        super(name, parent);
        this.registerSetting(startHurtTime = new SliderSetting("Start hurt time", 10, 1, 10, 1));
        this.registerSetting(stopHurtTime = new SliderSetting("Stop hurt time", 0, 0, 9, 1));
    }

    @Override
    public void guiUpdate() {
        Utils.correctValue(stopHurtTime, startHurtTime);
    }

    @Override
    public void onEnable() {
        needToBoundingPos.clear();
    }

    @SubscribeEvent
    public void onPostVelocity(PostVelocityEvent event) {
        BlockPos pos = new BlockPos(mc.thePlayer);

        if (mc.thePlayer.motionX > 0) {
            needToBoundingPos.add(pos.offset(EnumFacing.EAST));
        } else if (mc.thePlayer.motionX < 0) {
            needToBoundingPos.add(pos.offset(EnumFacing.WEST));
        }

        if (mc.thePlayer.motionZ > 0) {
            needToBoundingPos.add(pos.offset(EnumFacing.SOUTH));
        } else if (mc.thePlayer.motionZ < 0) {
            needToBoundingPos.add(pos.offset(EnumFacing.NORTH));
        }
    }

    @SubscribeEvent
    public void onBlockAABB(@NotNull BlockAABBEvent event) {
        if (mc.thePlayer.hurtTime <= startHurtTime.getInput() && mc.thePlayer.hurtTime > stopHurtTime.getInput()) {
            BlockPos pos = event.getBlockPos();
            if (needToBoundingPos.contains(pos)) {
                event.setBoundingBox(BOUNDING_BOX.offset(pos.getX(), pos.getY(), pos.getZ()));
            }
        } else {
            needToBoundingPos.clear();
        }
    }
}
