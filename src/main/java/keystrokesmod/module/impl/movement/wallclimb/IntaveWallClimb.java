package keystrokesmod.module.impl.movement.wallclimb;

import keystrokesmod.event.MoveEvent;
import keystrokesmod.module.impl.movement.WallClimb;
import keystrokesmod.module.setting.impl.SubMode;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class IntaveWallClimb extends SubMode<WallClimb> {
    public IntaveWallClimb(String name, @NotNull WallClimb parent) {
        super(name, parent);
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if (!mc.thePlayer.isCollidedHorizontally || mc.thePlayer.isOnLadder() || mc.thePlayer.isInWater() || mc.thePlayer.isInLava() || mc.thePlayer.isUsingItem())
            return;

        event.setY(0.2);
        mc.thePlayer.motionY = 0;
    }
}
