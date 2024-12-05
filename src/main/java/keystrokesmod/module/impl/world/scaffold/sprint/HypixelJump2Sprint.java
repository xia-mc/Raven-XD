package keystrokesmod.module.impl.world.scaffold.sprint;

import keystrokesmod.event.PrePlayerInputEvent;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class HypixelJump2Sprint extends VanillaSprint {
    private final ButtonSetting lowHop;

    public HypixelJump2Sprint(String name, @NotNull Scaffold parent) {
        super(name, parent);
        this.registerSetting(lowHop = new ButtonSetting("Low hop", true));
    }

    @SubscribeEvent
    public void onPrePlayerInput(PrePlayerInputEvent event) {
        if (!MoveUtil.isMoving() || ModuleManager.tower.canTower()) return;
        if (parent.offGroundTicks == 0) {
            if (!Utils.jumpDown()) {
                MoveUtil.strafe(Math.min(MoveUtil.getAllowedHorizontalDistance(), MoveUtil.speed() * 2) - Math.random() / 100f);
                mc.thePlayer.jump();
            }
        }

        if (lowHop.isToggled() && MoveUtil.getJumpEffect() == 0) {
            switch (parent.offGroundTicks) {
                case 1:
                    mc.thePlayer.motionY = 0.39;
                    break;
                case 3:
                    mc.thePlayer.motionY -= 0.13;
                    break;
                case 4:
                    mc.thePlayer.motionY -= 0.1;
                    break;
            }
        }
    }
}
