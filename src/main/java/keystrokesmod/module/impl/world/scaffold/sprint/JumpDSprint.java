package keystrokesmod.module.impl.world.scaffold.sprint;

import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.utility.aim.RotationData;
import org.jetbrains.annotations.NotNull;

/**
 * credit by strangers 😭
 */
public class JumpDSprint extends JumpSprint {
    public JumpDSprint(String name, @NotNull Scaffold parent) {
        super(name, parent);
    }

    @Override
    public RotationData onFinalRotation(RotationData data) {
        if (mc.thePlayer.onGround) {
            parent.delay = true;
            return new RotationData(mc.thePlayer.rotationYaw, (float) (0 + parent.getRandom()));
        }
        return super.onFinalRotation(data);
    }
}
