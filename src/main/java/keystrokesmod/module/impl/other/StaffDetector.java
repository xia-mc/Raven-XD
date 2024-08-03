package keystrokesmod.module.impl.other;

import keystrokesmod.Raven;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C01PacketChatMessage;

import java.io.*;
import java.util.*;

public class StaffDetector extends Module {
    public static final String[] STAFFLISTS = new String[]{"HypixelDefault", "BlocksMCDefault", "GamsterDefault", "GommeHDDefault", "PikaDefault", "SyuuDefault", "StardixDefault"};
    public static final List<Set<String>> STAFFS = new ArrayList<>();
    public static final Set<String> hasFlagged = new HashSet<>();

    private final ModeSetting mode = new ModeSetting("Mode", STAFFLISTS, 0);
    private final ButtonSetting autoLobby = new ButtonSetting("Auto lobby", false);

    public StaffDetector() {
        super("StaffDetector", category.other);
        this.registerSetting(mode, autoLobby);

        for (String s : STAFFLISTS) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            Objects.requireNonNull(Raven.class.getResourceAsStream("/assets/keystrokesmod/stafflists/" + s + ".txt"))))) {
                Set<String> lines = new HashSet<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }

                STAFFS.add(lines);
            } catch (NullPointerException | IOException ignored) {
            }
        }
    }

    @Override
    public void onUpdate() {
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            final String name = player.getName();
            if (hasFlagged.contains(name)) continue;

            if (STAFFS.get((int) mode.getInput()).contains(name)) {
                hasFlagged.add(name);

                Utils.sendMessage("§c§lStaff Detected: §r" + name);
                if (autoLobby.isToggled()) {
                    PacketUtils.sendPacket(new C01PacketChatMessage("/lobby"));
                    Utils.sendMessage("Return to lobby...");
                }
            }
        }
    }
}
