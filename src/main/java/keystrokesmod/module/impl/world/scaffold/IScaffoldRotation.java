package keystrokesmod.module.impl.world.scaffold;

import keystrokesmod.event.RotationEvent;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.aim.RotationData;
import org.jetbrains.annotations.NotNull;

public abstract class IScaffoldRotation extends SubMode<Scaffold> {

    public IScaffoldRotation(String name, @NotNull Scaffold parent) {
        super(name, parent);
    }

    /**
     * The target rotation
     *
     * @param placeYaw   block place yaw
     * @param placePitch block place pitch
     * @param event      rotation event
     * @return the final rotation data
     */
    public abstract @NotNull RotationData onRotation(float placeYaw, float placePitch, boolean forceStrict, @NotNull RotationEvent event);
}
