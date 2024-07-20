package keystrokesmod.module.impl.other;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.client.Notifications;
import keystrokesmod.module.impl.minigames.BedWars;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;

public class BedProximityAlert extends Module {
    private final Map<String, Boolean> playerAlertStatus;
    private final SliderSetting Distance;
    private final ButtonSetting shouldPing;
    private final ButtonSetting tellTheteam;

    public BedProximityAlert() {
        super("BedProximityAlert", category.other);
        this.registerSetting(Distance = new SliderSetting("Distance", 40, 10, 120, 1));
        this.registerSetting(shouldPing = new ButtonSetting("Should ping", true));
        this.registerSetting(tellTheteam = new ButtonSetting("Tell the team", false));
        playerAlertStatus = new HashMap<>();
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            return;
        }

        EntityPlayer player = event.player;
        BlockPos spawnPos = BedWars.getSpawnPos();
        if (BedWars.whitelistOwnBed.isToggled() && spawnPos != null) {
            for (EntityPlayer otherPlayer : player.worldObj.playerEntities) {
                if (otherPlayer == player || Utils.isTeamMate(otherPlayer)) {
                    continue;
                }

                double distance = otherPlayer.getDistance(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
                String playerName = otherPlayer.getDisplayName().getFormattedText();

                if (distance <= Distance.getInput()) {
                    if (!playerAlertStatus.getOrDefault(playerName, false)) {
                        Notifications.sendNotification(Notifications.NotificationTypes.WARN, playerName + " is " + (int) distance + " blocks away from the bed!");
                        informTeam(playerName, (int) distance);
                        ping();
                        playerAlertStatus.put(playerName, true);
                    }
                } else {
                    playerAlertStatus.put(playerName, false);
                }
            }
        }
    }

    private void ping() {
        if (shouldPing.isToggled()) {
            mc.thePlayer.playSound("note.pling", 1.0f, 1.0f);
        }
    }
    public void informTeam(String playerName, int distance) {
        if (tellTheteam.isToggled()) {
            mc.thePlayer.sendChatMessage(playerName + " is " + distance + " blocks away from the bed!");
        }
    }
}