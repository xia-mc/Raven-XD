package keystrokesmod.module.impl.movement.fly;

import keystrokesmod.module.impl.movement.Fly;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.Utils;
import org.jetbrains.annotations.NotNull;

public class Vanilla1Fly extends SubMode<Fly> {
    private final SliderSetting horizontalSpeed;

    private boolean lastFlying;

    public Vanilla1Fly(String name, @NotNull Fly parent) {
        super(name, parent);
        this.registerSetting(horizontalSpeed = new SliderSetting("Horizontal speed", 2.0, 0.0, 9.0, 0.1));
    }

    @Override
    public void onEnable() {
        this.lastFlying = mc.thePlayer.capabilities.isFlying;
        mc.thePlayer.motionY = 0.0;
    }

    @Override
    public void onUpdate() {
        mc.thePlayer.capabilities.setFlySpeed((float) (0.05000000074505806 * horizontalSpeed.getInput()));
        mc.thePlayer.capabilities.isFlying = true;
    }

    @Override
    public void onDisable() {
        if (Utils.nullCheck()) {
            if (mc.thePlayer.capabilities.allowFlying) {
                mc.thePlayer.capabilities.isFlying = this.lastFlying;
            } else {
                mc.thePlayer.capabilities.isFlying = false;
            }
            mc.thePlayer.capabilities.setFlySpeed(0.05F);
        }
        lastFlying = false;
    }
}
