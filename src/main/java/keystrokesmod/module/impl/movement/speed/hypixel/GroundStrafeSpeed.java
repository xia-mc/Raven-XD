package keystrokesmod.module.impl.movement.speed.hypixel;

import keystrokesmod.event.PrePlayerInputEvent;
import keystrokesmod.module.impl.movement.speed.HypixelSpeed;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class GroundStrafeSpeed extends SubMode<HypixelSpeed> {
    public GroundStrafeSpeed(String name, @NotNull HypixelSpeed parent) {
        super(name, parent);
    }

    @SubscribeEvent
    public void onPrePlayerInput(PrePlayerInputEvent event) {
        if (parent.parent.noAction()) return;

        if (!Utils.jumpDown() && Utils.isMoving() && mc.currentScreen == null && mc.thePlayer.onGround) {
            MoveUtil.strafe(MoveUtil.getAllowedHorizontalDistance());
            mc.thePlayer.jump();
        }
    }
}
