package keystrokesmod.module.impl.world.tower;

import keystrokesmod.event.MoveEvent;
import keystrokesmod.module.impl.world.Tower;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Reflection;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class HypixelCTower extends SubMode<Tower> {
    private int towerTicks;

    public HypixelCTower(String name, @NotNull Tower parent) {
        super(name, parent);
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) throws IllegalAccessException {
        boolean towering = parent.canTower();
        if (MoveUtil.isMoving() && MoveUtil.speed() > 0.1 && !mc.thePlayer.isPotionActive(Potion.jump)) {
            if (mc.thePlayer.onGround) {
                if (towering) {
                    this.towerTicks = 0;
                    Reflection.jumpTicks.set(mc.thePlayer, 0);
                    if (event.getY() > 0.0) {
                        event.setY(mc.thePlayer.motionY = 0.4198499917984009);
                        MoveUtil.strafe(0.26);
                    }
                }
            } else if (this.towerTicks == 2) {
                event.setY(Math.floor(mc.thePlayer.posY + 1.0) - mc.thePlayer.posY);
            } else if (this.towerTicks == 3) {
                if (towering) {
                    event.setY(mc.thePlayer.motionY = 0.4198499917984009);
                    this.towerTicks = 0;
                }
            }

            ++this.towerTicks;
        }
    }
}
