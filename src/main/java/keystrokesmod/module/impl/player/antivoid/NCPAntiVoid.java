package keystrokesmod.module.impl.player.antivoid;

import keystrokesmod.module.impl.player.AntiVoid;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.Utils;
import org.jetbrains.annotations.NotNull;

public class NCPAntiVoid extends SubMode<AntiVoid> {
    private final SliderSetting distance;

    private Vec3 lastOnGroundPos = Vec3.ZERO;

    public NCPAntiVoid(String name, @NotNull AntiVoid parent) {
        super(name, parent);
        this.registerSetting(distance = new SliderSetting("Distance", 5, 0, 10, 1));
    }

    @Override
    public void onEnable() throws Throwable {
        lastOnGroundPos = new Vec3(mc.thePlayer);
    }

    @Override
    public void onUpdate() throws Throwable {
        if (mc.thePlayer.fallDistance > distance.getInput() && Utils.overVoid() && !mc.thePlayer.onGround) {
            mc.thePlayer.setPosition(lastOnGroundPos.x, lastOnGroundPos.y, lastOnGroundPos.z);
        } else if (mc.thePlayer.onGround) {
            lastOnGroundPos = new Vec3(mc.thePlayer);
        }
    }

}
