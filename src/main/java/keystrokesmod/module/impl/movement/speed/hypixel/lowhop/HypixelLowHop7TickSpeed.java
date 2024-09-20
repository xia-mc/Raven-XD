package keystrokesmod.module.impl.movement.speed.hypixel.lowhop;

import keystrokesmod.event.PrePlayerInputEvent;
import keystrokesmod.module.impl.movement.speed.hypixel.HypixelLowHopSpeed;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class HypixelLowHop7TickSpeed extends SubMode<HypixelLowHopSpeed> {
    public HypixelLowHop7TickSpeed(String name, @NotNull HypixelLowHopSpeed parent) {
        super(name, parent);
    }

    @SubscribeEvent
    public void onPrePlayerInput(PrePlayerInputEvent event) {
        if (!MoveUtil.isMoving() || parent.parent.parent.noAction() || mc.thePlayer.isPotionActive(Potion.jump)) return;

        switch (parent.parent.parent.offGroundTicks) {
            case 0:
                if (!Utils.jumpDown()) {
                    MoveUtil.strafe(MoveUtil.getAllowedHorizontalDistance());
                    mc.thePlayer.jump();
                }
                break;
            case 1:
                set(0.3380001194477087);
                break;
            case 2:
                set(0.25302801701974894);
                break;
            case 3:
                set(0.18916746036107118);
                break;
            case 4:
                set(-0.079601588676394617);
                break;
            case 5:
                set(-0.31289557200443413);
                break;
            case 6:
                set(-0.3862369221364473);
                break;
            case 7:
                set(-0.45299219251018796);
                break;
        }
    }

    private void set(double motionY) {
        if (parent.noLowHop()) return;
        mc.thePlayer.motionY = motionY;
    }
}
