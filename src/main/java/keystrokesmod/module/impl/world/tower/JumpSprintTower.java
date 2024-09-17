package keystrokesmod.module.impl.world.tower;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.module.impl.world.Tower;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.Reflection;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import static keystrokesmod.module.ModuleManager.tower;

public class JumpSprintTower extends SubMode<Tower> {
    private final SliderSetting speed;
    private final ButtonSetting noStrafe;
    private final SliderSetting offGroundSpeed;
    private final ModeSetting lowHop;
    private int offGroundTicks = 0;
    private int onGroundTicks = 0;
    private boolean lowHopTest1$watchdog = false;

    public JumpSprintTower(String name, @NotNull Tower parent) {
        super(name, parent);
        this.registerSetting(speed = new SliderSetting("Speed", 0.95, 0.5, 1, 0.01));
        this.registerSetting(offGroundSpeed = new SliderSetting("Hypixel off ground speed", 0.5, 0.0, 1.0, 0.01));
        this.registerSetting(noStrafe = new ButtonSetting("Hypixel no strafe", false));
        this.registerSetting(lowHop = new ModeSetting("Low hop", new String[]{"None", "Default", "Test1", "Test2"}, 0));
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) throws IllegalAccessException {
        if (tower.canTower()) {
            Reflection.jumpTicks.set(mc.thePlayer, 0);
            event.setSprinting(false);

            double moveSpeed = event.isOnGround() ? speed.getInput() : offGroundSpeed.getInput();
            if (noStrafe.isToggled()) {
                if (Math.abs(mc.thePlayer.motionX) >= Math.abs(mc.thePlayer.motionZ)) {
                    mc.thePlayer.motionX *= moveSpeed;
                    mc.thePlayer.motionZ = 0;
                } else {
                    mc.thePlayer.motionZ *= moveSpeed;
                    mc.thePlayer.motionX = 0;
                }
            } else {
                mc.thePlayer.motionX *= moveSpeed;
                mc.thePlayer.motionZ *= moveSpeed;
            }

            if (lowHop.getInput() == 2) {
                onGroundTicks = mc.thePlayer.onGround ? onGroundTicks + 1 : 0;
                lowHopTest1$watchdog = (lowHopTest1$watchdog || onGroundTicks == 1) && onGroundTicks < 2;
                if (onGroundTicks > 0)
                    event.setPosY(event.getPosY() + 1E-14);

                if (lowHopTest1$watchdog) {
                    if (mc.thePlayer.motionY == 0.16477328182606651) mc.thePlayer.motionY = 0.14961479459521598;
                    if (mc.thePlayer.motionY == 0.0682225000311085) mc.thePlayer.motionY = 0.0532225003663811;
                    if (mc.thePlayer.motionY == -0.0262419501516868) mc.thePlayer.motionY = -0.027141950136226;
                    if (mc.thePlayer.motionY == -0.104999113177072) mc.thePlayer.motionY = -0.31999911675335113;
                    if (mc.thePlayer.motionY == -0.3919991420476618) mc.thePlayer.motionY = -0.3968991421057737;
                }
            }
            }
        }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer.onGround) {
            offGroundTicks = 0;
        } else {
            offGroundTicks++;
        }

        if (tower.canTower() && Utils.isMoving()) {
            switch ((int) lowHop.getInput()) {
                default:
                case 0:
                    break;
                case 1:
                    switch (offGroundTicks) {
                        case 0:
                            mc.thePlayer.motionY = 0.4196;
                            break;
                        case 3:
                        case 4:
                            mc.thePlayer.motionY = 0;
                            break;
                        case 5:
                            mc.thePlayer.motionY = 0.4191;
                            break;
                        case 6:
                            mc.thePlayer.motionY = 0.3275;
                            break;
                        case 11:
                            mc.thePlayer.motionY = -0.5;
                            break;
                    }
                    break;
                case 3:
                    switch (offGroundTicks) {
                        case 0:
                            mc.thePlayer.motionY = 0.4191;
                            break;
                        case 1:
                            mc.thePlayer.motionY = 0.327318;
                            break;
                        case 4:
                            mc.thePlayer.motionY = 0.065;
                            break;
                        case 5:
                            mc.thePlayer.motionY = -0.005;
                            break;
                        case 6:
                            mc.thePlayer.motionY = -1.0;
                            break;
                    }
                    break;
            }
        }
    }
}
