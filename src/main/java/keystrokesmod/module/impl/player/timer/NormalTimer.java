package keystrokesmod.module.impl.player.timer;

import keystrokesmod.module.impl.player.Timer;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.Utils;
import org.jetbrains.annotations.NotNull;

public class NormalTimer extends SubMode<Timer> {
    private final SliderSetting speed;

    public NormalTimer(String name, @NotNull Timer parent) {
        super(name, parent);
        this.registerSetting(speed = new SliderSetting("Speed", 1, 0, 5, 0.001));
    }

    @Override
    public void onUpdate() throws Throwable {
        if (parent.canTimer()) {
            Utils.getTimer().timerSpeed = (float) speed.getInput();
        } else {
            Utils.resetTimer();
        }
    }

    @Override
    public void onEnable() throws Throwable {
        Utils.resetTimer();
    }

    @Override
    public void onDisable() throws Throwable {
        Utils.resetTimer();
    }
}
