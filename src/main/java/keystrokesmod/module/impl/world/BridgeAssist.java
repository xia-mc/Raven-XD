package keystrokesmod.module.impl.world;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;

public class BridgeAssist extends Module {
    private final ButtonSetting onSneak;
    private final SliderSetting waitFor;
    private final SliderSetting glideTime;
    private final SliderSetting assistRange;
    private final DescriptionSetting description;
    private final ModeSetting assistMode;
    private boolean waitingForAim;
    private boolean gliding;
    private long startWaitTime;
    private final float[] godbridgePos = {75.6f, -315, -225, -135, -45, 0, 45, 135, 225, 315};
    private final float[] moonwalkPos = {79.6f, -340, -290, -250, -200, -160, -110, -70, -20, 0, 20, 70, 110, 160, 200, 250, 290, 340};
    private final float[] breezilyPos = {79.9f, -360, -270, -180, -90, 0, 90, 180, 270, 360};
    private final float[] normalPos = {78f, -315, -225, -135, -45, 0, 45, 135, 225, 315};
    private double speedYaw, speedPitch;
    private float waitingForYaw, waitingForPitch;

    private static final String NORMAL = "Normal";
    private static final String GODBRIDGE = "GodBridge";
    private static final String MOONWALK = "Moonwalk";
    private static final String BREEZILY = "Breezily";
    private static final String[] BridgeModes = new String[]{NORMAL, GODBRIDGE, MOONWALK, BREEZILY};

    public BridgeAssist() {
        super("BridgeAssist", Module.category.world);
        this.registerSetting(description = new DescriptionSetting("Aligns you for bridging"));
        this.registerSetting(waitFor = new SliderSetting("Wait time (ms)", 500, 0, 5000, 25));
        this.registerSetting(assistMode = new ModeSetting("Mode", BridgeModes, 1));
        this.registerSetting(onSneak = new ButtonSetting("Work only when sneaking", true));
        this.registerSetting(assistRange = new SliderSetting("Assist range", 10.0D, 1.0D, 40.0D, 1.0D));
        this.registerSetting(glideTime = new SliderSetting("Align speed", 150, 1, 201, 5));
    }

    @Override
    public void onEnable() {
        this.waitingForAim = false;
        this.gliding = false;
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent e) {
        if (!Utils.nullCheck()) {
            return;
        }

        if (!(Utils.overAir() && mc.thePlayer.onGround)) {
            return;
        }

        if (onSneak.isToggled()) {
            if (!mc.thePlayer.isSneaking()) {
                return;
            }
        }

        if (gliding) {
            float fuckedYaw = mc.thePlayer.rotationYaw;
            float fuckedPitch = mc.thePlayer.rotationPitch;

            float yaw = fuckedYaw - ((int) fuckedYaw / 360) * 360;
            float pitch = fuckedPitch - ((int) fuckedPitch / 360) * 360;

            double ilovebloat1 = yaw - speedYaw,
                    ilovebloat2 = yaw + speedYaw,
                    ilovebloat3 = pitch - speedPitch,
                    ilovebloat4 = pitch + speedPitch;

            ilovebloat1 = Math.abs(ilovebloat1);
            ilovebloat2 = Math.abs(ilovebloat2);
            ilovebloat3 = Math.abs(ilovebloat3);
            ilovebloat4 = Math.abs(ilovebloat4);

            if (this.speedYaw > ilovebloat1 || this.speedYaw > ilovebloat2)
                mc.thePlayer.rotationYaw = this.waitingForYaw;

            if (this.speedPitch > ilovebloat3 || this.speedPitch > ilovebloat4)
                mc.thePlayer.rotationPitch = this.waitingForPitch;

            if (mc.thePlayer.rotationYaw < this.waitingForYaw)
                mc.thePlayer.rotationYaw += this.speedYaw;

            if (mc.thePlayer.rotationYaw > this.waitingForYaw)
                mc.thePlayer.rotationYaw -= this.speedYaw;

            if (mc.thePlayer.rotationPitch > this.waitingForPitch)
                mc.thePlayer.rotationPitch -= this.speedPitch;

            if (mc.thePlayer.rotationYaw == this.waitingForYaw && mc.thePlayer.rotationPitch == this.waitingForPitch) {
                gliding = false;
                this.waitingForAim = false;
            }
            return;
        }

        if (!waitingForAim) {
            waitingForAim = true;
            startWaitTime = System.currentTimeMillis();
            return;
        }

        if (System.currentTimeMillis() - startWaitTime < waitFor.getInput())
            return;

        float fuckedYaw = mc.thePlayer.rotationYaw;
        float fuckedPitch = mc.thePlayer.rotationPitch;

        float yaw = fuckedYaw - ((int) fuckedYaw / 360) * 360;
        float pitch = fuckedPitch - ((int) fuckedPitch / 360) * 360;

        float range = (float) assistRange.getInput();

        //GodBridge
        if (assistMode.getInput() == 1) {
            if (godbridgePos[0] >= (pitch - range) && godbridgePos[0] <= (pitch + range)) {
                for (int k = 1; k < godbridgePos.length; k++) {
                    if (godbridgePos[k] >= (yaw - range) && godbridgePos[k] <= (yaw + range)) {
                        aimAt(godbridgePos[0], godbridgePos[k], fuckedYaw, fuckedPitch);
                        this.waitingForAim = false;
                        return;
                    }
                }
            }
        }

        //Moonwalk
        else if (assistMode.getInput() == 2) {
            if (moonwalkPos[0] >= (pitch - range) && moonwalkPos[0] <= (pitch + range)) {
                for (int k = 1; k < moonwalkPos.length; k++) {
                    if (moonwalkPos[k] >= (yaw - range) && moonwalkPos[k] <= (yaw + range)) {
                        aimAt(moonwalkPos[0], moonwalkPos[k], fuckedYaw, fuckedPitch);
                        this.waitingForAim = false;
                        return;
                    }
                }
            }
        }

        //Breezily
        else if (assistMode.getInput() == 3) {
            if (breezilyPos[0] >= (pitch - range) && breezilyPos[0] <= (pitch + range)) {
                for (int k = 1; k < breezilyPos.length; k++) {
                    if (breezilyPos[k] >= (yaw - range) && breezilyPos[k] <= (yaw + range)) {
                        aimAt(breezilyPos[0], breezilyPos[k], fuckedYaw, fuckedPitch);
                        this.waitingForAim = false;
                        return;
                    }
                }
            }
        }

        //Normal
        else {
            if (normalPos[0] >= (pitch - range) && normalPos[0] <= (pitch + range)) {
                for (int k = 1; k < normalPos.length; k++) {
                    if (normalPos[k] >= (yaw - range) && normalPos[k] <= (yaw + range)) {
                        aimAt(normalPos[0], normalPos[k], fuckedYaw, fuckedPitch);
                        this.waitingForAim = false;
                        return;
                    }
                }
            }
        }
        this.waitingForAim = false;
    }

    public void aimAt(float pitch, float yaw, float fuckedYaw, float fuckedPitch){
        mc.thePlayer.rotationPitch = pitch + ((int)fuckedPitch/360) * 360;
        mc.thePlayer.rotationYaw = yaw;
    }
}