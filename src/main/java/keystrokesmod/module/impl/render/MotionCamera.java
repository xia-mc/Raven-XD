package keystrokesmod.module.impl.render;

import keystrokesmod.event.EyeHeightEvent;
import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.render.Animation;
import keystrokesmod.utility.render.Easing;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class MotionCamera extends Module {
    private final SliderSetting offset;
    private final SliderSetting maxOffset;
    private final ButtonSetting smooth;
    private final ButtonSetting onlyThirdPerson;

    private double y = Double.NaN;
    private final Animation animation = new Animation(Easing.EASE_OUT_CIRC, 300);

    public MotionCamera() {
        super("MotionCamera", category.render);
        this.registerSetting(offset = new SliderSetting("Offset", 0, -2, 2, 0.1));
        this.registerSetting(maxOffset = new SliderSetting("Max offset", 1.5, 0, 5, 0.1));
        this.registerSetting(smooth = new ButtonSetting("Smooth", true));
        this.registerSetting(onlyThirdPerson = new ButtonSetting("Only third person", true));
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer.onGround) {
            if (Double.isNaN(y)) {
                y = mc.thePlayer.posY;
                animation.setValue(y);
            } else {
                y = mc.thePlayer.posY;
            }
        }
    }

    @Override
    public void onEnable() throws Throwable {
        y = Double.NaN;
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onEyeHeightEvent(@NotNull EyeHeightEvent event) {
        if (Double.isNaN(y)) return;
        if (onlyThirdPerson.isToggled() && mc.gameSettings.thirdPersonView != 1) {
            animation.setValue(y);
            return;
        }

        double curY = event.getY();
        double targetY = y + offset.getInput();
        animation.run(Utils.limit(targetY, curY - maxOffset.getInput() + offset.getInput(), curY + maxOffset.getInput() + offset.getInput()));
        if (smooth.isToggled())
            targetY = animation.getValue();
        event.setY(Utils.limit(targetY, curY - maxOffset.getInput() + offset.getInput(), curY + maxOffset.getInput() + offset.getInput()));
    }
}
