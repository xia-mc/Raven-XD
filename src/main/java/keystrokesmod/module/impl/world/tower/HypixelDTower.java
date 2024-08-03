package keystrokesmod.module.impl.world.tower;

import keystrokesmod.event.MoveEvent;
import keystrokesmod.module.impl.world.Tower;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Reflection;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class HypixelDTower extends SubMode<Tower> {
    private int towerTicks;

    public HypixelDTower(String name, @NotNull Tower parent) {
        super(name, parent);
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) throws IllegalAccessException {
        boolean towering = parent.canTower();

        if (mc.thePlayer.onGround) {
            this.towerTicks = 0;
        }

        Reflection.jumpTicks.set(mc.thePlayer, 0);
        if (MoveUtil.isMoving() && MoveUtil.speed() > 0.1 && !mc.thePlayer.isPotionActive(Potion.jump)) {
            if (towering) {
                if (mc.thePlayer.onGround) {
                    this.towerTicks = 0;
                } else if (this.towerTicks == 7) {
                    mc.thePlayer.setPosition(mc.thePlayer.posX, Math.floor(mc.thePlayer.posY), mc.thePlayer.posZ);
                }

                ++this.towerTicks;
            }
        }
    }
}
