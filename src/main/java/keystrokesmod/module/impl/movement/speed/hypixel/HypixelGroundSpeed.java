package keystrokesmod.module.impl.movement.speed.hypixel;

import keystrokesmod.event.PrePlayerInputEvent;
import keystrokesmod.module.impl.movement.speed.HypixelSpeed;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.movement.MoveCorrect;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class HypixelGroundSpeed extends SubMode<HypixelSpeed> {
    public HypixelGroundSpeed(String name, @NotNull HypixelSpeed parent) {
        super(name, parent);
    }

    @SubscribeEvent
    public void onPrePlayerInput(PrePlayerInputEvent event) {
        if (!Utils.nullCheck() || parent.parent.noAction()) return;
        if (mc.thePlayer.onGround && MoveUtil.isMoving() && mc.currentScreen == null) {
            event.setSpeed(MoveUtil.getAllowedHorizontalDistance());
        }
    }
}
