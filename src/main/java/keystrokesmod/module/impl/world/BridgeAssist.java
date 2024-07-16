package keystrokesmod.module.impl.world;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import keystrokesmod.utility.Utils;

public class BridgeAssist extends Module {
    private final ButtonSetting sneakOnly;
    private final SliderSetting waitTime;
    private final SliderSetting alignSpeed;
    private final SliderSetting assistRange;
    private final DescriptionSetting description;
    private final ModeSetting bridgeMode;
    private boolean isWaitingForAim;
    private boolean isGliding;
    private long startWaitTime;
    private final float[] godbridgeAngles = {75.6f, -315, -225, -135, -45, 0, 45, 135, 225, 315};
    private final float[] moonwalkAngles = {79.6f, -340, -290, -250, -200, -160, -110, -70, -20, 0, 20, 70, 110, 160, 200, 250, 290, 340};
    private final float[] breezilyAngles = {79.9f, -360, -270, -180, -90, 0, 90, 180, 270, 360};
    private final float[] normalAngles = {78f, -315, -225, -135, -45, 0, 45, 135, 225, 315};
    private double speedYaw, speedPitch;
    private float targetYaw, targetPitch;

    private static final String NORMAL = "Normal";
    private static final String GODBRIDGE = "Godbridge";
    private static final String MOONWALK = "Moonwalk";
    private static final String BREEZILY = "Breezily";
    private static final String[] BRIDGE_MODES = new String[]{NORMAL, GODBRIDGE, MOONWALK, BREEZILY};

    public BridgeAssist() {
        super("BridgeAssist", Module.category.world);
        this.registerSetting(description = new DescriptionSetting("Aligns you for bridging"));
        this.registerSetting(waitTime = new SliderSetting("Wait time (ms)", 500, 0, 5000, 25));
        this.registerSetting(bridgeMode = new ModeSetting("Mode", BRIDGE_MODES, 1));
        this.registerSetting(sneakOnly = new ButtonSetting("Work only when sneaking", false));
        this.registerSetting(assistRange = new SliderSetting("Assist range", 25.0D, 1.0D, 40.0D, 1.0D));
        this.registerSetting(alignSpeed = new SliderSetting("Align speed", 130, 1, 201, 5));
    }

    @Override
    public void onEnable() {
        this.isWaitingForAim = false;
        this.isGliding = false;
        super.onEnable();
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent e) {
        if (!Utils.isPlayerInGame()) {
            return;
        }

        if (!(Utils.Player.playerOverAir() && mc.thePlayer.onGround)) {
            return;
        }

        if (sneakOnly.isToggled() && !mc.thePlayer.isSneaking()) {
            return;
        }

        if (isGliding) {
            float yaw = mc.thePlayer.rotationYaw;
            float pitch = mc.thePlayer.rotationPitch;

            float yawDiff = yaw - ((float) (int) yaw / 360) * 360;
            float pitchDiff = pitch - ((float) (int) pitch / 360) * 360;

            double yawAbs = Math.abs(yawDiff - targetYaw);
            double pitchAbs = Math.abs(pitchDiff - targetPitch);

            if (speedYaw > yawAbs || speedYaw > yawAbs) {
                mc.thePlayer.rotationYaw = targetYaw;
            }

            if (speedPitch > pitchAbs || speedPitch > pitchAbs) {
                mc.thePlayer.rotationPitch = targetPitch;
            }

            if (mc.thePlayer.rotationYaw < targetYaw) {
                mc.thePlayer.rotationYaw += (float) speedYaw;
            }

            if (mc.thePlayer.rotationYaw > targetYaw) {
                mc.thePlayer.rotationYaw -= (float) speedYaw;
            }

            if (mc.thePlayer.rotationPitch > targetPitch) {
                mc.thePlayer.rotationPitch -= (float) speedPitch;
            }

            if (mc.thePlayer.rotationYaw == targetYaw && mc.thePlayer.rotationPitch == targetPitch) {
                isGliding = false;
                isWaitingForAim = false;
            }
            return;
        }

        if (!isWaitingForAim) {
            isWaitingForAim = true;
            startWaitTime = System.currentTimeMillis();
            return;
        }

        if (System.currentTimeMillis() - startWaitTime < waitTime.getInput())
            return;

        float yaw = mc.thePlayer.rotationYaw;
        float pitch = mc.thePlayer.rotationPitch;

        float range = (float) assistRange.getInput();

        //Godbridge
        if (bridgeMode.getInput() == 1) {
            if (Utils.isInRange(pitch, godbridgeAngles[0], range)) {
                for (float angle : godbridgeAngles) {
                    if (Utils.isInRange(yaw, angle, range)) {
                        setTargetAngles(godbridgeAngles[0], angle);
                        isWaitingForAim = false;
                        return;
                    }
                }
            }
        }

        //Moonwalk
        else if (bridgeMode.getInput() == 2) {
            if (Utils.isInRange(pitch, moonwalkAngles[0], range)) {
                for (float angle : moonwalkAngles) {
                    if (Utils.isInRange(yaw, angle, range)) {
                        setTargetAngles(moonwalkAngles[0], angle);
                        isWaitingForAim = false;
                        return;
                    }
                }
            }
        }

        //Breezily
        else if (bridgeMode.getInput() == 3) {
            if (Utils.isInRange(pitch, breezilyAngles[0], range)) {
                for (float angle : breezilyAngles) {
                    if (Utils.isInRange(yaw, angle, range)) {
                        setTargetAngles(breezilyAngles[0], angle);
                        isWaitingForAim = false;
                        return;
                    }
                }
            }
        }

        //Normal
        else {
            if (Utils.isInRange(pitch, normalAngles[0], range)) {
                for (float angle : normalAngles) {
                    if (Utils.isInRange(yaw, angle, range)) {
                        setTargetAngles(normalAngles[0], angle);
                        isWaitingForAim = false;
                        return;
                    }
                }
            }
        }
        isWaitingForAim = false;
    }

    private void setTargetAngles(double pitch, double yaw) {
        this.targetYaw = (float) yaw;
        this.targetPitch = (float) pitch;
        this.speedYaw = alignSpeed.getInput();
        this.speedPitch = alignSpeed.getInput();
        this.isGliding = true;
    }
}
