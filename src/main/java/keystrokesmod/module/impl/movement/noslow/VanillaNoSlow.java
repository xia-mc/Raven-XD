package keystrokesmod.module.impl.movement.noslow;

import keystrokesmod.module.impl.movement.NoSlow;
import org.jetbrains.annotations.NotNull;

public class VanillaNoSlow extends INoSlow {
    public VanillaNoSlow(String name, @NotNull NoSlow parent) {
        super(name, parent);
    }

    @Override
    public float getSlowdown() {
        return 1;
    }
}
