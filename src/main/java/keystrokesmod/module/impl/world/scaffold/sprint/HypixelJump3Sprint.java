package keystrokesmod.module.impl.world.scaffold.sprint;

import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.impl.world.scaffold.IScaffoldSchedule;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.aim.RotationData;
import org.jetbrains.annotations.NotNull;

public class HypixelJump3Sprint extends JumpSprint {
    private float pitch = (float) (85 + Utils.randomizeDouble(-5, 1));

    public HypixelJump3Sprint(String name, @NotNull Scaffold parent) {
        super(name, parent);
    }

    @Override
    public RotationData onFinalRotation(RotationData data) {
        if (((IScaffoldSchedule) parent.schedule.getSelected()).noRotation()) {
            return new RotationData(parent.getYaw() + 80, pitch);
        }
        pitch = (float) (85 + Utils.randomizeDouble(-5, 1));
        return data;
    }
}
