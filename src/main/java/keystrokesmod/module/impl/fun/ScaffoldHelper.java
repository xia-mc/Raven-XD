package keystrokesmod.module.impl.fun;

import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;

public class ScaffoldHelper extends Module {
    private final SliderSetting straightTicks;
    private final SliderSetting diagonalTicks;
    private final SliderSetting jumpDownTicks;

    private int offGroundTicks = 0;
    private int onGroundTicks = 0;

    public ScaffoldHelper() {
        super("ScaffoldHelper", category.fun, "√ Scaffold Enabled\n× Scaffold Disabled\n√ Scaffold Enabled\n× Scaffold Disabled\n√ Scaffold Enabled\n× Scaffold Disabled");
        this.registerSetting(straightTicks = new SliderSetting("Straight ticks", 6, 1, 8, 1));
        this.registerSetting(diagonalTicks = new SliderSetting("Diagonal ticks", 4, 1, 8, 1));
        this.registerSetting(jumpDownTicks = new SliderSetting("Jump down ticks", 1, 1, 8, 1));
    }

    @Override
    public void onEnable() throws Exception {
        scaEnable();
    }

    @Override
    public void onDisable() throws Exception {
        scaDisable();
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
            else if (MoveUtil.isMoving() && !Utils.jumpDown())
                mc.thePlayer.jump();
        } else if (BlockUtils.insideBlock(mc.thePlayer.getEntityBoundingBox().offset(mc.thePlayer.motionX * 0.5, mc.thePlayer.motionY, mc.thePlayer.motionZ * 0.5))) {
            scaDisable();
        } else {
            if (Utils.jumpDown()) {
                if (offGroundTicks == (int) jumpDownTicks.getInput()) {
                    scaEnable();
                }
            } else {
                if (Scaffold.isDiagonal()) {
                    if (offGroundTicks == (int) diagonalTicks.getInput()) {
                        scaEnable();
                    }
                } else {
                    if (offGroundTicks == (int) straightTicks.getInput()) {
                        scaEnable();
                    }
                }
            }
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
