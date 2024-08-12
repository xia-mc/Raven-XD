package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;

public class ClientSettings extends Module {
    public final ButtonSetting button;
    public final ButtonSetting background;
    public final ButtonSetting mainMenu;

    public ClientSettings() {
        super("ClientSettings", category.render);
        this.registerSetting(button = new ButtonSetting("Button", true));
        this.registerSetting(background = new ButtonSetting("Background", true));
        this.registerSetting(mainMenu = new ButtonSetting("Main menu", true));
    }
}
