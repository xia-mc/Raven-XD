package keystrokesmod.module.impl.world.tower;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.module.impl.world.Tower;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class VulcanTower extends SubMode<Tower> {
    private final ButtonSetting notWhileMoving;

    public VulcanTower(String name, @NotNull Tower parent) {
        super(name, parent);
        this.registerSetting(notWhileMoving = new ButtonSetting("Not while moving", true));
    }

    @Override
    public void onUpdate() {
        if (canTower()) {
            mc.thePlayer.motionY = mc.thePlayer.ticksExisted % 2 == 0 ? 0.7 : MoveUtil.isMoving() ? 0.42 : 0.6;
        }
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (canTower() && mc.thePlayer.ticksExisted % 2 == 0 && !MoveUtil.isMoving()) {
            event.setPosX(event.getPosX() + 0.1);
            event.setPosZ(event.getPosZ() + 0.1);
        }
    }

    private boolean canTower() {
        return parent.canTower() && !(notWhileMoving.isToggled() && MoveUtil.isMoving());
    }
}
