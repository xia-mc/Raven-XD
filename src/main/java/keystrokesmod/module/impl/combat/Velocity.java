package keystrokesmod.module.impl.combat;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.combat.velocity.*;
import keystrokesmod.module.setting.impl.ModeValue;

public class Velocity extends Module {
    private final ModeValue mode;

    public Velocity() {
        super("Velocity", category.combat, "Reduce knock-back.");
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new NormalVelocity("Normal", this))
                .add(new HypixelVelocity("Hypixel", this))
                .add(new IntaveVelocity("Intave", this))
                .add(new LegitVelocity("Legit", this))
                .add(new KarhuVelocity("Karhu", this))
                .add(new MatrixVelocity("Matrix", this))
                .add(new TickVelocity("Tick", this))
                .add(new ZipVelocity("7-Zip", this))
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
        return mode.getSubModeValues().get((int) mode.getInput()).getPrettyName();
    }
}