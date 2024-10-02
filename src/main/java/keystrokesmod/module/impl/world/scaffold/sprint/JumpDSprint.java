package keystrokesmod.module.impl.world.scaffold.sprint;

import keystrokesmod.event.ScaffoldPlaceEvent;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.aim.RotationData;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

/**
 * credit by strangers 😭
 */
public class JumpDSprint extends JumpSprint {
    private final SliderSetting delayTicks;

    private int delay = 0;
    public JumpDSprint(String name, @NotNull Scaffold parent) {
        super(name, parent);
        this.registerSetting(delayTicks = new SliderSetting("Delay", 1, 1, 3, 1, "tick"));
    }

    @Override
    public RotationData onFinalRotation(RotationData data) {
        if (mc.thePlayer.onGround && MoveUtil.isMoving() && parent.placeBlock != null && !ModuleManager.tower.canTower()) {
            delay = (int) delayTicks.getInput();
            return new RotationData(mc.thePlayer.rotationYaw, (float) (0 + parent.getRandom()));
        }
        return super.onFinalRotation(data);
    }

    @SubscribeEvent
    public void onPlace(ScaffoldPlaceEvent event) {
        if (delay > 0) {
            event.setCanceled(true);
            delay--;
        }
    }
}
