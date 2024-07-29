package keystrokesmod.module.impl.client.discordrpc;

import keystrokesmod.module.impl.client.DiscordRpc;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.Utils;
import net.arikia.dev.drpc.DiscordEventHandlers;
import net.arikia.dev.drpc.DiscordRPC;
import net.arikia.dev.drpc.DiscordRichPresence;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class LunarClientRPC extends SubMode<DiscordRpc> {
    private final String clientId = "562286213059444737";
    private final String mappingUrl = "https://servermappings.lunarclientcdn.com/servers.json";
    private static final String DEFAULT_SERVER_NAME = "Active in the launcher";
    private static final String DEFAULT_IMAGE = "logo-default";
    private static final String DEFAULT_SMALL_IMAGE = "logo-blue";
    private Map<String, ServerData> serverDataMap = new HashMap<>();
    private boolean started;
    private String serverName;
    private String bigImage;
    private String serveraddresses;

    public LunarClientRPC(String name, DiscordRpc parent) {
        super(name, parent);
    }

    private static class ServerData {
        String name;
        String logo;
    }

    @Override
    public void onUpdate() {
        String currentServerIP = mc.getCurrentServerData() != null ? mc.getCurrentServerData().serverIP : null;

        if (!started || (!Utils.nullCheck() && DEFAULT_SERVER_NAME.equals(serverName)) ||
                (currentServerIP != null && !currentServerIP.endsWith(serveraddresses))) {
            if (started) {
                onDisable();
            }
            if (serverDataMap.isEmpty()) {
                try {
                    fetchServerMappings();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (Utils.nullCheck()) {
                if (findServerData(currentServerIP)) {
                    DiscordRPC.discordUpdatePresence(makeRPC(DEFAULT_SMALL_IMAGE, "Lunar Client", serverName));
                } else {
                    updateDefaultRPC("version-1_8", "Minecraft 1.8.9", "Lunar Client");
                }
            } else {
                serveraddresses = null;
                updateDefaultRPC("", "", "");
            }

            DiscordEventHandlers handlers = new DiscordEventHandlers();
            DiscordRPC.discordInitialize(clientId, handlers, true);
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

    private void fetchServerMappings() throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(mappingUrl).openConnection();
        connection.setRequestMethod("GET");
        String response = readFromConnection(connection);
        connection.disconnect();

        JSONArray jsonArray = new JSONArray(response);
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject serverJson = jsonArray.getJSONObject(i);
            String primaryAddress = serverJson.getString("primaryAddress").toLowerCase();
            String name = serverJson.getString("name");
            String logo = serverJson.getJSONObject("images").getString("logo");

            ServerData serverData = new ServerData();
            serverData.name = name;
            serverData.logo = logo;

            serverDataMap.put(primaryAddress, serverData);

            JSONArray addressesArray = serverJson.getJSONArray("addresses");
            for (int j = 0; j < addressesArray.length(); j++) {
                String address = addressesArray.getString(j).toLowerCase();
                serverDataMap.put(address, serverData);
            }
        }
    }

    private static String readFromConnection(HttpURLConnection connection) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }

    private boolean findServerData(String serverIP) {
        if (serverIP == null) return false;
        serverIP = serverIP.toLowerCase();
        serveraddresses = serverIP;
        ServerData serverData = serverDataMap.get(serverIP);

        if (serverData != null) {
            serverName = "Playing " + serverData.name;
            bigImage = serverData.logo;
            return true;
        }
        for (Map.Entry<String, ServerData> entry : serverDataMap.entrySet()) {
            String knownAddress = entry.getKey();
            if (serverIP.endsWith(knownAddress)) {
                serverData = entry.getValue();
                serverName = "Playing " + serverData.name;
                bigImage = serverData.logo;
                return true;
            }
        }

        return false;
    }


    private void updateDefaultRPC(String small, String smallText, String bigText) {
        bigImage = DEFAULT_IMAGE;
        serverName = "Playing Private Server";
        DiscordRPC.discordUpdatePresence(makeRPC(small, smallText, bigText));
    }

    public DiscordRichPresence makeRPC(String small, String smallText, String bigText) {
        if (bigText.startsWith("Playing")) bigText = bigText.substring(8);
        return new DiscordRichPresence.Builder("")
                .setDetails(serverName)
                .setBigImage(bigImage, bigText)
                .setSmallImage(small, smallText)
                .setStartTimestamps(System.currentTimeMillis())
                .build();
    }
}
