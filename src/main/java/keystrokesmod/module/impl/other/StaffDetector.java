package keystrokesmod.module.impl.other;

import keystrokesmod.Raven;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.client.Notifications;
import keystrokesmod.module.impl.client.Notifications.NotificationTypes;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class StaffDetector extends Module {
    private static final String[] stafflists = {
            "Hypixel", "BlocksMC", "Gamster", "GommeHD", "Pika", "Syuu", "Stardix", "MinemenClub", "MushMC",
            "Twerion", "BedwarsPractice", "QuickMacro", "Heypixel", "HylexMC", "Jartex", "Mineland"
    };
    private static final String[] message = {
            "Chat", "Notification", "Both", "None"
    };
    private static final String[] detectionModes = {
            "TabList", "World", "Both"
    };
    private static final List<Set<String>> staff = new ArrayList<>();
    private static final Set<String> flagged = Collections.synchronizedSet(new HashSet<>());
    private static final String stafflistPath = "/assets/keystrokesmod/stafflists/";
    private static boolean isStaffListLoaded = false;

    private final ModeSetting stafflistMode = new ModeSetting("Stafflist", stafflists, 0);
    private final ModeSetting detectionMode = new ModeSetting("Detection", detectionModes, 2);
    private final ModeSetting messageMode = new ModeSetting("Message Mode", message, 0);
    private final ButtonSetting autoLobby = new ButtonSetting("Auto lobby", false);
    private final ButtonSetting alarm = new ButtonSetting("Alarm", false);

    public StaffDetector() {
        super("StaffDetector", category.other);
        this.registerSetting(stafflistMode, messageMode, detectionMode, autoLobby, alarm);
        loadStaffLists();
        MinecraftForge.EVENT_BUS.register(this);
    }

    private synchronized void loadStaffLists() {
        if (isStaffListLoaded) return;

        for (String listName : stafflists) {
            Set<String> staffNames = new HashSet<>();
            String filePath = stafflistPath + listName + ".txt";

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    Objects.requireNonNull(Raven.class.getResourceAsStream(filePath))))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    staffNames.add(line.trim());
                }
                staff.add(staffNames);
            } catch (IOException | NullPointerException e) {
                Notifications.sendNotification(NotificationTypes.ERROR, "Error loading staff list for " + listName + ": " + e.getMessage());
            }
        }
        isStaffListLoaded = true;
    }

    @Override
    public void onUpdate() {
        Set<String> currentStaffList = staff.get((int) stafflistMode.getInput());

        int detectionModeInput = (int) detectionMode.getInput();
        switch (detectionModeInput) {
            case 0:
                tablistStaff(currentStaffList);
                break;
            case 1:
                worldStaff(currentStaffList);
                break;
            case 2:
                tablistStaff(currentStaffList);
                worldStaff(currentStaffList);
                break;
        }
    }

    private void tablistStaff(Set<String> currentStaffList) {
        for (NetworkPlayerInfo playerInfo : mc.getNetHandler().getPlayerInfoMap()) {
            String cleanedPlayerName = playerInfo.getGameProfile().getName().replaceAll("§[0-9a-fk-or]", "").toLowerCase();

            if (!flagged.contains(cleanedPlayerName) && currentStaffList.stream().map(String::toLowerCase).anyMatch(cleanedPlayerName::equals)) {
                staffDetection(cleanedPlayerName);
            }
        }
    }

    private void worldStaff(Set<String> currentStaffList) {
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            String cleanedPlayerName = player.getName().replaceAll("§[0-9a-fk-or]", "").toLowerCase();

            if (!flagged.contains(cleanedPlayerName) && currentStaffList.stream().map(String::toLowerCase).anyMatch(cleanedPlayerName::equals)) {
                staffDetection(cleanedPlayerName);
            }
        }
    }


    private void staffDetection(String playerName) {
        flagged.add(playerName);

        int messageModeInput = (int) messageMode.getInput();
        switch (messageModeInput) {
            case 0:
                Utils.sendMessage("§c§lStaff Detected: §r" + playerName);
                break;
            case 1:
                Notifications.sendNotification(NotificationTypes.WARN, "Staff Detected: " + playerName);
                break;
            case 2:
                Utils.sendMessage("§c§lStaff Detected: §r" + playerName);
                Notifications.sendNotification(NotificationTypes.WARN, "Staff Detected: " + playerName);
                break;
            case 3:
                break;
        }

        if (autoLobby.isToggled()) {
            PacketUtils.sendPacket(new C01PacketChatMessage("/lobby"));
            lobbyMessage();
        }
        if (alarm.isToggled()) {
            mc.thePlayer.playSound("keystrokesmod:alarm", 1, 1);
        }
    }

    private void lobbyMessage() {
        int messageModeInput = (int) messageMode.getInput();
        switch (messageModeInput) {
            case 0:
                Utils.sendMessage("Returning to lobby...");
                break;
            case 1:
                Notifications.sendNotification(NotificationTypes.INFO, "Returning to lobby...");
                break;
            case 2:
                Notifications.sendNotification(NotificationTypes.INFO, "Returning to lobby...");
                Utils.sendMessage("Returning to lobby...");
                break;
            case 3:
                break;
        }
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        flagged.clear();
    }
}