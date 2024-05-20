package keystrokesmod.module.impl.movement;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;

public class Speed extends Module {
    public static SliderSetting speed;
    private final ButtonSetting strafeOnly;

    public Speed() {
        super("Speed", category.movement, 0);
        this.registerSetting(speed = new SliderSetting("Speed", 1.2D, 1.0D, 1.5D, 0.01D));
        this.registerSetting(strafeOnly = new ButtonSetting("Strafe only", false));
    }

    public void onUpdate() {
        double csp = Utils.getHorizontalSpeed();
        if (csp != 0.0D) {
            if (mc.thePlayer.onGround && !mc.thePlayer.capabilities.isFlying) {
                if (!strafeOnly.isToggled() || mc.thePlayer.moveStrafing != 0.0F) {
                    if (mc.thePlayer.hurtTime != mc.thePlayer.maxHurtTime || mc.thePlayer.maxHurtTime <= 0) {
                        if (!Utils.jumpDown()) {
                            double val = speed.getInput() - (speed.getInput() - 1.0D) * 0.5D;
                            Utils.ss(csp * val, true);
                        }
                    }
                }
            }
        }
    }
}
