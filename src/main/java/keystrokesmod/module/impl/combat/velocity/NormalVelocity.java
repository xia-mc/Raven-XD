package keystrokesmod.module.impl.combat.velocity;

import keystrokesmod.event.PreVelocityEvent;
import keystrokesmod.module.impl.combat.Velocity;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class NormalVelocity extends SubMode<Velocity> {
    private final SliderSetting horizontal;
    private final SliderSetting vertical;
    private final SliderSetting chance;
    private final ButtonSetting onlyFirstHit;
    private final SliderSetting resetTime;

    private long lastVelocityTime = 0;

    public NormalVelocity(String name, @NotNull Velocity parent) {
        super(name, parent);
        this.registerSetting(horizontal = new SliderSetting("Horizontal", 90.0, -100.0, 100.0, 1.0));
        this.registerSetting(vertical = new SliderSetting("Vertical", 100.0, 0.0, 100.0, 1.0));
        this.registerSetting(chance = new SliderSetting("Chance", 100, 0, 100, 1, "%"));
        this.registerSetting(onlyFirstHit = new ButtonSetting("Only first hit", false));
        this.registerSetting(resetTime = new SliderSetting("Reset time", 5000, 500, 10000, 500, "ms", onlyFirstHit::isToggled));
    }

    @SubscribeEvent
    public void onPreVelocity(PreVelocityEvent event) {
        final long time = System.currentTimeMillis();

        if (onlyFirstHit.isToggled() && time - lastVelocityTime < resetTime.getInput())
            return;
        lastVelocityTime = time;

        if (chance.getInput() != 100 && Math.random() * 100 > chance.getInput()) return;

        event.setMotionX((int) (event.getMotionX() * horizontal.getInput() / 100));
        event.setMotionY((int) (event.getMotionY() * vertical.getInput() / 100));
        event.setMotionZ((int) (event.getMotionZ() * horizontal.getInput() / 100));
    }
}
