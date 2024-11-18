package keystrokesmod.module.impl.fun.antiaim;

import keystrokesmod.event.RotationEvent;
import keystrokesmod.module.impl.fun.AntiAim;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;


public class SpinAntiAim extends SubMode<AntiAim> {
    private final SliderSetting speed = new SliderSetting("Speed", 25, -30, 30, 1);
    private final ButtonSetting constantPitch = new ButtonSetting("Constant pitch", true);
    private final SliderSetting pitch = new SliderSetting("Pitch", 90, -90, 90, 5, constantPitch::isToggled);
    public boolean pitchReserve = false;
    private Float lastYaw = null;
    private Float lastPitch = null;

    public SpinAntiAim(String name, AntiAim parent) {
        super(name, parent);
        this.registerSetting(speed, constantPitch, pitch);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRotation(@NotNull RotationEvent event) {
        if (!parent.canAntiAim()) {
            onDisable();
            return;
        }

        if (lastYaw == null) {
            lastYaw = event.getYaw();
        }
        event.setYaw(lastYaw += (float) speed.getInput());


        if (constantPitch.isToggled()) {
            event.setPitch((float) pitch.getInput());
        } else {
            if (lastPitch == null) {
                lastPitch = event.getPitch();
            }

            pitchCheck();
            lastPitch += (float) speed.getInput() * (pitchReserve ? -1 : 1);
            pitchCheck();
            event.setPitch(lastPitch);
        }
    }

    private void pitchCheck() {
        if (lastPitch >= 90) {
            lastPitch = 90f;
            pitchReserve = true;
        } else if (lastPitch <= -90) {
            lastPitch = -90f;
            pitchReserve = false;
        }
    }

    @Override
    public void onDisable() {
        lastYaw = null;
        lastPitch = null;
        pitchReserve = false;
    }
}

