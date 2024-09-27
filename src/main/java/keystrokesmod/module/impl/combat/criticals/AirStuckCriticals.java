package keystrokesmod.module.impl.combat.criticals;

import keystrokesmod.event.PostVelocityEvent;
import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.PreMoveEvent;
import keystrokesmod.module.impl.combat.Criticals;
import keystrokesmod.module.impl.combat.KillAura;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.Utils;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class AirStuckCriticals extends SubMode<Criticals> {
    private final ButtonSetting onlyKillAura;
    private final ButtonSetting autoJump;
    private final SliderSetting pauseOnVelocity;

    private int disableTicks = 0;
    private boolean active = false;

    public AirStuckCriticals(String name, @NotNull Criticals parent) {
        super(name, parent);
        this.registerSetting(onlyKillAura = new ButtonSetting("Only killAura", true));
        this.registerSetting(autoJump = new ButtonSetting("Auto jump", false));
        this.registerSetting(pauseOnVelocity = new SliderSetting("Pause on velocity", 1, 0, 5, 1, "tick"));
    }

    @Override
    public void onEnable() {
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer.onGround) {
            if (!Utils.jumpDown() && autoJump.isToggled())
                mc.thePlayer.jump();
        }

        active = (!onlyKillAura.isToggled() || KillAura.target != null)
                && disableTicks <= 0
                && !(mc.thePlayer.fallDistance <= 0)
                && !mc.thePlayer.onGround
                && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY;

        if (active)
            event.setCanceled(true);

        if (disableTicks > 0)
            disableTicks--;
    }

    @SubscribeEvent
    public void onPreMove(PreMoveEvent event) {
        if (active)
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onPostVelocity(PostVelocityEvent event) {
        disableTicks += (int) pauseOnVelocity.getInput();
    }
}
