package keystrokesmod.module.impl.world.tower;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.module.impl.world.Tower;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import static keystrokesmod.module.ModuleManager.tower;

public class JumpSprintTower extends SubMode<Tower> {
    private final SliderSetting speed;
    private final ButtonSetting noStrafe;
    private final SliderSetting offGroundSpeed;

    public JumpSprintTower(String name, @NotNull Tower parent) {
        super(name, parent);
        this.registerSetting(speed = new SliderSetting("Speed", 1, 0.5, 1, 0.01));
        this.registerSetting(offGroundSpeed = new SliderSetting("Off ground speed", 1, 0.0, 1.0, 0.01));
        this.registerSetting(noStrafe = new ButtonSetting("No strafe", false));
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) throws IllegalAccessException {
        if (tower.canTower()) {
            event.setSprinting(false);

            double moveSpeed = event.isOnGround() ? speed.getInput() : offGroundSpeed.getInput();
            if (noStrafe.isToggled()) {
                if (Math.abs(mc.thePlayer.motionX) >= Math.abs(mc.thePlayer.motionZ)) {
                    mc.thePlayer.motionX *= moveSpeed;
                    mc.thePlayer.motionZ = 0;
                } else {
                    mc.thePlayer.motionZ *= moveSpeed;
                    mc.thePlayer.motionX = 0;
                }
            } else {
                mc.thePlayer.motionX *= moveSpeed;
                mc.thePlayer.motionZ *= moveSpeed;
            }
        }
    }
}
