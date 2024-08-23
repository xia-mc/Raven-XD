package keystrokesmod.module.impl.movement.fly;

import keystrokesmod.event.MoveEvent;
import keystrokesmod.event.PreVelocityEvent;
import keystrokesmod.module.impl.movement.Fly;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class MatrixTNTFly extends SubMode<Fly> {
    private final SliderSetting horizontalSpeed;
    private final SliderSetting verticalSpeed;
    private final SliderSetting boost;
    private final ButtonSetting autoDisable;

    private boolean fly = false;
    private int velocityTicks = -1;

    public MatrixTNTFly(String name, @NotNull Fly parent) {
        super(name, parent);
        this.registerSetting(horizontalSpeed = new SliderSetting("Horizontal speed", 2, 0, 4, 0.1));
        this.registerSetting(verticalSpeed = new SliderSetting("Vertical speed", 0, 0, 9, 0.1));
        this.registerSetting(boost = new SliderSetting("Boost", 1.2, 1, 1.5, 0.1));
        this.registerSetting(autoDisable = new ButtonSetting("Auto disable", true));
    }

    @Override
    public void onEnable() {
        fly = false;
        velocityTicks = -1;
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if (!fly) {
            event.setCanceled(true);
            mc.thePlayer.motionY = 0;
        }
    }

    @SubscribeEvent
    public void onPreVelocity(@NotNull PreVelocityEvent event) {
        event.setMotionX((int) (event.getMotionX() * horizontalSpeed.getInput()));
        event.setMotionY(0);
        event.setMotionZ((int) (event.getMotionZ() * horizontalSpeed.getInput()));
        velocityTicks = 0;
        fly = true;
    }

    @Override
    public void onUpdate() {
        if (velocityTicks >= 0)
            velocityTicks++;
        if (velocityTicks >= 30 && autoDisable.isToggled())
            parent.disable();

        if (velocityTicks != -1 && velocityTicks < 30) {
            mc.thePlayer.motionY = verticalSpeed.getInput() / 10 - Math.random() / 1000;
            mc.thePlayer.motionX *= boost.getInput() - Math.random() / 1000;
            mc.thePlayer.motionZ *= boost.getInput() - Math.random() / 1000;
        }
    }
}
