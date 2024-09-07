package keystrokesmod.module.impl.movement.fly;

import keystrokesmod.module.impl.movement.Fly;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import org.jetbrains.annotations.NotNull;

public class TestFly extends SubMode<Fly> {
    private final SliderSetting limitMotionY;

    public TestFly(String name, @NotNull Fly parent) {
        super(name, parent);
        this.registerSetting(limitMotionY = new SliderSetting("Limit motionY", -0.2, -0.4, 0, 0.01));
    }

    @Override
    public void onUpdate() throws Throwable {
        if (mc.thePlayer.onGround) {
            return;
        }

        mc.thePlayer.motionY = Math.max(mc.thePlayer.motionY, limitMotionY.getInput());
    }
}
