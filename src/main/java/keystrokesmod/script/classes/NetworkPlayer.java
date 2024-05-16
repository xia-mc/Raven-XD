package keystrokesmod.script.classes;

import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;

public class NetworkPlayer {
    private final NetworkPlayerInfo networkPlayerInfo;
    protected NetworkPlayer(NetworkPlayerInfo networkPlayerInfo) {
        this.networkPlayerInfo = networkPlayerInfo;
    }

    public String getCape() {
        if (networkPlayerInfo == null) {
            return "";
        }
        return networkPlayerInfo.getLocationCape().getResourcePath();
    }

    public String getDisplayName() {
        if (networkPlayerInfo == null) {
            return "";
        }
        return networkPlayerInfo.getDisplayName() != null ? networkPlayerInfo.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfo.getPlayerTeam(), networkPlayerInfo.getGameProfile().getName());
    }

    public String getName() {
        if (networkPlayerInfo == null) {
            return "";
        }
        return networkPlayerInfo.getGameProfile().getName();
    }

    public int getPing() {
        if (networkPlayerInfo == null) {
            return 0;
        }
        return networkPlayerInfo.getResponseTime();
    }

    public String getSkinData() {
        if (networkPlayerInfo == null) {
            return "";
        }
        return networkPlayerInfo.getLocationSkin().getResourcePath();
    }

    public String getUUID() {
        if (networkPlayerInfo == null) {
            return "";
        }
        return networkPlayerInfo.getGameProfile().getId().toString();
    }
}
