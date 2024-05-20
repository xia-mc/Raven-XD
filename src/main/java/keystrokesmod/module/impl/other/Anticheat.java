package keystrokesmod.module.impl.other;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.PlayerData;
import keystrokesmod.utility.Utils;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.UUID;

public class Anticheat extends Module {
    private final DescriptionSetting description;
    private final SliderSetting interval;
    private final ButtonSetting enemyAdd;
    private final ButtonSetting ignoreTeammates;
    private final ButtonSetting atlasSuspect;
    private final ButtonSetting shouldPing;
    private final DescriptionSetting description2;
    private final ButtonSetting autoBlock;
    private final ButtonSetting noSlow;
    private final ButtonSetting scaffold;
    private final ButtonSetting legitScaffold;
    private final HashMap<UUID, HashMap<ButtonSetting, Long>> flags = new HashMap<>();
    private final HashMap<UUID, PlayerData> players = new HashMap<>();
    private long lastAlert;
    public Anticheat() {
        super("Anticheat", category.other);
        this.registerSetting(description = new DescriptionSetting("Tries to detect cheaters."));
        this.registerSetting(interval = new SliderSetting("Flag interval", 20.0, 0.0, 60.0, 1.0, " second"));
        this.registerSetting(enemyAdd = new ButtonSetting("Add cheaters as enemy", false));
        this.registerSetting(ignoreTeammates = new ButtonSetting("Ignore teammates", false));
        this.registerSetting(atlasSuspect = new ButtonSetting("Only atlas suspect", false));
        this.registerSetting(shouldPing = new ButtonSetting("Should ping", true));
        this.registerSetting(description2 = new DescriptionSetting("Detected cheats"));
        this.registerSetting(autoBlock = new ButtonSetting("Autoblock", true));
        this.registerSetting(noSlow = new ButtonSetting("NoSlow", true));
        this.registerSetting(scaffold = new ButtonSetting("Scaffold", true));
        this.registerSetting(legitScaffold = new ButtonSetting("Legit scaffold", true));
    }

    private void alert(final EntityPlayer entityPlayer, ButtonSetting mode) {
        if (Utils.isFriended(entityPlayer) || (ignoreTeammates.isToggled() && Utils.isTeamMate(entityPlayer))) {
            return;
        }
        if (atlasSuspect.isToggled()) {
            if (!entityPlayer.getName().equals("Suspect§r")) {
                return;
            }
        }
        else if (enemyAdd.isToggled()) {
            Utils.addEnemy(entityPlayer.getName());
        }
        final long currentTimeMillis = System.currentTimeMillis();
        if (interval.getInput() > 0.0) {
            HashMap<ButtonSetting, Long> hashMap = flags.get(entityPlayer.getUniqueID());
            if (hashMap == null) {
                hashMap = new HashMap<>();
            }
            else {
                final Long n = hashMap.get(mode);
                if (n != null && Utils.getDifference(n, currentTimeMillis) <= interval.getInput() * 1000.0) {
                    return;
                }
            }
            hashMap.put(mode, currentTimeMillis);
            flags.put(entityPlayer.getUniqueID(), hashMap);
        }
        final ChatComponentText chatComponentText = new ChatComponentText(Utils.formatColor("&7[&dR&7]&r " + entityPlayer.getDisplayName().getUnformattedText() + " &7detected for &d" + mode.getName()));
        final ChatStyle chatStyle = new ChatStyle();
        chatStyle.setChatClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/wdr " + entityPlayer.getName()));
        ((IChatComponent)chatComponentText).appendSibling(new ChatComponentText(Utils.formatColor(" §7[§cWDR§7]")).setChatStyle(chatStyle));
        mc.thePlayer.addChatMessage(chatComponentText);
        if (shouldPing.isToggled() && Utils.getDifference(lastAlert, currentTimeMillis) >= 1500L) {
            mc.thePlayer.playSound("note.pling", 1.0f, 1.0f);
            lastAlert = currentTimeMillis;
        }
    }

    public void onUpdate() {
        if (mc.isSingleplayer()) {
            return;
        }
        for (EntityPlayer entityPlayer : mc.theWorld.playerEntities) {
            if (entityPlayer == null) {
                continue;
            }
            if (entityPlayer == mc.thePlayer) {
                continue;
            }
            PlayerData data = players.get(entityPlayer.getUniqueID());
            if (data == null) {
                data = new PlayerData();
            }
            data.update(entityPlayer);
            this.performCheck(entityPlayer, data);
            data.updateSneak(entityPlayer);
            players.put(entityPlayer.getUniqueID(), data);
        }
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinWorldEvent e) {
        if (e.entity == mc.thePlayer) {
            players.clear();
            flags.clear();
        }
    }

    public void onDisable() {
        players.clear();
        flags.clear();
        lastAlert = 0L;
    }

    private void performCheck(EntityPlayer entityPlayer, PlayerData ej) {
        if (autoBlock.isToggled() && ej.d >= 10) {
            alert(entityPlayer, autoBlock);
            return;
        }
        if (legitScaffold.isToggled() && ej.h >= 3) {
            alert(entityPlayer, legitScaffold);
            return;
        }
        if (noSlow.isToggled() && ej.i >= 11 && ej.a >= 0.08) {
            alert(entityPlayer, noSlow);
            return;
        }
        if (scaffold.isToggled() && entityPlayer.isSwingInProgress && entityPlayer.rotationPitch >= 70.0f && entityPlayer.getHeldItem() != null && entityPlayer.getHeldItem().getItem() instanceof ItemBlock && ej.c >= 20 && entityPlayer.ticksExisted - ej.f >= 30 && entityPlayer.ticksExisted - ej.b >= 20) {
            boolean b = true;
            BlockPos blockPos = entityPlayer.getPosition().down(2);
            for (int i = 0; i < 4; ++i) {
                if (!(BlockUtils.getBlock(blockPos) instanceof BlockAir)) {
                    b = false;
                    break;
                }
                blockPos = blockPos.down();
            }
            if (b) {
                alert(entityPlayer, scaffold);
            }
        }
    }
}
