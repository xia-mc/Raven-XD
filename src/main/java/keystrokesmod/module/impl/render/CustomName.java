package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;

public class CustomName extends Module {
    public final ButtonSetting info;

    public CustomName() {
        super("CustomName", category.render, "allow you change module's name.");
        this.registerSetting(info = new ButtonSetting("Info", false, setting -> ModuleManager.sort()));
        this.registerSetting(new DescriptionSetting("Command: rename [module] [name] <info>"));
    }

    @Override
    public void onEnable() {
        ModuleManager.sort();
    }

    @Override
    public void onDisable() {
        ModuleManager.sort();
    }
}
