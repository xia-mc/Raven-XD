package keystrokesmod.module.impl.player;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.player.antivoid.AirStuckAntiVoid;
import keystrokesmod.module.impl.player.antivoid.GrimACAntiVoid;
import keystrokesmod.module.impl.player.antivoid.HypixelAntiVoid;
import keystrokesmod.module.impl.player.antivoid.VulcanAntiVoid;
import keystrokesmod.module.setting.impl.ModeValue;

public class AntiVoid extends Module {
    private final ModeValue mode;

    public AntiVoid() {
        super("AntiVoid", category.player);
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new HypixelAntiVoid("Hypixel", this))
                .add(new AirStuckAntiVoid("AirStuck", this))
                .add(new VulcanAntiVoid("Vulcan", this))
                .add(new GrimACAntiVoid("GrimAC", this))
        );
    }

    @Override
    public void onEnable() throws Throwable {
        mode.enable();
    }

    @Override
    public void onDisable() throws Throwable {
        mode.disable();
    }
}
