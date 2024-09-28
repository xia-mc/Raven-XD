package keystrokesmod.module.impl.player.timer;

import keystrokesmod.module.impl.player.Timer;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;

public class BalanceTimer extends SubMode<Timer> {
    private final SliderSetting speed;
    private final SliderSetting slowTimer;
    private final SliderSetting maxBalance;
    private final SliderSetting costMultiplier;
    private final ButtonSetting autoSlow;
    private final ButtonSetting autoDisable;

    private long balance = 0;
    private long startTime = -1;
    private BalanceState balanceState = BalanceState.NONE;

    public BalanceTimer(String name, @NotNull Timer parent) {
        super(name, parent);
        this.registerSetting(speed = new SliderSetting("Speed", 2, 1, 10.0, 0.01));
        this.registerSetting(slowTimer = new SliderSetting("Slow timer", 0, 0, 1, 0.01, "x"));
        this.registerSetting(maxBalance = new SliderSetting("Max balance", 1000, 0, 3000, 10, "ms"));
        this.registerSetting(costMultiplier = new SliderSetting("Cost multiplier", 1, 0.5, 5, 0.05, "x"));
        this.registerSetting(autoSlow = new ButtonSetting("Auto slow", false));
        this.registerSetting(autoDisable = new ButtonSetting("Auto disable", true));
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event) {
        final long curTime = System.currentTimeMillis();

        if (!parent.canTimer()) {
            reset();
            return;
        }

        switch (balanceState) {
            case NONE:
                startTime = curTime;
                if (autoSlow.isToggled() && MoveUtil.isRealMoving()) break;
                Utils.getTimer().timerSpeed = (float) slowTimer.getInput();
                balanceState = BalanceState.SLOW;
                break;
            case SLOW:
                if (autoSlow.isToggled() && MoveUtil.isRealMoving()) {
                    if (balance > 0) {
                        balanceState = BalanceState.TIMER;
                    } else {
                        balanceState = BalanceState.NONE;
                    }
                    break;
                }
                balance += (long) ((curTime - startTime) * (1 - slowTimer.getInput()));
                if (balance >= maxBalance.getInput()) {
                    balance = (long) maxBalance.getInput();
                    balanceState = BalanceState.TIMER;
                    startTime = curTime;
                } else {
                    startTime = curTime;
                    Utils.getTimer().timerSpeed = (float) slowTimer.getInput();
                }
                break;
            case TIMER:
                balance -= (long) ((curTime - startTime) * speed.getInput() * costMultiplier.getInput());
                if (balance <= 0) {
                    reset();
                    if (autoDisable.isToggled())
                        parent.disable();
                    break;
                }
                startTime = curTime;
                Utils.getTimer().timerSpeed = (float) speed.getInput();
                break;
        }
    }

    private void reset() {
        Utils.resetTimer();
        balance = 0;
        balanceState = BalanceState.NONE;
    }

    enum BalanceState {
        NONE,
        SLOW,
        TIMER
    }
}
