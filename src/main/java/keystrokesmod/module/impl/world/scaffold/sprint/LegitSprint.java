package keystrokesmod.module.impl.world.scaffold.sprint;

import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.impl.world.scaffold.IScaffoldSprint;
import net.minecraft.util.MathHelper;
import org.jetbrains.annotations.NotNull;

public class LegitSprint extends IScaffoldSprint {
    public LegitSprint(String name, @NotNull Scaffold parent) {
        super(name, parent);
    }

    @Override
    public boolean isSprint() {
        return Math.abs(MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw) - MathHelper.wrapAngleTo180_float(RotationHandler.getRotationYaw())) <= 90;
    }
}
