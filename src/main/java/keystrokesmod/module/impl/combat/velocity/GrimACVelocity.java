package keystrokesmod.module.impl.combat.velocity;

import keystrokesmod.event.PostVelocityEvent;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.combat.KillAura;
import keystrokesmod.module.impl.combat.Velocity;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class GrimACVelocity extends SubMode<Velocity> {
    private final SliderSetting reduceCountEveryTime;
    private final SliderSetting reduceTimes;
    private final ButtonSetting onlyWhileMoving;
    private final ButtonSetting debug;

    private int unReduceTimes = 0;

    public GrimACVelocity(String name, @NotNull Velocity parent) {
        super(name, parent);
        this.registerSetting(new DescriptionSetting("Only work on 1.9+"));
        this.registerSetting(reduceCountEveryTime = new SliderSetting("Reduce count every time", 4, 1, 10, 1));
        this.registerSetting(reduceTimes = new SliderSetting("Reduce times", 1, 1, 5, 1));
        this.registerSetting(onlyWhileMoving = new ButtonSetting("Only while moving", false));
        this.registerSetting(debug = new ButtonSetting("Debug", false));
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        if (unReduceTimes > 0 && mc.thePlayer.hurtTime > 0 && !(onlyWhileMoving.isToggled() && !MoveUtil.isMoving()) && !ModuleManager.killAura.noAimToEntity()) {
            for (int i = 0; i < (int) reduceCountEveryTime.getInput(); i++) {
                Utils.attackEntity(KillAura.target, debug.isToggled());
            }
            if (debug.isToggled())
                Utils.sendMessage(String.format("%d Reduced %.3f %.3f", (int) reduceTimes.getInput() - unReduceTimes,  mc.thePlayer.motionX, mc.thePlayer.motionZ));
            unReduceTimes--;
        } else {
            unReduceTimes = 0;
        }
    }

    @SubscribeEvent
    public void onPostVelocity(PostVelocityEvent event) {
        unReduceTimes = (int) reduceTimes.getInput();
    }
}
