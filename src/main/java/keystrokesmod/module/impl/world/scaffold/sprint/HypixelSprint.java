package keystrokesmod.module.impl.world.scaffold.sprint;

import keystrokesmod.event.ScaffoldPlaceEvent;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.impl.world.scaffold.IScaffoldSprint;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.MoveUtil;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class HypixelSprint extends IScaffoldSprint {
    private final SliderSetting slowDown;

    public HypixelSprint(String name, @NotNull Scaffold parent) {
        super(name, parent);
        this.registerSetting(slowDown = new SliderSetting("SlowDown", 0.5, 0, 0.8, 0.1));
    }

    @SubscribeEvent
    public void onScaffold(ScaffoldPlaceEvent event) {
        if (MoveUtil.isMoving() && !ModuleManager.tower.canTower()) {
            mc.thePlayer.motionX *= slowDown.getInput();
            mc.thePlayer.motionZ *= slowDown.getInput();
        }
    }

    @Override
    public boolean isSprint() {
        return true;
    }
}
