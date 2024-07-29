package keystrokesmod.module.impl.client.discordrpc;

import keystrokesmod.module.impl.client.DiscordRpc;
import keystrokesmod.module.setting.impl.SubMode;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;

public class BadlionRPC extends SubMode<DiscordRpc> {
    private final String clientId = "418076578333851669";
    private boolean started;

    public BadlionRPC(String name, DiscordRpc parent) {
        super(name, parent);
    }

    @Override
    public void onUpdate() {
        if (!started) {
            DiscordRPC.discordInitialize(clientId, new DiscordEventHandlers.Builder().setReadyEventHandler(user -> {
                DiscordRichPresence.Builder presence = new DiscordRichPresence.Builder("");
                presence.setDetails("Playing Minecraft 1.8.9");
                presence.setBigImage("newlogo", "Using Badlion Client Minecraft Launcher").setStartTimestamps(System.currentTimeMillis());
                DiscordRPC.discordUpdatePresence(presence.build());
            }).build(), true);
            new Thread(() -> {
                while (this.isEnabled()) {
                    DiscordRPC.discordRunCallbacks();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "Discord RPC Callback").start();
            started = true;
        }
    }

    @Override
    public void onDisable() {
        DiscordRPC.discordShutdown();
        started = false;
    }
}
