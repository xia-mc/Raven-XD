package keystrokesmod.module.impl.movement.step;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.module.impl.movement.Step;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class HypixelStep extends SubMode<Step> {
    private final SliderSetting delay = new SliderSetting("Delay", 0, 0, 5000, 250, "ms");

    private int offGroundTicks = -1;
    private boolean stepping = false;
    private long lastStep = -1;

    public HypixelStep(String name, Step parent) {
        super(name, parent);
        this.registerSetting(delay);
    }

    @Override
    public void onDisable() {
        offGroundTicks = -1;
        stepping = false;
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        final long time = System.currentTimeMillis();
        if (mc.thePlayer.onGround && mc.thePlayer.isCollidedHorizontally && MoveUtil.isMoving() && time - lastStep >= delay.getInput()) {
            stepping = true;
            lastStep = time;
        }
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer.onGround) {
            offGroundTicks = 0;
        } else if (offGroundTicks != -1) {
            offGroundTicks++;
        }

        if (stepping) {
            if (!MoveUtil.isMoving() || Utils.jumpDown() || (!mc.thePlayer.isCollidedHorizontally && offGroundTicks != 3)) {
                stepping = false;
                return;
            }

            switch (offGroundTicks) {
                case 0:
                    MoveUtil.stop();
                    MoveUtil.strafe();
                    mc.thePlayer.jump();
                    break;
                case 3:
                    MoveUtil.moveFlying(0.1);
                    mc.thePlayer.motionY = MoveUtil.predictedMotion(mc.thePlayer.motionY, 2);
                    break;
            }
        }
    }
}
