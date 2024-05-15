package keystrokesmod.module.impl.client;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.utility.Commands;
import keystrokesmod.utility.Timer;

public class CommandLine extends Module {
    public static boolean isEnable = false;
    public static boolean isDisable = false;
    public static Timer animationTimer;
    public static ButtonSetting animate;

    public CommandLine() {
        super("Command line", Module.category.client, 0);
        this.registerSetting(animate = new ButtonSetting("Animate", true));
    }

    public void onEnable() {
        Commands.setccs();
        isEnable = true;
        isDisable = false;
        (animationTimer = new Timer(500.0F)).start();
    }

    public void onDisable() {
        isDisable = true;
        if (animationTimer != null) {
            animationTimer.start();
        }

        Commands.od();
    }
}
