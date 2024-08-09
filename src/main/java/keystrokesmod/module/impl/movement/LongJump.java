package keystrokesmod.module.impl.movement;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.movement.longjump.HypixelFireballLongJump;
import keystrokesmod.module.impl.movement.longjump.HypixelLongJump;
import keystrokesmod.module.impl.movement.longjump.VulcanLongJump;
import keystrokesmod.module.setting.impl.ModeValue;

public class LongJump extends Module {
    private final ModeValue mode;

    public LongJump() {
        super("LongJump", category.movement);
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new HypixelLongJump("Hypixel", this))
                .add(new HypixelFireballLongJump("HypixelFireball", this))
                .add(new VulcanLongJump("Vulcan", this))
        );
    }

    @Override
    public void onEnable() {
        mode.enable();
    }

    @Override
    public void onDisable() {
        mode.disable();
    }

    @Override
    public String getInfo() {
        return mode.getSelected().getPrettyName();
    }
}
