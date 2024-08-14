package keystrokesmod.module.impl.combat.criticals;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.combat.Criticals;
import keystrokesmod.module.impl.combat.KillAura;
import keystrokesmod.module.impl.exploit.disabler.hypixel.HypixelMotionDisabler;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class HypixelCriticals extends SubMode<Criticals> {
    private final ButtonSetting onlyKillAura;
    private final ButtonSetting autoJump;

    private int offGroundTicks = 0;

    public HypixelCriticals(String name, @NotNull Criticals parent) {
        super(name, parent);
        this.registerSetting(onlyKillAura = new ButtonSetting("Only killAura", true));
        this.registerSetting(autoJump = new ButtonSetting("Auto jump", false));
    }

    @Override
    public void onEnable() {
        offGroundTicks = 0;
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer.onGround) {
            offGroundTicks = 0;
        } else {
            offGroundTicks++;
        }

        if (!HypixelMotionDisabler.isDisabled() || ModuleManager.speed.isEnabled()) return;
        if (onlyKillAura.isToggled() && KillAura.target == null) return;
        if (!onlyKillAura.isToggled() && !Utils.isTargetNearby(3.2)) return;

        switch (offGroundTicks) {
            case 0:
                if (!Utils.jumpDown() && autoJump.isToggled())
                    mc.thePlayer.jump();
                break;
            case 5:
                mc.thePlayer.motionY = MoveUtil.predictedMotion(mc.thePlayer.motionY, 2);
                break;
        }
    }
}
