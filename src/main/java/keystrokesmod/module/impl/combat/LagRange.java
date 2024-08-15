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

public class LagRange extends Module {
    private final SliderSetting lagTime;
    private final SliderSetting minRange;
    private final SliderSetting maxRange;
    private final SliderSetting delay;
    private final SliderSetting fov;
    private final ButtonSetting ignoreTeammates;
    private final ButtonSetting onlyOnGround;

    private long lastLagTime = 0;

    public LagRange() {
        super("LagRange", category.combat);
        this.registerSetting(lagTime = new SliderSetting("Lag time", 150, 0, 500, 10, "ms"));
        this.registerSetting(minRange = new SliderSetting("Min range", 3.6, 0, 8, 0.1));
        this.registerSetting(maxRange = new SliderSetting("Max range", 5, 0, 8, 0.1));
        this.registerSetting(delay = new SliderSetting("Delay", 2000, 500, 10000, 100, "ms"));
        this.registerSetting(fov = new SliderSetting("Fov", 180, 0, 360, 30));
        this.registerSetting(ignoreTeammates = new ButtonSetting("Ignore teammates", true));
        this.registerSetting(onlyOnGround = new ButtonSetting("Only onGround", false));
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent e) throws InterruptedException {
        if (!shouldStart()) {
            return;
        }

        Thread.sleep((int) lagTime.getInput());
        lastLagTime = System.currentTimeMillis();
    }

    private boolean shouldStart() {
        if (!Utils.nullCheck()) return false;
        if (onlyOnGround.isToggled() && !mc.thePlayer.onGround) return false;
        if (!Utils.isMoving()) return false;
        if (fov.getInput() == 0) return false;
        if (System.currentTimeMillis() - lastLagTime < delay.getInput()) return false;

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
        return (int) lagTime.getInput() + "ms";
    }
}
