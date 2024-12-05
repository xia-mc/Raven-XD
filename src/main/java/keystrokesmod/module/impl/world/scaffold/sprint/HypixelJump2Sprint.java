package keystrokesmod.module.impl.world.scaffold.sprint;

import keystrokesmod.event.PrePlayerInputEvent;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.exploit.disabler.hypixel.HypixelMotionDisabler;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class HypixelJump2Sprint extends VanillaSprint {
    public HypixelJump2Sprint(String name, @NotNull Scaffold parent) {
        super(name, parent);
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
    }
}
