package keystrokesmod.module.impl.movement;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.movement.teleport.HypixelTeleport;
import keystrokesmod.module.setting.impl.ModeValue;

public class Teleport extends Module {
    private final ModeValue mode;

    public Teleport() {
        super("Teleport", category.movement);
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new HypixelTeleport("Hypixel", this))
        );
    }

    @Override
    public void onEnable() throws Exception {
        mode.enable();
    }

    @Override
    public void onDisable() throws Exception {
        mode.disable();
    }
}
