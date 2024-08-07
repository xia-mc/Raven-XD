package keystrokesmod.module.impl.movement.speed;

import keystrokesmod.event.PrePlayerInputEvent;
import keystrokesmod.module.impl.movement.Speed;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class IntaveSpeed extends SubMode<Speed> {
    private final ButtonSetting airSpeed;

    public IntaveSpeed(String name, @NotNull Speed parent) {
        super(name, parent);
        this.registerSetting(airSpeed = new ButtonSetting("Air speed", true));
    }

    @SubscribeEvent
    public void onPrePlayerInput(PrePlayerInputEvent event) {
        if (mc.thePlayer.onGround && !Utils.jumpDown()) {
            mc.thePlayer.motionY = 0.42;
            MoveUtil.moveFlying(0.29);
        }

        if (mc.thePlayer.motionY > 0.003 && airSpeed.isToggled()) {
            mc.thePlayer.motionX *= 1.0015;
            mc.thePlayer.motionZ *= 1.0015;
        }
    }
}
