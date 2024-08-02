package keystrokesmod.module.impl.fun;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.RotationEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.fun.antiaim.Backward;
import keystrokesmod.module.impl.fun.antiaim.Spin;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeValue;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class AntiAim extends Module {
    private final ModeValue mode;
    private final ButtonSetting cancelSprint;
    private final ButtonSetting moveFix;

    public AntiAim() {
        super("AntiAim", category.fun);
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new Spin("Spin", this))
                .add(new Backward("Backward", this))
        );
        this.registerSetting(moveFix = new ButtonSetting("Move fix", false));
        this.registerSetting(cancelSprint = new ButtonSetting("Cancel sprint", false));
    }

    @Override
    public void onEnable() {
        mode.enable();
    }

    @Override
    public void onDisable() {
        mode.disable();
    }

    @SubscribeEvent
    public void onRotation(@NotNull RotationEvent event) {
        event.setMoveFix(moveFix.isToggled() ? RotationHandler.MoveFix.SILENT : RotationHandler.MoveFix.NONE);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPreMotion(PreMotionEvent event) {
        if (cancelSprint.isToggled()) {
            event.setSprinting(false);
        }
    }
}
