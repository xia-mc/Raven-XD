package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;

public class CustomName extends Module {
    public final ButtonSetting info;

    public CustomName() {
        super("CustomName", category.render, "allow you change module's name.");
        this.registerSetting(info = new ButtonSetting("Info", false));
        this.registerSetting(new DescriptionSetting("Command: rename [module] [name] <info>"));
    }
}
