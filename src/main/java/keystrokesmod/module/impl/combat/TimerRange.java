package keystrokesmod.module.impl.combat;

import akka.japi.Pair;
import keystrokesmod.event.MoveEvent;
import keystrokesmod.event.RotationEvent;
import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TimerRange extends Module {
    private final SliderSetting lagTicks;
    private final SliderSetting timerTicks;
    private final SliderSetting minRange;
    private final SliderSetting maxRange;
    private final SliderSetting delay;
    private final SliderSetting fov;
    private final ButtonSetting ignoreTeammates;
    private final ButtonSetting onlyOnGround;
    private final ButtonSetting clearMotion;
    private final ButtonSetting notWhileKB;
    private final ButtonSetting notWhileScaffold;

    private State state = State.NONE;
    private int hasLag = 0;
    private long lastTimerTime = -1;
    private final Queue<Packet<?>> delayedPackets = new ConcurrentLinkedQueue<>();
    private float yaw, pitch;
    private double motionX, motionY, motionZ;

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
        this.registerSetting(clearMotion = new ButtonSetting("Clear motion", false));
        this.registerSetting(notWhileKB = new ButtonSetting("Not while kb", false));
        this.registerSetting(notWhileScaffold = new ButtonSetting("Not while scaffold", true));
    }

    @Override
    public void onUpdate() {
        switch (state) {
            case NONE:
                if (shouldStart())
                    state = State.TIMER;
                break;
            case TIMER:
                for (int i = 0; i < timerTicks.getInput(); i++) {
                    mc.thePlayer.onUpdate();
                }
                yaw = RotationHandler.getRotationYaw();
                pitch = RotationHandler.getRotationPitch();
                motionX = mc.thePlayer.motionX;
                motionY = mc.thePlayer.motionY;
                motionZ = mc.thePlayer.motionZ;
                hasLag = 0;
                state = State.LAG;
                break;
            case LAG:
                if (hasLag >= lagTicks.getInput())
                    done();
                else
                    hasLag++;
                break;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onSendPacket(SendPacketEvent event) {
        switch (state) {
            case TIMER:
                synchronized (delayedPackets) {
                    delayedPackets.add(event.getPacket());
                    event.setCanceled(true);
                }
                break;
            case LAG:
                if (event.getPacket() instanceof C03PacketPlayer) {
                    event.setCanceled(true);
                } else {
                    synchronized (delayedPackets) {
                        delayedPackets.add(event.getPacket());
                        event.setCanceled(true);
                    }
                }
                break;
        }
    }

    @SubscribeEvent
    public void onMove(@NotNull MoveEvent event) {
        if (state == State.LAG) {
            event.setCanceled(true);
            mc.thePlayer.motionX = motionX;
            mc.thePlayer.motionY = motionY;
            mc.thePlayer.motionZ = motionZ;
        }
    }

    @SubscribeEvent
    public void onRotation(RotationEvent event) {
        if (state == State.LAG) {
            event.setYaw(yaw);
            event.setPitch(pitch);
            event.noSmoothBack();
        }
    }

    @Override
    public void onDisable() {
        done();
    }

    private void done() {
        state = State.NONE;
        hasLag = 0;
        lastTimerTime = System.currentTimeMillis();

        synchronized (delayedPackets) {
            for (Packet<?> p : delayedPackets) {
                PacketUtils.sendPacket(p);
            }
            delayedPackets.clear();
        }

        if (clearMotion.isToggled()) {
            mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0;
        } else {
            mc.thePlayer.motionX = motionX;
            mc.thePlayer.motionY = motionY;
            mc.thePlayer.motionZ = motionZ;
        }
    }

    private boolean shouldStart() {
        if (!Utils.nullCheck()) return false;
        if (onlyOnGround.isToggled() && !mc.thePlayer.onGround) return false;
        if (notWhileKB.isToggled() && mc.thePlayer.hurtTime > 0) return false;
        if (notWhileScaffold.isToggled() && ModuleManager.scaffold.isEnabled()) return false;
        if (!Utils.isMoving()) return false;
        if (fov.getInput() == 0) return false;
        if (System.currentTimeMillis() - lastTimerTime < delay.getInput()) return false;

        EntityPlayer target = mc.theWorld.playerEntities.parallelStream()
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

    enum State {
        NONE,
        TIMER,
        LAG
    }
}
