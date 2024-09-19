package keystrokesmod.module.impl.world.scaffold.sprint;

import keystrokesmod.event.MoveInputEvent;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.impl.world.scaffold.IScaffoldSprint;
import keystrokesmod.module.setting.impl.SliderSetting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class SneakSprint extends IScaffoldSprint {
    private final SliderSetting slowDown;

    public SneakSprint(String name, @NotNull Scaffold parent) {
        super(name, parent);
        this.registerSetting(slowDown = new SliderSetting("SlowDown", 1, 0.2, 1, 0.01));
    }

    @SubscribeEvent
    public void onMoveInput(@NotNull MoveInputEvent event) {
        event.setSneak(true);
        event.setSneakSlowDownMultiplier(slowDown.getInput());
    }

    @Override
    public boolean isSprint() {
        return mc.thePlayer.isSneaking();
    }
}
