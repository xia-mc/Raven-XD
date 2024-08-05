package keystrokesmod.module.impl.combat;

import akka.japi.Pair;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Comparator;

public class TimerRange extends Module {
    private final SliderSetting lagTicks;
    private final SliderSetting timerTicks;
    private final SliderSetting minRange;
    private final SliderSetting maxRange;
    private final SliderSetting delay;
    private final SliderSetting fov;
    private final ButtonSetting ignoreTeammates;
    private final ButtonSetting onlyOnGround;

    private int hasLag = 0;
    private long lastTimerTime = 0;
    private long lastLagTime = 0;
    public TimerRange() {
        super("TimerRange", category.combat, "Use timer help you to beat opponent.");
        this.registerSetting(lagTicks = new SliderSetting("Lag ticks", 2, 0, 10, 1));
        this.registerSetting(timerTicks = new SliderSetting("Timer ticks", 2, 0, 10, 1));
        this.registerSetting(minRange = new SliderSetting("Min range", 3.6, 0, 8, 0.1));
        this.registerSetting(maxRange = new SliderSetting("Max range", 5, 0, 8, 0.1));
        this.registerSetting(delay = new SliderSetting("Delay", 500, 0, 4000, 100, "ms"));
        this.registerSetting(fov = new SliderSetting("Fov", 180, 0, 360, 30));
        this.registerSetting(ignoreTeammates = new ButtonSetting("Ignore teammates", true));
        this.registerSetting(onlyOnGround = new ButtonSetting("Only onGround", false));
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent e) {
        if (!shouldStart()) {
            reset();
            return;
        }

        if (hasLag < lagTicks.getInput()) {
            if (System.currentTimeMillis() - lastLagTime >= 50) {
                hasLag++;
                lastLagTime = System.currentTimeMillis();
                Utils.getTimer().timerSpeed = 0.0F;
            }
            return;
        }

        Utils.resetTimer();
        for (int i = 0; i < timerTicks.getInput(); i++) {
            mc.thePlayer.onUpdate();
        }

        hasLag = 0;
        lastTimerTime = System.currentTimeMillis();
    }

    @Override
    public void onDisable() {
        reset();
    }

    private void reset() {
        lastTimerTime = 0;
        lastLagTime = 0;
        if (hasLag > 0)
            Utils.resetTimer();
        hasLag = 0;
    }

    private boolean shouldStart() {
        if (!Utils.nullCheck()) return false;
        if (onlyOnGround.isToggled() && !mc.thePlayer.onGround) return false;
        if (!Utils.isMoving()) return false;
        if (fov.getInput() == 0) return false;
        if (System.currentTimeMillis() - lastTimerTime < delay.getInput()) return false;

        EntityPlayer target = mc.theWorld.playerEntities.stream()
                .filter(p -> p != mc.thePlayer)
                .filter(p -> !ignoreTeammates.isToggled() || !Utils.isTeamMate(p))
                .filter(p -> !Utils.isFriended(p))
                .filter(p -> !AntiBot.isBot(p))
                .map(p -> new Pair<>(p, mc.thePlayer.getDistanceSqToEntity(p)))
                .min(Comparator.comparing(Pair::second))
                .map(Pair::first)
                .orElse(null);

        if (target == null) return false;

        if (fov.getInput() < 360 && !Utils.inFov((float) fov.getInput(), target)) return false;

        double distance = new Vec3(target).distanceTo(mc.thePlayer);
        return distance >= minRange.getInput() && distance <= maxRange.getInput();
    }

    @Override
    public String getInfo() {
        return String.valueOf((int) timerTicks.getInput());
    }
}
