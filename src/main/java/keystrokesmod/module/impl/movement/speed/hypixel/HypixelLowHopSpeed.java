package keystrokesmod.module.impl.movement.speed.hypixel;

import keystrokesmod.module.impl.exploit.disabler.hypixel.HypixelMotionDisabler;
import keystrokesmod.module.impl.movement.speed.HypixelSpeed;
import keystrokesmod.module.impl.movement.speed.hypixel.lowhop.HypixelLowHopPredictSpeed;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.ModeValue;
import keystrokesmod.module.setting.impl.SubMode;
import org.jetbrains.annotations.NotNull;

/**
 * Vulcan speed lol
 */
public class HypixelLowHopSpeed extends SubMode<HypixelSpeed> {
    private final ModeValue mode;
    private final ButtonSetting stopOnHurt;

    public HypixelLowHopSpeed(String name, @NotNull HypixelSpeed parent) {
        super(name, parent);
        this.registerSetting(new DescriptionSetting("Motion disabler only."));
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new HypixelLowHopPredictSpeed("Predict", this))
        );
        this.registerSetting(stopOnHurt = new ButtonSetting("Stop on hurt", true));
    }

    @Override
    public void onEnable() throws Throwable {
        mode.enable();
    }

    @Override
    public void onDisable() throws Throwable {
        mode.disable();
    }

    public boolean noLowHop() {
        if (!HypixelMotionDisabler.isDisabled()) return true;
        return stopOnHurt.isToggled() && mc.thePlayer.hurtTime > 0;
    }
}
