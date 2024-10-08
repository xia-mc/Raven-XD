package keystrokesmod.module.impl.world.scaffold.sprint;

import keystrokesmod.event.ScaffoldPlaceEvent;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
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
        if (mc.thePlayer.onGround && MoveUtil.isMoving() && parent.placeBlock != null && !ModuleManager.tower.canTower() && !Utils.jumpDown()) {
            delay = (int) delayTicks.getInput();
        }
        if (delay > 0) {
            return new RotationData((float) (data.getYaw() - 180 - parent.getRandom()), (float) Utils.limit(data.getPitch() + parent.getRandom(), -90, 90));
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
