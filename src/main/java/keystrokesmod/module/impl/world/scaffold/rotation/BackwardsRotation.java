package keystrokesmod.module.impl.world.scaffold.rotation;

import keystrokesmod.event.RotationEvent;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.impl.world.scaffold.IScaffoldRotation;
import keystrokesmod.utility.aim.RotationData;
import org.jetbrains.annotations.NotNull;

public class BackwardsRotation extends IScaffoldRotation {
    public BackwardsRotation(String name, @NotNull Scaffold parent) {
        super(name, parent);
    }

    @Override
    public @NotNull RotationData onRotation(float placeYaw, float placePitch, boolean forceStrict, @NotNull RotationEvent event) {
        return new RotationData(parent.getYaw() + (Scaffold.isDiagonal() ? 0 : (float) parent.strafe.getInput()), 85);
    }
}
