package keystrokesmod.module.impl.movement.speed.hypixel.lowhop;

import keystrokesmod.event.PrePlayerInputEvent;
import keystrokesmod.module.impl.movement.speed.hypixel.HypixelLowHopSpeed;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class HypixelLowHop9TickSpeed extends SubMode<HypixelLowHopSpeed> {
    public HypixelLowHop9TickSpeed(String name, @NotNull HypixelLowHopSpeed parent) {
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
                set(0.33310120140062277);
                break;
            case 2:
                set(0.24796918219826297);
                break;
            case 3:
                set(0.14960980209333172);
                break;
            case 4:
                set(0.05321760771444281);
                break;
            case 5:
                set(-0.02624674495067964);
                break;
            case 6:
                set(-0.3191218156544406);
                break;
            case 7:
                set(-0.3161693874618279);
                break;
            case 8:
                set(-0.3882460072689227);
                break;
            case 9:
                set(-0.4588810960546281);
                break;
        }
    }

    private void set(double motionY) {
        if (parent.noLowHop()) return;
        mc.thePlayer.motionY = motionY;
    }
}
