package keystrokesmod.module.impl.player.nofall;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.module.impl.player.NoFall;
import keystrokesmod.module.setting.impl.SubMode;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class OnGround2NoFall extends SubMode<NoFall> {
    public OnGround2NoFall(String name, @NotNull NoFall parent) {
        super(name, parent);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPreMotion(@NotNull PreMotionEvent event) {
        if (!parent.noAction() && mc.thePlayer.fallDistance > 3)
            event.setOnGround(true);
    }
}
