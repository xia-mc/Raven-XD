package keystrokesmod.module.impl.movement.noweb;

import keystrokesmod.event.BlockWebEvent;
import keystrokesmod.module.impl.movement.NoWeb;
import keystrokesmod.module.setting.impl.SubMode;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class IgnoreNoWeb extends SubMode<NoWeb> {
    public IgnoreNoWeb(String name, @NotNull NoWeb parent) {
        super(name, parent);
    }

    @SubscribeEvent
    public void onBlockWeb(@NotNull BlockWebEvent event) {
        event.setCanceled(true);
    }
}
