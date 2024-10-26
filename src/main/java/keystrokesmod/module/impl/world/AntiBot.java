package keystrokesmod.module.impl.world;

import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import keystrokesmod.Raven;
import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.render.Freecam;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.Utils;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class AntiBot extends Module {
    private static final Map<EntityPlayer, Long> entities = new Object2LongOpenHashMap<>();
    private static final Set<EntityPlayer> filteredBot = new ObjectOpenHashSet<>();
    private static ButtonSetting entitySpawnDelay;
    private static SliderSetting delay;
    private static ButtonSetting pitSpawn;
    private static ButtonSetting tablist;
    private static ButtonSetting matrix;
    private static ButtonSetting cancelBotHit;
    private static ButtonSetting debug;
    private static ButtonSetting whitelistGolem;
    private static ButtonSetting whitelistSilverfish;
    private static ButtonSetting whitelistChicken;

    private final Map<String, EntityPlayer> lastPlayers = new Object2ObjectOpenHashMap<>();

    public AntiBot() {
        super("AntiBot", Module.category.world, 0);
        this.registerSetting(entitySpawnDelay = new ButtonSetting("Entity spawn delay", false));
        this.registerSetting(delay = new SliderSetting("Delay", 7.0, 0.5, 15.0, 0.5, " second", entitySpawnDelay::isToggled));
        this.registerSetting(tablist = new ButtonSetting("Tab list", false));
        this.registerSetting(matrix = new ButtonSetting("MatrixTest", false));
        this.registerSetting(debug = new ButtonSetting("Debug", false, matrix::isToggled));
        this.registerSetting(pitSpawn = new ButtonSetting("Pit spawn", false));
        this.registerSetting(cancelBotHit = new ButtonSetting("Cancel bot hit", false));
        this.registerSetting(whitelistGolem = new ButtonSetting("Whitelist golems", false));
        this.registerSetting(whitelistSilverfish = new ButtonSetting("Whitelist silverfishes", false));
        this.registerSetting(whitelistChicken = new ButtonSetting("Whitelist chickens", false));
    }

    @SubscribeEvent
    public void c(final EntityJoinWorldEvent entityJoinWorldEvent) {
        if (entitySpawnDelay.isToggled() && entityJoinWorldEvent.entity instanceof EntityPlayer && entityJoinWorldEvent.entity != mc.thePlayer) {
            entities.put((EntityPlayer) entityJoinWorldEvent.entity, System.currentTimeMillis());
        }
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent event) {
        if (cancelBotHit.isToggled() && event.getPacket() instanceof C02PacketUseEntity) {
            C02PacketUseEntity packet = (C02PacketUseEntity) event.getPacket();
            if (packet.getAction() == C02PacketUseEntity.Action.ATTACK) {
                if (isBot(packet.getEntityFromWorld(mc.theWorld))) {
                    event.setCanceled(true);
                }
            }
        }
    }

    public void onUpdate() {
        if (entitySpawnDelay.isToggled() && !entities.isEmpty()) {
            entities.values().removeIf(n -> n < System.currentTimeMillis() - delay.getInput());
        }

        lastPlayers.clear();
        for (EntityPlayer p : mc.theWorld.playerEntities) {
            if (filteredBot.contains(p)) continue;

            String name = p.getName();
            if (lastPlayers.containsKey(name)) {
                if (debug.isToggled()) Utils.sendMessage("Filtered bot: " + p.getName() + ".");

                EntityPlayer exists = lastPlayers.get(name);
                Vec3 thePlayer = new Vec3(mc.thePlayer);
                double existsDistance = thePlayer.distanceTo(exists);
                double curDistance = thePlayer.distanceTo(p);

                if (existsDistance > curDistance) {
                    filteredBot.add(p);
                } else {
                    filteredBot.add(exists);
                }
                break;
            }
            lastPlayers.put(name, p);
        }
    }

    public void onDisable() {
        entities.clear();
        filteredBot.clear();
        lastPlayers.clear();
    }

    public static boolean isBot(Entity entity) {
        if (!ModuleManager.antiBot.isEnabled()) {
            return false;
        }
        if (Freecam.freeEntity != null && Freecam.freeEntity == entity) {
            return true;
        }
        if (whitelistGolem.isToggled() && entity instanceof EntityIronGolem) {
            return false;
        }
        if (whitelistSilverfish.isToggled() && entity instanceof EntitySilverfish) {
            return false;
        }
        if (whitelistChicken.isToggled() && entity instanceof EntityChicken) {
            return false;
        }
        if (!(entity instanceof EntityPlayer)) {
            return true;
        }
        final EntityPlayer entityPlayer = (EntityPlayer) entity;
        if (entitySpawnDelay.isToggled() && !entities.isEmpty() && entities.containsKey(entityPlayer)) {
            return true;
        }
        if (matrix.isToggled() && filteredBot.contains(entityPlayer)) {
            return true;
        }
        if (entityPlayer.isDead) {
            return true;
        }
        if (entityPlayer.getName().isEmpty()) {
            return true;
        }
        if (!getTablist().contains(entityPlayer.getName()) && tablist.isToggled()) {
            return true;
        }
        if (entityPlayer.getHealth() != 20.0f && entityPlayer.getName().startsWith("§c")) {
            return true;
        }
        if (pitSpawn.isToggled() && entityPlayer.posY >= 114 && entityPlayer.posY <= 130 && entityPlayer.getDistance(0, 114, 0) <= 25) {
            if (Utils.isHypixel()) {
                List<String> sidebarLines = Utils.getSidebarLines();
                if (!sidebarLines.isEmpty() && Utils.stripColor(sidebarLines.get(0)).contains("THE HYPIXEL PIT")) {
                    return true;
                }
            }
        }
        if (entityPlayer.maxHurtTime == 0) {
            if (entityPlayer.getHealth() == 20.0f) {
                String unformattedText = entityPlayer.getDisplayName().getUnformattedText();
                if (unformattedText.length() == 10 && unformattedText.charAt(0) != '§') {
                    return true;
                }
                if (unformattedText.length() == 12 && entityPlayer.isPlayerSleeping() && unformattedText.charAt(0) == '§') {
                    return true;
                }
                if (unformattedText.length() >= 7 && unformattedText.charAt(2) == '[' && unformattedText.charAt(3) == 'N' && unformattedText.charAt(6) == ']') {
                    return true;
                }
                if (entityPlayer.getName().contains(" ")) {
                    return true;
                }
            } else if (entityPlayer.isInvisible()) {
                String unformattedText = entityPlayer.getDisplayName().getUnformattedText();
                return unformattedText.length() >= 3 && unformattedText.charAt(0) == '§' && unformattedText.charAt(1) == 'c';
            }
        }
        return false;
    }

    private static @NotNull List<String> getTablist() {
        return Raven.mc.getNetHandler().getPlayerInfoMap().parallelStream()
                .map(NetworkPlayerInfo::getGameProfile)
                .filter(profile -> profile.getId() != Raven.mc.thePlayer.getUniqueID())
                .map(GameProfile::getName)
                .collect(Collectors.toList());
    }
}
