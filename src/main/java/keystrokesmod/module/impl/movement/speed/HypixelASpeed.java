package keystrokesmod.module.impl.movement.speed;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.module.impl.movement.Speed;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class HypixelASpeed extends SubMode<Speed> {
    private final ButtonSetting autoJump;

    public HypixelASpeed(String name, @NotNull Speed parent) {
        super(name, parent);
        this.registerSetting(autoJump = new ButtonSetting("Auto jump", true));
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (parent.noAction()) return;

        if (mc.thePlayer.onGround) {
            if (!Utils.jumpDown() && autoJump.isToggled())
                mc.thePlayer.jump();
            MoveUtil.strafe(MoveUtil.getAllowedHorizontalDistance());
        }
    }
}
