package keystrokesmod.module.impl.combat.ragebot.nospread;

import keystrokesmod.event.PreTickEvent;
import keystrokesmod.module.impl.combat.RageBot;
import keystrokesmod.module.impl.combat.ragebot.IRageBotFeature;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class LegitNoSpread extends IRageBotFeature {
    private boolean shouldSwitch = false;
    private boolean fire;

    public LegitNoSpread(String name, @NotNull RageBot parent) {
        super(name, parent);
    }

    @Override
    public void onFire() {
        fire = true;
    }

    @SubscribeEvent
    public void onPreTick(PreTickEvent event) {
        if (shouldSwitch) {
            int curSlot = SlotHandler.getCurrentSlot();
            int nextSlot = Utils.randomizeInt(0, 8);
            if (nextSlot == curSlot)
                nextSlot = nextSlot % 8 + 1;
            SlotHandler.setCurrentSlot(nextSlot);
            shouldSwitch = false;
        } else if (fire) {
            shouldSwitch = true;
            fire = false;
        }
    }
}
