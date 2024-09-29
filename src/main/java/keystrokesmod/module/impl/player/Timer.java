package keystrokesmod.module.impl.player;

import keystrokesmod.clickgui.ClickGui;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.player.timer.BalanceTimer;
import keystrokesmod.module.impl.player.timer.HypixelTimer;
import keystrokesmod.module.impl.player.timer.NormalTimer;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.ModeValue;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Timer extends Module {
    private final ModeValue mode;
    private final ButtonSetting strafeOnly;
    private final ButtonSetting onlyOnGround;
    private final ButtonSetting notWhileClickGUI;

    public Timer() {
        super("Timer", category.player, 0);
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new NormalTimer("Normal", this))
                .add(new BalanceTimer("Balance", this))
                .add(new HypixelTimer("Hypixel", this))
        );
        this.registerSetting(strafeOnly = new ButtonSetting("Strafe only", false));
        this.registerSetting(onlyOnGround = new ButtonSetting("Only onGround", false));
        this.registerSetting(notWhileClickGUI = new ButtonSetting("Not while click gui", true));
    }

    @Override
    public void onEnable() throws Throwable {
        mode.enable();
    }

    @Override
    public void onDisable() throws Throwable {
        mode.disable();
        Utils.resetTimer();
    }

    public boolean canTimer() {
        if (strafeOnly.isToggled() && mc.thePlayer.moveStrafing == 0)
            return false;
        if (notWhileClickGUI.isToggled() && mc.currentScreen instanceof ClickGui)
            return false;
        return !onlyOnGround.isToggled() || mc.thePlayer.onGround;
    }


    @Override
    public String getInfo() {
        return String.format("%.3f", Utils.getTimer().timerSpeed);
    }
}
