package keystrokesmod.module.impl.world.scaffold.sprint;

import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.impl.world.scaffold.IScaffoldSprint;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraft.potion.Potion;
import org.jetbrains.annotations.NotNull;

public class HypixelJumpSprint extends IScaffoldSprint {
    private final ButtonSetting visualSprint;

    public HypixelJumpSprint(String name, @NotNull Scaffold parent) {
        super(name, parent);
        this.registerSetting(visualSprint = new ButtonSetting("Visual sprint", true));
    }

    @Override
    public boolean onPreSchedulePlace() {
        if (visualSprint.isToggled() && MoveUtil.isMoving())
            mc.thePlayer.setSprinting(true);

        if (!MoveUtil.isMoving() || mc.thePlayer.isPotionActive(Potion.jump)) return true;

        switch (parent.offGroundTicks) {
            case 0:
                if (!Utils.jumpDown()) {
                    MoveUtil.strafe(MoveUtil.getAllowedHorizontalDistance(false));
                    mc.thePlayer.jump();
                }
                break;
            case 1:
                set(0.3380001194477087);
                break;
            case 2:
                set(0.25302801701974894);
                break;
            case 3:
                set(0.18916746036107118);
                break;
            case 4:
                set(-0.079601588676394617);
                break;
            case 5:
                set(-0.31289557200443413);
                break;
            case 6:
                set(-0.3862369221364473);
                break;
            case 7:
                set(-0.45299219251018796);
                break;
        }

        return true;
    }

    private void set(double motionY) {
        mc.thePlayer.motionY = motionY;
    }

    @Override
    public boolean isSprint() {
        return false;
    }
}
