package keystrokesmod.module.impl.movement.noslow;

import keystrokesmod.module.impl.movement.NoSlow;
import keystrokesmod.module.setting.impl.SubMode;
import org.jetbrains.annotations.NotNull;

public abstract class INoSlow extends SubMode<NoSlow> {
    public INoSlow(String name, @NotNull NoSlow parent) {
        super(name, parent);
    }

    public abstract float getSlowdown();

    public float getStrafeSlowdown() {
        return getSlowdown();
    }
}
