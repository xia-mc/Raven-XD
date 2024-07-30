package keystrokesmod.module.impl.movement.fly;

import keystrokesmod.event.RotationEvent;
import keystrokesmod.module.impl.movement.Fly;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class AirPlaceFly extends SubMode<Fly> {
    public AirPlaceFly(String name, @NotNull Fly parent) {
        super(name, parent);
    }

    @Override
    public void onUpdate() {
        SlotHandler.setCurrentSlot(Scaffold.getSlot());

        if (mc.thePlayer.onGround) {
            if (!Utils.jumpDown())
                mc.thePlayer.jump();
        } else if (mc.thePlayer.motionY < 0) {
            if (!Utils.jumpDown() && mc.thePlayer.motionY > -0.25) {
                return;
            }

            BlockPos pos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ).down();
            if (BlockUtils.replaceable(pos)) {
                mc.playerController.onPlayerRightClick(
                        mc.thePlayer, mc.theWorld, SlotHandler.getHeldItem(),
                        pos, EnumFacing.UP, new Vec3(mc.thePlayer.posX, pos.getY(), mc.thePlayer.posZ)
                );
                mc.thePlayer.swingItem();
            }
        }
    }

    @SubscribeEvent
    public void onRotation(@NotNull RotationEvent event) {
        event.setPitch(90);
    }
}
