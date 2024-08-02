package keystrokesmod.module.impl.fun.antiaim;

import keystrokesmod.event.RotationEvent;
import keystrokesmod.module.impl.fun.AntiAim;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class Backward extends SubMode<AntiAim> {
    private final SliderSetting pitch;
    private final ButtonSetting random;
    private final SliderSetting randomValue;

    public Backward(String name, @NotNull AntiAim parent) {
        super(name, parent);
        this.registerSetting(pitch = new SliderSetting("Pitch", 90, -90, 90, 5));
        this.registerSetting(random = new ButtonSetting("Random", false));
        this.registerSetting(randomValue = new SliderSetting("Random", 8, 0, 20, 1, random::isToggled));
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRotation(@NotNull RotationEvent event) {
        float yaw = mc.thePlayer.rotationYaw + 180;

        if (random.isToggled()) {
            yaw += (float) ((Math.random() - 0.5) * randomValue.getInput());
        }

        event.setYaw(yaw);
        event.setPitch((float) pitch.getInput());
    }
}
