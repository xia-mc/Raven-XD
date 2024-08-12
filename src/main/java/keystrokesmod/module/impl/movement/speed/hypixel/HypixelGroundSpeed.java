package keystrokesmod.module.impl.movement.speed.hypixel;

import keystrokesmod.event.PrePlayerInputEvent;
import keystrokesmod.module.impl.movement.speed.HypixelSpeed;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class HypixelGroundSpeed extends SubMode<HypixelSpeed> {
    private final ButtonSetting fast;

    private boolean timed = false;

    public HypixelGroundSpeed(String name, @NotNull HypixelSpeed parent) {
        super(name, parent);
        this.registerSetting(fast = new ButtonSetting("Fast", false));
    }

    @SubscribeEvent
    public void onPrePlayerInput(PrePlayerInputEvent event) {
        if (!Utils.nullCheck() || parent.parent.noAction()) return;
        if (mc.thePlayer.onGround && MoveUtil.isMoving() && mc.currentScreen == null) {
            if (!fast.isToggled()) {
                Utils.getTimer().timerSpeed = (float) ((mc.thePlayer.ticksExisted % 2 == 0 ? 0.9095f : 1.11f) + (Math.random() - 0.5) / 1000.0);
                timed = true;
            }

            event.setSpeed(MoveUtil.getAllowedHorizontalDistance() - (fast.isToggled() ? 0 : Math.random() / 100.0));
        } else if (timed) {
            Utils.resetTimer();
            timed = false;
        }
    }

    @Override
    public void onDisable() {
        if (timed)
            Utils.resetTimer();
        timed = false;
    }
}
