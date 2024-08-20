package keystrokesmod.module.impl.movement.speed.hypixel;

import keystrokesmod.event.PrePlayerInputEvent;
import keystrokesmod.module.impl.exploit.disabler.hypixel.HypixelMotionDisabler;
import keystrokesmod.module.impl.movement.speed.HypixelSpeed;
import keystrokesmod.module.setting.impl.*;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Vulcan speed lol
 */
public class HypixelLowHopSpeed extends SubMode<HypixelSpeed> {
    private final ButtonSetting fast;
    private final SliderSetting tick;

    public HypixelLowHopSpeed(String name, @NotNull HypixelSpeed parent) {
        super(name, parent);
        this.registerSetting(new DescriptionSetting("Motion disabler only."));
        this.registerSetting(fast = new ButtonSetting("Fast", false));
        this.registerSetting(tick = new SliderSetting("Tick", 5, 1, 11, 1));
    }

    @SubscribeEvent
    public void onPrePlayerInput(PrePlayerInputEvent event) {
        if (!MoveUtil.isMoving() || !HypixelMotionDisabler.isDisabled() || parent.parent.noAction()) return;
        if (parent.parent.offGroundTicks == 0) {
            if (!Utils.jumpDown()) {
                if (fast.isToggled()) {
                    mc.thePlayer.jump();
                    if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                        MoveUtil.strafe(0.6);
                    } else {
                        MoveUtil.strafe(0.485);
                    }
                } else {
                    MoveUtil.strafe(MoveUtil.getAllowedHorizontalDistance() - Math.random() / 100f);
                    mc.thePlayer.jump();
                }
            }
        } else if (parent.parent.offGroundTicks == (int) tick.getInput()) {
            mc.thePlayer.motionY = MoveUtil.predictedMotion(mc.thePlayer.motionY, 2);
        }
    }

}
