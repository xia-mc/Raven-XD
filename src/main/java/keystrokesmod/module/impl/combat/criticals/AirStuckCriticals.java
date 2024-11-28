package keystrokesmod.module.impl.combat.criticals;

import keystrokesmod.event.PostVelocityEvent;
import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.PreMoveEvent;
import keystrokesmod.event.PreVelocityEvent;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.combat.Criticals;
import keystrokesmod.module.impl.combat.KillAura;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.Utils;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class AirStuckCriticals extends SubMode<Criticals> {
    private final ButtonSetting onlyKillAura;
    private final ButtonSetting autoJump;
    private final ButtonSetting limitStuckTime;
    private final SliderSetting maxStuckTime;
    private final SliderSetting delay;
    private final SliderSetting pauseOnVelocity;
    private final ButtonSetting cancelC03;

    private int disableTicks = 0;
    private int stuckTicks = 0;
    private boolean active = false;
    private boolean lastActive = false;

    public AirStuckCriticals(String name, @NotNull Criticals parent) {
        super(name, parent);
        this.registerSetting(onlyKillAura = new ButtonSetting("Only killAura", true));
        this.registerSetting(autoJump = new ButtonSetting("Auto jump", false));
        this.registerSetting(limitStuckTime = new ButtonSetting("Limit stuck time", false));
        this.registerSetting(maxStuckTime = new SliderSetting("Max stuck time", 10, 2, 40, 1, "tick", limitStuckTime::isToggled));
        this.registerSetting(delay = new SliderSetting("Delay", 10, 0, 100, 2, "tick"));
        this.registerSetting(pauseOnVelocity = new SliderSetting("Pause on velocity", 1, 0, 5, 1, "tick"));
        this.registerSetting(cancelC03 = new ButtonSetting("Cancel C03", true));
    }

    @Override
    public void onEnable() {
        disableTicks = 0;
        stuckTicks = 0;
        active = lastActive = false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer.onGround) {
            if (!Utils.jumpDown() && autoJump.isToggled() && canActive(true))
                mc.thePlayer.jump();
        }

        lastActive = active;
        active = canActive(false);

        if (active) {
            stuckTicks++;
            if (cancelC03.isToggled())
                event.setCanceled(true);
        } else {
            stuckTicks = 0;
            if (lastActive) {
                disableTicks = (int) delay.getInput();
            }
        }

        if (disableTicks > 0)
            disableTicks--;
    }

    private boolean canActive(boolean jump) {
        if (!jump) {
            if (disableTicks > 0) return false;
            if (mc.thePlayer.fallDistance <= 0) return false;
            if (mc.thePlayer.onGround) return false;
        }
        if (limitStuckTime.isToggled()) {
            if (stuckTicks > maxStuckTime.getInput())
                return false;
        }

        if (onlyKillAura.isToggled()) {
            return KillAura.target != null && !ModuleManager.killAura.noAimToEntity();
        } else {
            return mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY;
        }
    }

    @SubscribeEvent
    public void onPreMove(PreMoveEvent event) {
        if (active)
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onPreVelocity(PreVelocityEvent event) {
        disableTicks += (int) pauseOnVelocity.getInput();
    }
}
