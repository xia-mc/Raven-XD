package keystrokesmod.module.impl.player.timer;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.module.impl.combat.KillAura;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.impl.player.Timer;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class HypixelTimer extends SubMode<Timer> {
    private final SliderSetting speed;
    private final SliderSetting maxBalance;
    private final ButtonSetting notWhileKillAura;

    private double balanceTicks = 0;
    private boolean active = false;

    public HypixelTimer(String name, @NotNull Timer parent) {
        super(name, parent);
        this.registerSetting(speed = new SliderSetting("Speed", 2, 1.1, 5, 0.1));
        this.registerSetting(maxBalance = new SliderSetting("Max balance", 750, 500, 1500, 50, "ms"));
        this.registerSetting(notWhileKillAura = new ButtonSetting("Not while KillAura", true));
    }

    @Override
    public void onUpdate() throws Throwable {
        if (canTimer()) {
            active = true;
        } else {
            active = false;
            float speed = (float) this.speed.getInput();
            if (balanceTicks > speed - 1 && (!notWhileKillAura.isToggled() || KillAura.target == null)) {
                balanceTicks -= speed - 1;
                Utils.getTimer().timerSpeed = speed;
            } else {
                balanceTicks = 0;
                Utils.resetTimer();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPreMotion(PreMotionEvent event) {
        if (active) {
            event.setCanceled(true);
            balanceTicks = Math.min(balanceTicks + 1, maxBalance.getInput() / 50);
        }
    }

    @Override
    public void onEnable() throws Throwable {
        balanceTicks = 0;
        active = false;
        Utils.resetTimer();
    }

    private boolean canTimer() {
        if (!parent.canTimer())
            return false;
        if (MoveUtil.isMoving() || MoveUtil.isRealMoving())
            return false;
        if (!mc.thePlayer.onGround)
            return false;
        if (notWhileKillAura.isToggled() && KillAura.target != null)
            return false;
        return !RotationHandler.isSet();
    }
}
