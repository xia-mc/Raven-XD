package keystrokesmod.module.impl.world.scaffold.sprint;

import keystrokesmod.event.MoveInputEvent;
import keystrokesmod.event.ScaffoldPlaceEvent;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.impl.world.scaffold.IScaffoldSprint;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.MoveUtil;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class HypixelSprint extends IScaffoldSprint {
    private final SliderSetting placeSlowdown;
    private final SliderSetting sneakSlowdown;

    private boolean slow = false;

    public HypixelSprint(String name, @NotNull Scaffold parent) {
        super(name, parent);
        this.registerSetting(placeSlowdown = new SliderSetting("Place slowdown", 0.6, 0.5, 1, 0.01));
        this.registerSetting(sneakSlowdown = new SliderSetting("Sneak slowdown", 0.3, 0.3, 1, 0.01));
    }

    @SubscribeEvent
    public void onScaffold(ScaffoldPlaceEvent event) {
        if (shouldSlow()) {
            mc.thePlayer.motionX *= placeSlowdown.getInput();
            mc.thePlayer.motionZ *= placeSlowdown.getInput();
            slow = true;
        }
    }

    @SubscribeEvent
    public void onMoveInput(MoveInputEvent event) {
        if (slow) {
            event.setSneak(true);
            event.setSneakSlowDown(sneakSlowdown.getInput());
            slow = false;
        }
    }

    private static boolean shouldSlow() {
        return MoveUtil.isMoving() && !ModuleManager.tower.canTower() && !Scaffold.isDiagonal();
    }

    @Override
    public boolean isSprint() {
        return !Scaffold.isDiagonal() || ModuleManager.tower.canTower();
    }
}
