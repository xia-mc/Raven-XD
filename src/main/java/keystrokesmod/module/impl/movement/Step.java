package keystrokesmod.module.impl.movement;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Step extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", new String[]{"Hypixel 1.5"}, 0);
    private final SliderSetting delay = new SliderSetting("Delay", 1000, 0, 5000, 250, "ms");
    private final ButtonSetting test = new ButtonSetting("Test", false, new ModeOnly(mode, 0));

    private int offGroundTicks = -1;
    private boolean stepping = false;
    private long lastStep = -1;

    public Step() {
        super("Step", category.movement);
        this.registerSetting(mode, delay, test);
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
            if (!MoveUtil.isMoving() || Utils.jumpDown() || !mc.thePlayer.isCollidedHorizontally) {
                stepping = false;
                return;
            }

            switch (test.isToggled() ? offGroundTicks % 16 : offGroundTicks) {
                case 0:
                    MoveUtil.stop();
                    MoveUtil.strafe();
                    mc.thePlayer.jump();
                    break;
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                    MoveUtil.stop();
                    break;
                case 10:
                case 11:
                case 13:
                case 14:
                case 15:
                    mc.thePlayer.motionY = 0;
                    MoveUtil.stop();
                    break;
                case 16:
                    mc.thePlayer.jump();
                    stepping = false;
                    break;
            }
        }
    }

    @Override
    public String getInfo() {
        return mode.getOptions()[(int) mode.getInput()];
    }
}
