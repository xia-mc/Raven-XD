package keystrokesmod.module.impl.minigames;

import com.mojang.realmsclient.gui.ChatFormatting;
import keystrokesmod.module.Module;
import java.util.concurrent.CopyOnWriteArrayList;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.render.RenderUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class BedWars extends Module {
    private final String[] SERVERS = new String[]{"Hypixel", "Pika/Jartex"};
    private final ModeSetting serverMode;
    public static ButtonSetting whitelistOwnBed;
    private final ButtonSetting diamondArmor;
    private final ButtonSetting enderPearl;
    private final ButtonSetting fireball;
    private final ButtonSetting obsidian;
    private final ButtonSetting shouldPing;
    private static BlockPos spawnPos;
    private boolean check;
    public static boolean outsideSpawn = true;
    private final List<String> armoredPlayer = new CopyOnWriteArrayList<>();
    private final Map<String, String> lastHeldMap = new ConcurrentHashMap<>();
    private final Set<BlockPos> obsidianPos = new HashSet<>();
    private static final double MAX_SPAWN_DISTANCE_SQUARED = 800;
    private final int obsidianColor = new Color(0, 0, 0).getRGB();

    public BedWars() {
        super("Bed Wars", category.minigames);
        this.registerSetting(serverMode = new ModeSetting("Server", SERVERS, 0));
        this.registerSetting(whitelistOwnBed = new ButtonSetting("Whitelist own bed", true));
        this.registerSetting(diamondArmor = new ButtonSetting("Diamond armor", true));
        this.registerSetting(enderPearl = new ButtonSetting("Ender pearl", true));
        this.registerSetting(fireball = new ButtonSetting("Fireball", true));
        this.registerSetting(obsidian = new ButtonSetting("Obsidian", true));
        this.registerSetting(shouldPing = new ButtonSetting("Should ping", true));
    }

    public void onEnable() {
        armoredPlayer.clear();
        lastHeldMap.clear();
        obsidianPos.clear();
        check = false;
        outsideSpawn = true;
    }

    public void onDisable() {
        outsideSpawn = true;
    }

    @SubscribeEvent
    public void onBlock(BlockEvent.PlaceEvent e) {
        if (!Utils.nullCheck() || !obsidian.isToggled()) {
            return;
        }
        if (!(e.state.getBlock() instanceof BlockObsidian)) {
            return;
        }
        for (EnumFacing facing : EnumFacing.values()) {
            if (BlockUtils.getBlock(e.pos.offset(facing)) instanceof BlockBed) {
                obsidianPos.add(e.pos);
                Utils.sendMessage(e.player.getDisplayName().getFormattedText() + " &7placed &dObsidian");
                break;
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent e) {
        if (Utils.nullCheck()) {
            if (this.obsidianPos.isEmpty()) {
                return;
            }
            try {
                Iterator<BlockPos> iterator = this.obsidianPos.iterator();
                while (iterator.hasNext()) {
                    BlockPos blockPos = iterator.next();
                    if (!(mc.theWorld.getBlockState(blockPos).getBlock() instanceof BlockObsidian)) {
                        iterator.remove();
                        continue;
                    }
                    RenderUtils.renderBlock(blockPos, obsidianColor, false, true);
                }
            } catch (Exception ignored) {}
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent e) {
        if (!Utils.nullCheck() || e.entity == null) {
            return;
        }
        if (e.entity == mc.thePlayer) {
            armoredPlayer.clear();
            lastHeldMap.clear();
        }
    }

    public void onUpdate() {
        try {
            if (Utils.getBedwarsStatus() == 2) {
                if (diamondArmor.isToggled() || enderPearl.isToggled() || obsidian.isToggled()) {
                    for (EntityPlayer p : mc.theWorld.playerEntities) {
                        if (p == null) {
                            continue;
                        }
                        if (p == mc.thePlayer) {
                            continue;
                        }
                        if (AntiBot.isBot(p)) {
                            continue;
                        }
                        String name = p.getName();
                        ItemStack item = p.getHeldItem();
                        if (diamondArmor.isToggled()) {
                            ItemStack leggings = p.inventory.armorInventory[1];
                            if (!armoredPlayer.contains(name) && leggings != null && leggings.getItem() != null && leggings.getItem() == Items.diamond_leggings) {
                                armoredPlayer.add(name);
                                Utils.sendMessage("&eAlert: &r" + p.getDisplayName().getFormattedText() + " &7has purchased &bDiamond Armor");
                                ping();
                            }
                        }
                        if (item != null && !lastHeldMap.containsKey(name)) {
                            String itemType = getItemType(item);
                            if (itemType != null) {
                                lastHeldMap.put(name, itemType);
                                double distance = Math.round(mc.thePlayer.getDistanceToEntity(p));
                                handleAlert(itemType, p.getDisplayName().getFormattedText(), Utils.isWholeNumber(distance) ? (int) distance + "" : String.valueOf(distance));
                            }
                        } else if (lastHeldMap.containsKey(name)) {
                            String itemType = lastHeldMap.get(name);
                            if (!itemType.equals(getItemType(item))) {
                                lastHeldMap.remove(name);
                            }
                        }
                    }
                }
                if (whitelistOwnBed.isToggled()) {
                    if (check) {
                        spawnPos = mc.thePlayer.getPosition();
                        check = false;
                    }
                    if (spawnPos != null) outsideSpawn = mc.thePlayer.getDistanceSq(spawnPos) > MAX_SPAWN_DISTANCE_SQUARED;
                } else {
                    outsideSpawn = true;
                }
            }
        } catch (Exception e) {
            Utils.sendMessage(e.getLocalizedMessage());
        }
    }
    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent c) {
        if (!Utils.nullCheck()) {
            return;
        }
        String strippedMessage = Utils.stripColor(c.message.getUnformattedText());
        if (strippedMessage.startsWith(" ") && (strippedMessage.contains("Protect your bed and destroy the enemy beds.") || strippedMessage.contains("Goodluck with your BedWars Game"))) {
            Utils.sendMessage(this.getPrettyName() + ChatFormatting.GREEN + " game has started!");
            ping();
            check = true;
        }
    }

    private String getItemType(ItemStack item) {
        if (item == null || item.getItem() == null) {
            return null;
        }
        String unlocalizedName = item.getItem().getUnlocalizedName();
        if (item.getItem() instanceof ItemEnderPearl && enderPearl.isToggled()) {
            return "&7an §3Ender Pearl";
        } else if (unlocalizedName.contains("tile.obsidian") && obsidian.isToggled()) {
            return "§dObsidian";
        } else if (item.getItem() instanceof ItemFireball && fireball.isToggled()) {
            return "&7a §cFireball";
        }
        return null;
    }

    private void handleAlert(String itemType, String name, String info) {
        String alert = "&eAlert: &r" + name + " &7is holding " + itemType + " &7(" + "§d" + info + "m" + "&7)";
        Utils.sendMessage(alert);
        ping();
    }

    private void ping() {
        if (shouldPing.isToggled()) {
            mc.thePlayer.playSound("note.pling", 1.0f, 1.0f);
        }
    }
    public static BlockPos getSpawnPos() {
        return spawnPos;
    }

    @Override
    public String getInfo() {
        return SERVERS[(int) serverMode.getInput()];
    }
}
