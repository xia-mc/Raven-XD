package keystrokesmod.module.impl.world.scaffold.rotation;

import keystrokesmod.event.RotationEvent;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.impl.world.scaffold.IScaffoldRotation;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.aim.RotationData;
import org.jetbrains.annotations.NotNull;

public class StrictRotation extends IScaffoldRotation {
    public StrictRotation(String name, @NotNull Scaffold parent) {
        super(name, parent);
    }

    @Override
    public @NotNull RotationData onRotation(float placeYaw, float placePitch, boolean forceStrict, @NotNull RotationEvent event) {
        if (!forceStrict && MoveUtil.isMoving()) {
            return new RotationData(parent.getYaw(), 85);
        }
        return new RotationData(placeYaw, placePitch);
    }
}
