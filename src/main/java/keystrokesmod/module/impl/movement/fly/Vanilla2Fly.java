package keystrokesmod.module.impl.movement.fly;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.module.impl.movement.Fly;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class Vanilla2Fly extends SubMode<Fly> {
    private final SliderSetting horizontalSpeed;
    private final SliderSetting verticalSpeed;
    private final ButtonSetting groundSpoof;

    public Vanilla2Fly(String name, @NotNull Fly parent) {
        super(name, parent);
        this.registerSetting(horizontalSpeed = new SliderSetting("Horizontal speed", 2.0, 0.0, 9.0, 0.1));
        this.registerSetting(verticalSpeed = new SliderSetting("Vertical speed", 2.0, 0.0, 9.0, 0.1));
        this.registerSetting(groundSpoof = new ButtonSetting("Ground spoof", false));
    }

    @Override
    public void onUpdate() {
        if (mc.currentScreen == null) {
            if (Utils.jumpDown()) {
                mc.thePlayer.motionY = 0.3 * verticalSpeed.getInput();
            }
            else if (Utils.jumpDown()) {
                mc.thePlayer.motionY = -0.3 * verticalSpeed.getInput();
            }
            else {
                mc.thePlayer.motionY = 0.0;
            }
        }
        else {
            mc.thePlayer.motionY = 0.0;
        }
        MoveUtil.strafe(0.85 * horizontalSpeed.getInput());
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (groundSpoof.isToggled()) {
            event.setOnGround(true);
        }
    }
}
