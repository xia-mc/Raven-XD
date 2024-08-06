package keystrokesmod.module.impl.movement;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.movement.speed.*;
import keystrokesmod.module.setting.impl.*;
import keystrokesmod.utility.*;

import static keystrokesmod.module.ModuleManager.scaffold;

public class Speed extends Module {
    private final ModeValue mode;
    private final ButtonSetting liquidDisable;
    private final ButtonSetting sneakDisable;
    private final ButtonSetting stopMotion;
    public int offGroundTicks = 0;

    public Speed() {
        super("Speed", Module.category.movement);
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new LegitSpeed("Legit", this))
                .add(new HypixelASpeed("HypixelA", this))
                .add(new HypixelBSpeed("HypixelB", this))
                .add(new HypixelCSpeed("HypixelC", this))
                .add(new VanillaSpeed("Vanilla", this))
                .add(new BlocksMCSpeed("BlocksMC", this))
                .add(new VulcanSpeed("Vulcan", this))
                .add(new GrimACSpeed("GrimAC", this))
//                .add(new IntaveSpeed("Intave", this))
        );
        this.registerSetting(liquidDisable = new ButtonSetting("Disable in liquid", true));
        this.registerSetting(sneakDisable = new ButtonSetting("Disable while sneaking", true));
        this.registerSetting(stopMotion = new ButtonSetting("Stop motion", false));
    }

    @Override
    public String getInfo() {
        return mode.getSubModeValues().get((int) mode.getInput()).getInfo();
    }

    @Override
    public void onEnable() {
        mode.enable();
    }

    public void onUpdate() {
        if (mc.thePlayer.onGround) {
            offGroundTicks = 0;
        } else {
            offGroundTicks++;
        }
    }

    public boolean noAction() {
        return !Utils.nullCheck()
                || ((mc.thePlayer.isInWater() || mc.thePlayer.isInLava())
                && liquidDisable.isToggled())
                || (mc.thePlayer.isSneaking() && sneakDisable.isToggled())
                || scaffold.isEnabled();
    }

    @Override
    public void onDisable() {
        mode.disable();

        if (stopMotion.isToggled()) {
            MoveUtil.stop();
        }
        Utils.resetTimer();
    }
}
