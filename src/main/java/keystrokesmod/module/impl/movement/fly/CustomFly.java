package keystrokesmod.module.impl.movement.fly;

import keystrokesmod.module.impl.movement.Fly;
import keystrokesmod.module.impl.movement.motionmodifier.SimpleMotionModifier;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeValue;
import keystrokesmod.module.setting.impl.SubMode;
import org.jetbrains.annotations.NotNull;

public class CustomFly extends SubMode<Fly> {
    private final ModeValue editMotion;
    private final ButtonSetting clearMotion;

    public CustomFly(String name, @NotNull Fly parent) {
        super(name, parent);
        this.registerSetting(editMotion = new ModeValue("Edit motion", this, () -> false)
                .add(new SimpleMotionModifier("MotionModifier", this))
        );
        this.registerSetting(clearMotion = new ButtonSetting("Clear motion", true));
    }

    @Override
    public void onDisable() throws Throwable {
        editMotion.disable();
        if (clearMotion.isToggled())
            mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0;
    }

    @Override
    public void onEnable() throws Throwable {
        editMotion.enable();
        ((SimpleMotionModifier) editMotion.getSelected()).update();
    }

    @Override
    public void onUpdate() throws Throwable {
        ((SimpleMotionModifier) editMotion.getSelected()).update();
    }
}
