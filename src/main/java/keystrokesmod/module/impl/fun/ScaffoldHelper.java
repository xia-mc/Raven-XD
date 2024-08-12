package keystrokesmod.module.impl.fun;

import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;

public class ScaffoldHelper extends Module {
    private final SliderSetting tick;

    private int offGroundTicks = 0;
    private int onGroundTicks = 0;

    public ScaffoldHelper() {
        super("ScaffoldHelper", category.fun, "√ Scaffold Enabled × Scaffold Disabled");
        this.registerSetting(tick = new SliderSetting("Tick", 1, 1, 11, 1));
    }

    @Override
    public void onUpdate() {
        if (mc.thePlayer.onGround) {
            offGroundTicks = 0;
            onGroundTicks++;
        } else {
            offGroundTicks++;
            onGroundTicks = 0;
        }

        if (offGroundTicks == 0) {
            if (onGroundTicks == 0)
                scaDisable();
            if (onGroundTicks == 1)
                if (MoveUtil.isMoving() && !Utils.jumpDown())
                    mc.thePlayer.jump();
        } else if (BlockUtils.insideBlock(mc.thePlayer.getEntityBoundingBox().offset(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ))) {
            scaDisable();
        } else if (offGroundTicks == (int) tick.getInput()) {
            scaEnable();
        }
    }

    private void scaEnable() {
        if (ModuleManager.scaffold.isEnabled()) return;
        ModuleManager.scaffold.toggle();
    }

    private void scaDisable() {
        if (!ModuleManager.scaffold.isEnabled()) return;
        ModuleManager.scaffold.toggle();
    }
}
