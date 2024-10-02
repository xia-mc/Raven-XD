package keystrokesmod.module.impl.world.scaffold.rotation;

import keystrokesmod.event.RotationEvent;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.impl.world.scaffold.IScaffoldRotation;
import keystrokesmod.utility.aim.RotationData;
import net.minecraft.util.MovingObjectPosition;
import org.jetbrains.annotations.NotNull;

public class ConstantRotation extends IScaffoldRotation {
    public ConstantRotation(String name, @NotNull Scaffold parent) {
        super(name, parent);
    }

    @Override
    public @NotNull RotationData onRotation(float placeYaw, float placePitch, boolean forceStrict, @NotNull RotationEvent event) {
        final MovingObjectPosition rayCasted = parent.rayCasted;
        if (rayCasted == null)
            return new RotationData(placeYaw, placePitch);

        return new RotationData(parent.getYaw(), placePitch);
    }
}
