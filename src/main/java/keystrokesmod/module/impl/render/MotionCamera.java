package keystrokesmod.module.impl.render;

import keystrokesmod.event.EyeHeightEvent;
import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
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
    private final ButtonSetting camera;
    private final ButtonSetting scaffold;
    private final ButtonSetting notWhileTower;

    private double y = Double.NaN;
    private final Animation animation = new Animation(Easing.EASE_OUT_CUBIC, 1000);

    public MotionCamera() {
        super("MotionCamera", category.render);
        this.registerSetting(offset = new SliderSetting("Offset", 0, -1, 1, 0.01));
        this.registerSetting(maxOffset = new SliderSetting("Max offset", 1.5, 0, 5, 0.1));
        this.registerSetting(smooth = new ButtonSetting("Smooth", true));
        this.registerSetting(onlyThirdPerson = new ButtonSetting("Only third person", true));
        this.registerSetting(camera = new ButtonSetting("Camera", true));
        this.registerSetting(scaffold = new ButtonSetting("Scaffold", false));
        this.registerSetting(notWhileTower = new ButtonSetting("Not while tower", true, scaffold::isToggled));
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
        if (!canMotion()) {
            animation.setValue(y);
            return;
        }

        double curY = event.getY();
        double targetY = mc.thePlayer.posY + offset.getInput();

        if (camera.isToggled() && mc.gameSettings.thirdPersonView != 0) {
            if (Double.isNaN(y)) {
                animation.setValue(y);
            }
            animation.run(Utils.limit(targetY, curY - maxOffset.getInput(), curY + maxOffset.getInput()));

            if (smooth.isToggled()) {
                targetY = animation.getValue();

                event.setY(Utils.limit(targetY, curY - maxOffset.getInput(), curY + maxOffset.getInput()));
            } else {
                animation.setValue(y);

                event.setY(Utils.limit(y + offset.getInput(), curY - maxOffset.getInput(), curY + maxOffset.getInput()));
            }
        }
    }

    private boolean canMotion() {
        if (scaffold.isToggled() && ModuleManager.scaffold.isEnabled())
            return !notWhileTower.isToggled() || !ModuleManager.tower.canTower();
        if (onlyThirdPerson.isToggled() && mc.gameSettings.thirdPersonView == 0)
            return false;

        return true;
    }
}
