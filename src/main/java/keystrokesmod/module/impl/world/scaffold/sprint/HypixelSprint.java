package keystrokesmod.module.impl.world.scaffold.sprint;

import keystrokesmod.event.ScaffoldPlaceEvent;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.impl.world.scaffold.IScaffoldSprint;
import keystrokesmod.utility.MoveUtil;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class HypixelSprint extends IScaffoldSprint {
    public HypixelSprint(String name, @NotNull Scaffold parent) {
        super(name, parent);
    }

    @SubscribeEvent
    public void onScaffold(ScaffoldPlaceEvent event) {
        if (MoveUtil.isMoving() && !ModuleManager.tower.canTower()) {
            MoveUtil.stop();
        }
    }

    @Override
    public boolean isSprint() {
        return true;
    }
}
