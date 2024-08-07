package keystrokesmod.module.impl.combat.criticals;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.module.impl.combat.Criticals;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class NoGroundCriticals extends SubMode<Criticals> {
    public NoGroundCriticals(String name, @NotNull Criticals parent) {
        super(name, parent);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPreMotion(PreMotionEvent event) {
        if (Utils.isTargetNearby()) {
            event.setOnGround(false);
        }
    }
}
