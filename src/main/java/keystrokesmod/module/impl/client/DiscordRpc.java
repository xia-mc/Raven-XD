package keystrokesmod.module.impl.client;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.client.discordrpc.LunarClientRPC;
import keystrokesmod.module.setting.impl.ModeValue;

public class DiscordRpc extends Module {
    private final ModeValue mode;
    public DiscordRpc() {
        super("DiscordRPC", category.client);
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new LunarClientRPC("Lunar Client", this))
                .setDefaultValue("Lunar Client"));
    }
    public void onEnable() {
        mode.enable();
    }

    public void onDisable() {
        mode.disable();
    }
}
