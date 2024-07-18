package keystrokesmod.module.impl.movement;

import keystrokesmod.clickgui.ClickGui;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Timer extends Module {
    private final ModeSetting mode;
    public static SliderSetting speed;
    private final SliderSetting slowTimer;
    private final SliderSetting maxBalance;
    private final SliderSetting costMultiplier;
    private final ButtonSetting autoDisable;
    public static ButtonSetting strafeOnly;
    private final ButtonSetting onlyOnGround;

    private long balance = 0;
    private long startTime = -1;
    private BalanceState balanceState = BalanceState.NONE;

    private int enableTicks;

    public Timer() {
        super("Timer", Module.category.movement, 0);
        this.registerSetting(mode = new ModeSetting("Mode", new String[]{"Normal", "Balance", "Hypixel", "None"}, 0));
        final ModeOnly mode1 = new ModeOnly(mode, 1);
        this.registerSetting(speed = new SliderSetting("Speed", 1.00, 0.01, 10.0, 0.05, new ModeOnly(mode, 0, 1)));
        this.registerSetting(slowTimer = new SliderSetting("Slow timer", 0, 0, 1, 0.01, "x", mode1));
        this.registerSetting(maxBalance = new SliderSetting("Max balance", 1000, 0, 3000, 10, "ms", mode1));
        this.registerSetting(costMultiplier = new SliderSetting("Cost multiplier", 1, 0.5, 5, 0.05, "x", mode1));
        this.registerSetting(autoDisable = new ButtonSetting("Auto disable", true, mode1));
        this.registerSetting(strafeOnly = new ButtonSetting("Strafe only", false));
        this.registerSetting(onlyOnGround = new ButtonSetting("Only onGround", false));
    }

    @Override
    public void onUpdate() {
        enableTicks++;
        if (enableTicks > 4)
            enableTicks = 0;

        if ((int) mode.getInput() == 2) {
            if (mc.thePlayer.onGround) {
                switch (enableTicks) {
                    case 0:
                        Utils.getTimer().timerSpeed = 0.3f;
                        MoveUtil.strafe();
                        break;
                    case 1:
                        Utils.getTimer().timerSpeed = 1.8f;
                        MoveUtil.strafe();
                        break;
                }
            } else {
                reset();
            }
        }
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event) {
        final long curTime = System.currentTimeMillis();

        if (mc.currentScreen instanceof ClickGui) {
            reset();
        } else {
            if (strafeOnly.isToggled() && mc.thePlayer.moveStrafing == 0.0F) {
                reset();
                return;
            }
            if (onlyOnGround.isToggled() && !mc.thePlayer.onGround) {
                reset();
                return;
            }

            switch ((int) mode.getInput()) {
                case 0:
                    Utils.getTimer().timerSpeed = (float) speed.getInput();
                    break;
                case 1:
                    switch (balanceState) {
                        case NONE:
                            startTime = curTime;
                            Utils.getTimer().timerSpeed = (float) slowTimer.getInput();
                            balanceState = BalanceState.SLOW;
                            break;
                        case SLOW:
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
                                    disable();
                                break;
                            }
                            startTime = curTime;
                            Utils.getTimer().timerSpeed = (float) speed.getInput();
                            break;
                    }
                    break;
            }
        }
    }

    private void reset() {
        Utils.resetTimer();
        balance = 0;
        balanceState = BalanceState.NONE;
        enableTicks = 0;
    }

    @Override
    public String getInfo() {
        return String.valueOf(Utils.getTimer().timerSpeed);
    }

    @Override
    public void onDisable() {
        reset();
    }

    enum BalanceState {
        NONE,
        SLOW,
        TIMER
    }
}
