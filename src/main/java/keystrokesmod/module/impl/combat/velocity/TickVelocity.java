package keystrokesmod.module.impl.combat.velocity;

import keystrokesmod.Raven;
import keystrokesmod.event.PostVelocityEvent;
import keystrokesmod.module.impl.combat.Velocity;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class TickVelocity extends SubMode<Velocity> {
    private final SliderSetting horizontal;
    private final SliderSetting vertical;
    private final SliderSetting chance;
    private final SliderSetting delay;
    private final ButtonSetting onlyFirstHit;
    private final SliderSetting resetTime;

    private long lastVelocityTime = 0;

    public TickVelocity(String name, @NotNull Velocity parent) {
        super(name, parent);
        this.registerSetting(horizontal = new SliderSetting("Horizontal", 0.0, -100.0, 100.0, 1.0));
        this.registerSetting(vertical = new SliderSetting("Vertical", 100.0, 0.0, 100.0, 1.0));
        this.registerSetting(chance = new SliderSetting("Chance", 100, 0, 100, 1, "%"));
        this.registerSetting(delay = new SliderSetting("Delay", 50, 10, 400, 10, "ms"));
        this.registerSetting(onlyFirstHit = new ButtonSetting("Only first hit", false));
        this.registerSetting(resetTime = new SliderSetting("Reset time", 5000, 500, 10000, 500, "ms", onlyFirstHit::isToggled));
    }

    @SubscribeEvent
    public void onPostVelocity(PostVelocityEvent event) {
        final long time = System.currentTimeMillis();

        if (onlyFirstHit.isToggled() && time - lastVelocityTime < resetTime.getInput())
            return;
        lastVelocityTime = time;

        if (chance.getInput() == 100 || Math.random() * 100 <= chance.getInput()) {
            Raven.getExecutor().schedule(() -> {
                if (mc.thePlayer.hurtTime > 0) {
                    mc.thePlayer.motionX *= horizontal.getInput() / 100;
                    mc.thePlayer.motionY *= vertical.getInput() / 100;
                    mc.thePlayer.motionZ *= horizontal.getInput() / 100;
                }
            }, (long) delay.getInput(), TimeUnit.MILLISECONDS);
        }
    }
}
