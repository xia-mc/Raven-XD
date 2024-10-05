package keystrokesmod.module.impl.combat.ragebot.rapidfire;

import keystrokesmod.module.impl.combat.RageBot;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;

public class TimerRapidFire extends LegitRapidFire {
    private final SliderSetting ticks;
    private final SliderSetting delay;

    private boolean fire;
    private long lastRapid = 0;

    public TimerRapidFire(String name, @NotNull RageBot parent) {
        super(name, parent);
        this.registerSetting(ticks = new SliderSetting("Ticks", 4, 1, 20, 1));
        this.registerSetting(delay = new SliderSetting("Delay", 2000, 500, 5000, 500));
    }

    @Override
    public void onFire() {
        fire = true;
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event) {
        if (!Utils.nullCheck() || !fire) return;

        final long time = System.currentTimeMillis();
        if (time - lastRapid > delay.getInput()) {
            lastRapid = time;
            for (int i = 0; i < (int) ticks.getInput(); i++) {
                mc.thePlayer.onUpdate();
            }
        }
        fire = false;
    }

    @Override
    public void onDisable() throws Throwable {
        fire = false;
    }
}
