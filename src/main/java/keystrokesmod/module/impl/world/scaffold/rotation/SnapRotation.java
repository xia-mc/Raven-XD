package keystrokesmod.module.impl.world.scaffold.rotation;

import keystrokesmod.event.RotationEvent;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.impl.world.scaffold.IScaffoldRotation;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.aim.RotationData;
import org.jetbrains.annotations.NotNull;

public class SnapRotation extends IScaffoldRotation {
    private final ButtonSetting changePitch;
    private final ButtonSetting grimAC;
    private final ButtonSetting visualSprint;

    public SnapRotation(String name, @NotNull Scaffold parent) {
        super(name, parent);
        this.registerSetting(changePitch = new ButtonSetting("Change pitch", false));
        this.registerSetting(grimAC = new ButtonSetting("GrimAC", false));
        this.registerSetting(visualSprint = new ButtonSetting("Visual sprint", true));
    }

    @Override
    public @NotNull RotationData onRotation(float placeYaw, float placePitch, boolean forceStrict, @NotNull RotationEvent event) {
        if (visualSprint.isToggled() && MoveUtil.isMoving())
            mc.thePlayer.setSprinting(true);

        if (parent.place) {
            float yaw = placeYaw;

            if (grimAC.isToggled()) {
                yaw += (float) ((Math.random() - 0.5) * 0.2);
            }

            return new RotationData(yaw, placePitch);
        } else {
            return new RotationData(event.getYaw(), changePitch.isToggled() ? event.getPitch() : placePitch);
        }
    }
}
