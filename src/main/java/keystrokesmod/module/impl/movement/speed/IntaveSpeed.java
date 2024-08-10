package keystrokesmod.module.impl.movement.speed;

import keystrokesmod.module.impl.movement.Speed;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import org.jetbrains.annotations.NotNull;

/**
 * @see net.ccbluex.liquidbounce.features.module.modules.movement.speedmodes.other.MineblazeHop
 * <p>
 * credit: @thatonecoder & @larryngton / Intave14
 */
public class IntaveSpeed extends SubMode<Speed> {
    private final ButtonSetting airSpeed;

    public IntaveSpeed(String name, @NotNull Speed parent) {
        super(name, parent);
        this.registerSetting(airSpeed = new ButtonSetting("Air speed", true));
    }

    @Override
    public void onUpdate() {
        if (!MoveUtil.isMoving() || Utils.inLiquid() || mc.thePlayer.isOnLadder() || Utils.jumpDown()) return;

        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump();

            if (mc.thePlayer.isSprinting())
                MoveUtil.strafe(0.29);
        }

        if (mc.thePlayer.motionY > 0.003 && mc.thePlayer.isSprinting() && airSpeed.isToggled()) {
            mc.thePlayer.motionX *= 1.0015;
            mc.thePlayer.motionZ *= 1.0015;
        }
    }
}
