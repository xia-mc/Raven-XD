package keystrokesmod.module.impl.movement.step;

import keystrokesmod.event.MoveEvent;
import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.SprintEvent;
import keystrokesmod.module.impl.movement.Step;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static keystrokesmod.module.impl.world.tower.HypixelTower.isGoingDiagonally;
import static keystrokesmod.module.impl.world.tower.HypixelTower.randomAmount;

public class HypixelStep extends SubMode<Step> {
    private final SliderSetting boost = new SliderSetting("Boost", 0, 0, 0.4, 0.1);
    private final SliderSetting delay = new SliderSetting("Delay", 0, 0, 5000, 250, "ms");

    private int offGroundTicks = -1;
    private boolean stepping = false;
    private long lastStep = -1;

    public HypixelStep(String name, Step parent) {
        super(name, parent);
        this.registerSetting(boost, delay);
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
    public void onMove(MoveEvent event) {
        if (mc.thePlayer.onGround) {
            offGroundTicks = 0;
        } else if (offGroundTicks != -1) {
            offGroundTicks++;
        }

        if (stepping) {
            if (!MoveUtil.isMoving() || Utils.jumpDown() || (!mc.thePlayer.isCollidedHorizontally && offGroundTicks > 5)) {
                stepping = false;
                return;
            }

            if (mc.thePlayer.isPotionActive(Potion.jump)) return;
            final boolean airUnder = !BlockUtils.insideBlock(
                    mc.thePlayer.getEntityBoundingBox()
                            .offset(0, -1, 0)
                            .expand(0.239, 0, 0.239)
            );;
            final float speed = isGoingDiagonally(0.1) ? 0.22F : 0.29888888F;

            switch (offGroundTicks) {
                case 0:
                    event.setY(mc.thePlayer.motionY = 0.4198479950428009);
                    MoveUtil.strafe(speed - randomAmount());
                    break;
                case 1:
                    event.setY(Math.floor(mc.thePlayer.posY + 1.0) - mc.thePlayer.posY);
                    break;
                case 5:
                    if (mc.thePlayer.isCollidedHorizontally || !BlockUtils.blockRelativeToPlayer(0, -1, 0).isFullCube())
                        return;
                    MoveUtil.moveFlying(boost.getInput());
                    mc.thePlayer.motionY = MoveUtil.predictedMotion(mc.thePlayer.motionY, 2);
                    break;
            }
        }
    }

    @SubscribeEvent
    public void onSprint(SprintEvent event) {
        if (stepping) {
            event.setOmni(true);
        }
    }
}
