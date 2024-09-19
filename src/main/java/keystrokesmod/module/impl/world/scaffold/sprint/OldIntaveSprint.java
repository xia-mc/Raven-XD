package keystrokesmod.module.impl.world.scaffold.sprint;

import keystrokesmod.event.JumpEvent;
import keystrokesmod.event.MoveInputEvent;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.impl.world.scaffold.IScaffoldSprint;
import keystrokesmod.utility.MoveUtil;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class OldIntaveSprint extends IScaffoldSprint {
    public OldIntaveSprint(String name, @NotNull Scaffold parent) {
        super(name, parent);
    }

    @SubscribeEvent
    public void onMoveInput(@NotNull MoveInputEvent event) {
        if (!mc.thePlayer.onGround) {
            event.setForward(mc.thePlayer.movementInput.moveForward);
            event.setStrafe(mc.thePlayer.movementInput.moveStrafe);
        }
    }

    @SubscribeEvent
    public void onJump(@NotNull JumpEvent event) {
        event.setCanceled(true);
        mc.thePlayer.motionY = MoveUtil.jumpMotion();
    }

    @Override
    public boolean isSprint() {
        return false;
    }
}
