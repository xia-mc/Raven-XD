package keystrokesmod.module.impl.other.anticheats;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import keystrokesmod.module.impl.other.Anticheat;
import keystrokesmod.module.impl.other.LatencyAlerts;
import keystrokesmod.module.impl.other.anticheats.utils.alert.LogUtils;
import keystrokesmod.module.impl.other.anticheats.utils.world.LevelUtils;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.utility.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PlayerManager {
    public Map<UUID, Boolean> activeMap;  // 实时活动玩家(可被检查)列表

    public Map<UUID, TRPlayer> dataMap;  // 玩家数据列表

    public PlayerManager() {
        activeMap = new Object2ObjectOpenHashMap<>();
        dataMap = new Object2ObjectOpenHashMap<>();
    }

    public void update(@NotNull Minecraft client) {
        if (client.theWorld == null || client.thePlayer == null) return;
        activeMap.forEach((uuid, aBoolean) -> activeMap.replace(uuid, false));

        // 遍历活动玩家
        try {
            for (AbstractClientPlayer player : LevelUtils.getPlayers()) {
                final UUID uuid = player.getUniqueID();
                if (AntiBot.isBot(player)) {
                    activeMap.remove(uuid);
                    continue;
                }
                if (!Anticheat.getCheckForTeammates().isToggled() && Utils.isTeamMate(player)) {
                    activeMap.remove(uuid);
                    continue;
                }
                if (!activeMap.containsKey(uuid)) {
                    final TRPlayer trPlayer;
                    if (client.thePlayer.equals(player)) {
                        trPlayer = new TRSelf(client.thePlayer);
                    } else {
                        trPlayer = TRPlayer.create(player);
                    }
                    activeMap.put(uuid, true);
                    dataMap.put(uuid, trPlayer);
                }

                // 更新
                activeMap.replace(uuid, true);
                try {
                    final TRPlayer trPlayer = dataMap.get(uuid);
                    if (LatencyAlerts.isFreeze() || Utils.getTimer().timerSpeed != 1.0F || (Anticheat.getDisableInLobby().isToggled() && Utils.isLobby())) {
                        trPlayer.manager.disableTick = 30;
                    }
                    trPlayer.update(player);
                } catch (Exception e) {
                    LogUtils.custom(Arrays.toString(e.getStackTrace()));
                    LogUtils.LOGGER.error("遇到了异常，丢弃玩家 {} 数据。{}", player, e.getLocalizedMessage());
                    activeMap.remove(uuid);
                }
            }
        } catch (ConcurrentModificationException e) {
            LogUtils.custom(e.getLocalizedMessage());
        }
    }
}
