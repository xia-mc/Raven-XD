package keystrokesmod.utility;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.realmsclient.gui.ChatFormatting;
import keystrokesmod.event.ClickEvent;
import keystrokesmod.mixins.impl.client.GuiScreenAccessor;
import keystrokesmod.module.impl.other.SlotHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.MouseEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.client.NyaProxy;
import keystrokesmod.module.impl.client.Settings;
import keystrokesmod.module.impl.combat.HitSelect;
import keystrokesmod.module.impl.minigames.DuelsStats;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.clicks.CPSCalculator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockSign;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.potion.Potion;
import net.minecraft.scoreboard.*;
import net.minecraft.util.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.nio.ByteBuffer;
import java.util.stream.IntStream;

public class Utils {
    private static final Random rand = new Random();
    public static final Minecraft mc = Minecraft.getMinecraft();
    public static HashSet<String> friends = new HashSet<>();
    public static HashSet<String> enemies = new HashSet<>();
    public static final Logger log = LogManager.getLogger();

    public static boolean addEnemy(String name) {
        if (enemies.add(name.toLowerCase())) {
            Utils.sendMessage("&7Added &cenemy&7: &b" + name);
            return true;
        }
        return false;
    }

    public static String getServerName() {
        return DuelsStats.nick.isEmpty() ? mc.thePlayer.getName() : DuelsStats.nick;
    }

    public static boolean overVoid(double posX, double posY, double posZ) {
        for (int i = (int) posY; i > -1; i--) {
            if (!(mc.theWorld.getBlockState(new BlockPos(posX, i, posZ)).getBlock() instanceof BlockAir)) {
                return false;
            }
        }
        return true;
    }

    public static boolean overVoid() {
        return overVoid(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
    }

    public static List<NetworkPlayerInfo> getTablist() {
        final ArrayList<NetworkPlayerInfo> list = new ArrayList<>(mc.getNetHandler().getPlayerInfoMap());
        removeDuplicates(list);
        list.remove(mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID()));
        return list;
    }

    public static double getFallDistance(Entity entity) {
        double fallDist = -1;
        Vec3 pos = new Vec3(entity.posX, entity.posY, entity.posZ);
        int y = (int) Math.floor(pos.yCoord);
        if (pos.yCoord % 1 == 0) y--;
        for (int i = y; i > -1; i--) {
            Block block = BlockUtils.getBlock(new BlockPos((int) Math.floor(pos.xCoord), i, (int) Math.floor(pos.zCoord)));
            if (!(block instanceof BlockAir) && !(block instanceof BlockSign)) {
                fallDist = y - i;
                break;
            }
        }
        return fallDist;
    }

    public static void removeDuplicates(final ArrayList list) {
        final HashSet set = new HashSet(list);
        list.clear();
        list.addAll(set);
    }

    public static boolean removeFriend(String name) {
        if (friends.remove(name.toLowerCase())) {
            Utils.sendMessage("&7Removed &afriend&7: &b" + name);
            return true;
        }
        return false;
    }

    public static boolean addFriend(String name) {
        if (friends.add(name.toLowerCase())) {
            Utils.sendMessage("&7Added &afriend&7: &b" + name);
            return true;
        }
        return false;
    }

    public static boolean isWholeNumber(double num) {
        return num == Math.floor(num);
    }

    public static int randomizeInt(double min, double max) {
        return (int) randomizeDouble(min, max);
    }

    public static double randomizeDouble(double min, double max) {
        return Math.random() * (max - min) + min;
    }

    public static boolean inFov(float fov, BlockPos blockPos) {
        return inFov(fov, blockPos.getX(), blockPos.getZ());
    }

    public static boolean inFov(float fov, Entity entity) {
        return inFov(fov, entity.posX, entity.posZ);
    }

    public static boolean inFov(float fov, final double n2, final double n3) {
        fov *= 0.5F;
        final double fovToPoint = getFov(n2, n3);
        if (fovToPoint > 0.0) {
            return fovToPoint < fov;
        } else return fovToPoint > -fov;
    }

    public static @Range(from = -180, to = 180) double getFov(final double posX, final double posZ) {
        return MathHelper.wrapAngleTo180_double((mc.thePlayer.rotationYaw - RotationUtils.angle(posX, posZ)) % 360.0f);
    }

    public static void sendMessage(String txt) {
        if (nullCheck()) {
            String m = formatColor("&7[&dR&7]&r " + txt);
            mc.thePlayer.addChatMessage(new ChatComponentText(m));
        }
    }

    public static void sendDebugMessage(String message) {
        if (nullCheck()) {
            mc.thePlayer.addChatMessage(new ChatComponentText("§7[§dR§7]§r " + message));
        }
    }

    public static void attackEntity(Entity e, boolean clientSwing) {
        boolean attack = HitSelect.canAttack(e);
        if (clientSwing) {
            if (attack || HitSelect.canSwing()) mc.thePlayer.swingItem();
        } else {
            if (attack || HitSelect.canSwing()) mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
        }
        if (attack) mc.playerController.attackEntity(mc.thePlayer, e);
    }

    public static void attackEntityNoSwing(Entity e) {
        if (HitSelect.canAttack(e)) mc.playerController.attackEntity(mc.thePlayer, e);
    }

    public static void sendRawMessage(String txt) {
        if (nullCheck()) {
            mc.thePlayer.addChatMessage(new ChatComponentText(formatColor(txt)));
        }
    }

    public static float getCompleteHealth(EntityLivingBase entity) {
        return entity.getHealth() + entity.getAbsorptionAmount();
    }

    public static String getHealthStr(EntityLivingBase entity) {
        float completeHealth = getCompleteHealth(entity);
        return getColorForHealth(entity.getHealth() / entity.getMaxHealth(), completeHealth);
    }

    public static int limit(int current, int min, int max) {
        return Math.min(Math.max(current, min), max);
    }

    public static int getTool(Block block) {
        float n = 1.0f;
        int n2 = -1;
        for (int i = 0; i < InventoryPlayer.getHotbarSize(); ++i) {
            final ItemStack getStackInSlot = mc.thePlayer.inventory.getStackInSlot(i);
            if (getStackInSlot != null) {
                final float a = getEfficiency(getStackInSlot, block);
                if (a > n) {
                    n = a;
                    n2 = i;
                }
            }
        }
        return n2;
    }

    public static int getWeapon() {
        int weaponIndex = -1;
        double highestPriority = 0.0;

        for (int i = 0; i < InventoryPlayer.getHotbarSize(); ++i) {
            ItemStack item = mc.thePlayer.inventory.getStackInSlot(i);
            if (item == null) continue;

            double priority = 0.0;
            double typePriority = 0.0;

            if (Settings.weaponSword.isToggled() && item.getItem() instanceof ItemSword) {
                priority = getDamage(item);
                typePriority = 0.4;
            } else if (Settings.weaponAxe.isToggled() && item.getItem() instanceof ItemAxe) {
                priority = getDamage(item);
                typePriority = 0.3;
            } else if (Settings.weaponStick.isToggled() && item.getItem() == Items.stick) {
                priority = getDamage(item);
                typePriority = 0.2;
            } else if (Settings.weaponRod.isToggled() && item.getItem() == Items.fishing_rod) {
                priority = getDamage(item);
                typePriority = 0.1;
            }
            // The typePriority is added to ensure that if two weapons have the same damage,
            // the weapon with the higher typePriority (based on the predefined weapon order:
            // sword > axe > stick > rod) is selected. This helps in making a more informed
            // choice when weapons have identical damage values.
            priority += typePriority;

            if (priority > highestPriority) {
                highestPriority = priority;
                weaponIndex = i;
            }
        }

        return weaponIndex;
    }

    public static float getEfficiency(final ItemStack itemStack, final Block block) {
        float getStrVsBlock = itemStack.getStrVsBlock(block);
        if (getStrVsBlock > 1.0f) {
            final int getEnchantmentLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, itemStack);
            if (getEnchantmentLevel > 0) {
                getStrVsBlock += getEnchantmentLevel * getEnchantmentLevel + 1;
            }
        }
        return getStrVsBlock;
    }

    public static boolean isEnemy(EntityPlayer entityPlayer) {
        return !enemies.isEmpty() && enemies.contains(entityPlayer.getName().toLowerCase());
    }

    public static boolean isEnemy(String name) {
        return !enemies.isEmpty() && enemies.contains(name.toLowerCase());
    }

    public static String getColorForHealth(double n, double n2) {
        double health = rnd(n2, 1);
        return ((n < 0.3) ? "§c" : ((n < 0.5) ? "§6" : ((n < 0.7) ? "§e" : "§a"))) + (isWholeNumber(health) ? (int) health + "" : health);
    }

    public static int getColorForHealth(double health) {
        return ((health < 0.3) ? -43691 : ((health < 0.5) ? -22016 : ((health < 0.7) ? -171 : -11141291)));
    }

    public static String formatColor(String txt) {
        return txt.replaceAll("&", "§");
    }

    public static void correctValue(SliderSetting c, SliderSetting d) {
        if (c.getInput() > d.getInput()) {
            double p = c.getInput();
            c.setValue(d.getInput());
            d.setValue(p);
        }
    }

    public static String generateRandomString(final int n) {
        final char[] array = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();
        final StringBuilder sb = new StringBuilder();
        IntStream.range(0, n).forEach(p2 -> sb.append(array[rand.nextInt(array.length)]));
        return sb.toString();
    }

    public static boolean isFriended(EntityPlayer entityPlayer) {
        return !friends.isEmpty() && friends.contains(entityPlayer.getName().toLowerCase());
    }

    public static boolean isFriended(String name) {
        return !friends.isEmpty() && friends.contains(name.toLowerCase());
    }

    public static double getRandomValue(SliderSetting a, SliderSetting b, Random r) {
        return a.getInput() == b.getInput() ? a.getInput() : a.getInput() + r.nextDouble() * (b.getInput() - a.getInput());
    }

    public static boolean nullCheck() {
        return mc.thePlayer != null && mc.theWorld != null;
    }

    public static boolean isHypixel() {
        return !mc.isSingleplayer() && mc.getCurrentServerData() != null
                && (mc.getCurrentServerData().serverIP.contains("hypixel.net")
                || NyaProxy.isNyaProxy(mc.getCurrentServerData().serverIP).isPresent());
    }

    public static boolean isCraftiGames() {
        return !mc.isSingleplayer() && mc.getCurrentServerData() != null
                && ((mc.getCurrentServerData().serverIP.contains("pika-network.net") || mc.getCurrentServerData().serverIP.contains("pikasys.net") || mc.getCurrentServerData().serverIP.contains("pika.host") || mc.getCurrentServerData().serverIP.contains("jartexsys.net") || mc.getCurrentServerData().serverIP.contains("jartexnetwork.com"))
                || NyaProxy.isNyaProxy(mc.getCurrentServerData().serverIP).isPresent());
    }

    public static net.minecraft.util.Timer getTimer() {
        return ObfuscationReflectionHelper.getPrivateValue(Minecraft.class, Minecraft.getMinecraft(), "timer", "field_71428_T");
    }

    public static String getHitsToKill(final EntityPlayer entityPlayer, final ItemStack itemStack) {
        final int n = (int) Math.ceil(ap(entityPlayer, itemStack));
        return "§" + ((n <= 1) ? "c" : ((n <= 3) ? "6" : ((n <= 5) ? "e" : "a"))) + n;
    }

    public static double ap(final EntityPlayer entityPlayer, final ItemStack itemStack) {
        double n = 1.0;
        if (itemStack != null && (itemStack.getItem() instanceof ItemSword || itemStack.getItem() instanceof ItemAxe)) {
            n += getDamage(itemStack);
        }
        double n2 = 0.0;
        double n3 = 0.0;
        for (int i = 0; i < 4; ++i) {
            final ItemStack armorItemInSlot = entityPlayer.inventory.armorItemInSlot(i);
            if (armorItemInSlot != null) {
                if (armorItemInSlot.getItem() instanceof ItemArmor) {
                    n2 += ((ItemArmor) armorItemInSlot.getItem()).damageReduceAmount * 0.04;
                    final int getEnchantmentLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, armorItemInSlot);
                    if (getEnchantmentLevel != 0) {
                        n3 += Math.floor(0.75 * (6 + getEnchantmentLevel * getEnchantmentLevel) / 3.0);
                    }
                }
            }
        }
        return rnd((double) getCompleteHealth(entityPlayer) / (n * (1.0 - (n2 + 0.04 * Math.min(Math.ceil(Math.min(n3, 25.0) * 0.75), 20.0) * (1.0 - n2)))), 1);
    }

    public static float n() {
        return ae(mc.thePlayer.rotationYaw, mc.thePlayer.movementInput.moveForward, mc.thePlayer.movementInput.moveStrafe);
    }

    public static String extractFileName(String name) {
        int firstIndex = name.indexOf("_");
        int lastIndex = name.lastIndexOf("_");

        if (firstIndex != -1 && lastIndex != -1 && lastIndex > firstIndex) {
            return name.substring(firstIndex + 1, lastIndex);
        } else {
            return name;
        }
    }

    public static int merge(int n, int n2) {
        return (n & 0xFFFFFF) | n2 << 24;
    }

    public static int clamp(int n) {
        if (n > 255) {
            return 255;
        }
        if (n < 4) {
            return 4;
        }
        return n;
    }

    public static boolean isTeamMate(Entity entity) {
        try {
            Entity teamMate = entity;
            if (mc.thePlayer.isOnSameTeam((EntityLivingBase) entity) || mc.thePlayer.getDisplayName().getUnformattedText().startsWith(teamMate.getDisplayName().getUnformattedText().substring(0, 2))) {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public static void setSpeed(double n) {
        if (n == 0.0) {
            mc.thePlayer.motionZ = 0.0;
            mc.thePlayer.motionX = 0.0;
            return;
        }
        float n3 = n();
        mc.thePlayer.motionX = -Math.sin(n3) * n;
        mc.thePlayer.motionZ = Math.cos(n3) * n;
    }

    public static void resetTimer() {
        try {
            getTimer().timerSpeed = 1.0F;
        } catch (NullPointerException var1) {
        }
    }

    public static boolean inInventory() {
        if (!Utils.nullCheck()) {
            return false;
        }
        return (mc.currentScreen != null) && (mc.thePlayer.inventoryContainer != null) && (mc.thePlayer.inventoryContainer instanceof ContainerPlayer) && (mc.currentScreen instanceof GuiInventory);
    }

    public static int getBedwarsStatus() {
        if (!Utils.nullCheck()) {
            return -1;
        }
        final Scoreboard scoreboard = mc.theWorld.getScoreboard();
        if (scoreboard == null) {
            return -1;
        }
        final ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) {
            return -1;
        }
        String displayName = stripString(objective.getDisplayName());
        if (!displayName.contains("BED WARS") && !displayName.contains("BedWars")) {
            return -1;
        }
        for (String line : getSidebarLines()) {
            line = stripString(line).trim();
            String[] parts = line.split("  ");
            if (parts.length > 1 && parts[1].equalsIgnoreCase("L")) {
                return 0;
            }
            if (line.equalsIgnoreCase("Waiting...") || line.startsWith("Starting")) {
                return 1;
            }
            if (line.startsWith("R Red:") || line.startsWith("B Blue:") || line.startsWith("Red") || line.startsWith("Blue")) {
                return 2;
            }
        }
        return -1;
    }

    public static String stripString(final String s) {
        final char[] nonValidatedString = StringUtils.stripControlCodes(s).toCharArray();
        final StringBuilder validated = new StringBuilder();
        for (final char c : nonValidatedString) {
            if (c < '' && c > '') {
                validated.append(c);
            }
        }
        return validated.toString();
    }

    public static List<String> getSidebarLines() {
        final List<String> lines = new ArrayList<>();
        if (mc.theWorld == null) {
            return lines;
        }
        final Scoreboard scoreboard = mc.theWorld.getScoreboard();
        if (scoreboard == null) {
            return lines;
        }
        final ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
        if (objective == null) {
            return lines;
        }
        Collection<Score> scores = scoreboard.getSortedScores(objective);
        final List<Score> list = new ArrayList<>();
        for (final Score input : scores) {
            if (input != null && input.getPlayerName() != null && !input.getPlayerName().startsWith("#")) {
                list.add(input);
            }
        }
        if (list.size() > 15) {
            scores = new ArrayList<>(Lists.newArrayList(Iterables.skip(list, list.size() - 15)));
        } else {
            scores = list;
        }
        int index = 0;
        for (final Score score : scores) {
            ++index;
            final ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
            lines.add(ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()));
            if (index == scores.size()) {
                lines.add(objective.getDisplayName());
            }
        }
        return lines;
    }

    public static Random getRandom() {
        return rand;
    }

    public static boolean isMoving() {
        if (!Utils.nullCheck()) return false;
        return mc.thePlayer.moveForward != 0.0F || mc.thePlayer.moveStrafing != 0.0F;
    }

    /**
     * it's UNFAIR if pc == true!
     * TODO we need a rotation system.
     */
    public static void aim(Entity en, float ps, boolean pc) {
        if (en != null) {
            float[] t = gr(en);
            if (t != null) {
                float y = t[0];
                float p = t[1] + 4.0F + ps;
                if (pc) {
                    mc.getNetHandler().addToSendQueue(new C05PacketPlayerLook(y, p, mc.thePlayer.onGround));
                } else {
                    mc.thePlayer.rotationYaw = y;
                    mc.thePlayer.rotationPitch = p;
                }
            }

        }
    }

    public static float[] gr(Entity q) {
        if (q == null) {
            return null;
        } else {
            double diffX = q.posX - mc.thePlayer.posX;
            double diffY;
            if (q instanceof EntityLivingBase) {
                EntityLivingBase en = (EntityLivingBase) q;
                diffY = en.posY + (double) en.getEyeHeight() * 0.9D - (mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight());
            } else {
                diffY = (q.getEntityBoundingBox().minY + q.getEntityBoundingBox().maxY) / 2.0D - (mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight());
            }

            double diffZ = q.posZ - mc.thePlayer.posZ;
            double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
            float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0D / 3.141592653589793D) - 90.0F;
            float pitch = (float) (-(Math.atan2(diffY, dist) * 180.0D / 3.141592653589793D));
            return new float[]{mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float(yaw - mc.thePlayer.rotationYaw), mc.thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float(pitch - mc.thePlayer.rotationPitch)};
        }
    }

    public static double n(Entity en) {
        return ((double) (mc.thePlayer.rotationYaw - getYaw(en)) % 360.0D + 540.0D) % 360.0D - 180.0D;
    }

    public static float getYaw(Entity ent) {
        double x = ent.posX - mc.thePlayer.posX;
        double z = ent.posZ - mc.thePlayer.posZ;
        double yaw = Math.atan2(x, z) * 57.29577951308232;
        return (float) (yaw * -1.0D);
    }

    public static void ss(double s, boolean m) {
        if (!m || isMoving()) {
            mc.thePlayer.motionX = -Math.sin(gd()) * s;
            mc.thePlayer.motionZ = Math.cos(gd()) * s;
        }
    }

    public static boolean keysDown() {
        return Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()) || Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode()) || Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode()) || Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode());
    }

    public static boolean jumpDown() {
        return Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode());
    }

    public static float gd() {
        float yw = mc.thePlayer.rotationYaw;
        if (mc.thePlayer.moveForward < 0.0F) {
            yw += 180.0F;
        }

        float f;
        if (mc.thePlayer.moveForward < 0.0F) {
            f = -0.5F;
        } else if (mc.thePlayer.moveForward > 0.0F) {
            f = 0.5F;
        } else {
            f = 1.0F;
        }

        if (mc.thePlayer.moveStrafing > 0.0F) {
            yw -= 90.0F * f;
        }

        if (mc.thePlayer.moveStrafing < 0.0F) {
            yw += 90.0F * f;
        }

        yw *= 0.017453292F;
        return yw;
    }

    public static float ae(float n, float n2, float n3) {
        float n4 = 1.0f;
        if (n2 < 0.0f) {
            n += 180.0f;
            n4 = -0.5f;
        } else if (n2 > 0.0f) {
            n4 = 0.5f;
        }
        if (n3 > 0.0f) {
            n -= 90.0f * n4;
        } else if (n3 < 0.0f) {
            n += 90.0f * n4;
        }
        return n * 0.017453292f;
    }

    public static double getHorizontalSpeed() {
        return getHorizontalSpeed(mc.thePlayer);
    }

    public static double getHorizontalSpeed(Entity entity) {
        return Math.sqrt(entity.motionX * entity.motionX + entity.motionZ * entity.motionZ);
    }

    public static boolean onEdge() {
        return onEdge(mc.thePlayer);
    }

    public static boolean onEdge(Entity entity) {
        return mc.theWorld.getCollidingBoundingBoxes(entity, entity.getEntityBoundingBox().offset(entity.motionX / 3.0D, -1.0D, entity.motionZ / 3.0D)).isEmpty();
    }

    public static double gbps(Entity en, int d) {
        double x = en.posX - en.prevPosX;
        double z = en.posZ - en.prevPosZ;
        double sp = Math.sqrt(x * x + z * z) * 20.0D;
        return rnd(sp, d);
    }

    public static String removeFormatCodes(String str) {
        return str.replace("§k", "").replace("§l", "").replace("§m", "").replace("§n", "").replace("§o", "").replace("§r", "");
    }

    public static boolean ilc() {
        if (ModuleManager.autoClicker.isEnabled()) {
            return Mouse.isButtonDown(0);
        } else return CPSCalculator.f() > 1 && System.currentTimeMillis() - CPSCalculator.LL < 300L;
    }

    public static boolean tryingToCombo() {
        return Mouse.isButtonDown(0) && Mouse.isButtonDown(1);
    }

    public static void setMouseButtonState(int mouseButton, boolean held) {
        MouseEvent m = new MouseEvent();

        ObfuscationReflectionHelper.setPrivateValue(MouseEvent.class, m, mouseButton, "button");
        ObfuscationReflectionHelper.setPrivateValue(MouseEvent.class, m, held, "buttonstate");
        MinecraftForge.EVENT_BUS.post(m);

        ByteBuffer buttons = ObfuscationReflectionHelper.getPrivateValue(Mouse.class, null, "buttons");
        buttons.put(mouseButton, (byte) (held ? 1 : 0));
        ObfuscationReflectionHelper.setPrivateValue(Mouse.class, null, buttons, "buttons");

    }

    public static long getDifference(long n, long n2) {
        return Math.abs(n2 - n);
    }

    public static void sendModuleMessage(Module module, String s) {
        sendRawMessage("&3" + module.getName() + "&7: &r" + s);
    }

    public static EntityLivingBase raytrace(final int n) {
        Entity entity = null;
        MovingObjectPosition rayTrace = mc.thePlayer.rayTrace((double) n, 1.0f);
        final Vec3 getPositionEyes = mc.thePlayer.getPositionEyes(1.0f);
        final float rotationYaw = mc.thePlayer.rotationYaw;
        final float rotationPitch = mc.thePlayer.rotationPitch;
        final float cos = MathHelper.cos(-rotationYaw * 0.017453292f - 3.1415927f);
        final float sin = MathHelper.sin(-rotationYaw * 0.017453292f - 3.1415927f);
        final float n2 = -MathHelper.cos(-rotationPitch * 0.017453292f);
        final Vec3 vec3 = new Vec3((double) (sin * n2), (double) MathHelper.sin(-rotationPitch * 0.017453292f), (double) (cos * n2));
        final Vec3 addVector = getPositionEyes.addVector(vec3.xCoord * (double) n, vec3.yCoord * (double) n, vec3.zCoord * (double) n);
        Vec3 vec4 = null;
        final List getEntitiesWithinAABBExcludingEntity = mc.theWorld.getEntitiesWithinAABBExcludingEntity(mc.getRenderViewEntity(), mc.getRenderViewEntity().getEntityBoundingBox().addCoord(vec3.xCoord * (double) n, vec3.yCoord * (double) n, vec3.zCoord * (double) n).expand(1.0, 1.0, 1.0));
        double n3 = (double) n;
        for (int i = 0; i < getEntitiesWithinAABBExcludingEntity.size(); ++i) {
            final Entity entity2 = (Entity) getEntitiesWithinAABBExcludingEntity.get(i);
            if (entity2.canBeCollidedWith()) {
                final float getCollisionBorderSize = entity2.getCollisionBorderSize();
                final AxisAlignedBB expand = entity2.getEntityBoundingBox().expand((double) getCollisionBorderSize, (double) getCollisionBorderSize, (double) getCollisionBorderSize);
                final MovingObjectPosition calculateIntercept = expand.calculateIntercept(getPositionEyes, addVector);
                if (expand.isVecInside(getPositionEyes)) {
                    if (0.0 < n3 || n3 == 0.0) {
                        entity = entity2;
                        vec4 = ((calculateIntercept == null) ? getPositionEyes : calculateIntercept.hitVec);
                        n3 = 0.0;
                    }
                } else if (calculateIntercept != null) {
                    final double distanceTo = getPositionEyes.distanceTo(calculateIntercept.hitVec);
                    if (distanceTo < n3 || n3 == 0.0) {
                        if (entity2 == mc.getRenderViewEntity().ridingEntity && !entity2.canRiderInteract()) {
                            if (n3 == 0.0) {
                                entity = entity2;
                                vec4 = calculateIntercept.hitVec;
                            }
                        } else {
                            entity = entity2;
                            vec4 = calculateIntercept.hitVec;
                            n3 = distanceTo;
                        }
                    }
                }
            }
        }
        if (entity != null && (n3 < n || rayTrace == null)) {
            rayTrace = new MovingObjectPosition(entity, vec4);
        }
        if (rayTrace != null && rayTrace.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && rayTrace.entityHit instanceof EntityLivingBase) {
            return (EntityLivingBase) rayTrace.entityHit;
        }
        return null;
    }

    public static int getChroma(long speed, long... delay) {
        long time = System.currentTimeMillis() + (delay.length > 0 ? delay[0] : 0L);
        return Color.getHSBColor((float) (time % (15000L / speed)) / (15000.0F / (float) speed), 1.0F, 1.0F).getRGB();
    }

    public static double rnd(double n, int d) {
        if (d == 0) {
            return (double) Math.round(n);
        } else {
            double p = Math.pow(10.0D, (double) d);
            return (double) Math.round(n * p) / p;
        }
    }

    public static String stripColor(final String s) {
        if (s.isEmpty()) {
            return s;
        }
        final char[] array = StringUtils.stripControlCodes(s).toCharArray();
        final StringBuilder sb = new StringBuilder();
        for (final char c : array) {
            if (c < '\u007f' && c > '\u0014') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static List<String> gsl() {
        List<String> lines = new ArrayList();
        if (mc.theWorld == null) {
            return lines;
        } else {
            Scoreboard scoreboard = mc.theWorld.getScoreboard();
            if (scoreboard == null) {
                return lines;
            } else {
                ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(1);
                if (objective == null) {
                    return lines;
                } else {
                    Collection<Score> scores = scoreboard.getSortedScores(objective);
                    List<Score> list = new ArrayList();
                    Iterator var5 = scores.iterator();

                    Score score;
                    while (var5.hasNext()) {
                        score = (Score) var5.next();
                        if (score != null && score.getPlayerName() != null && !score.getPlayerName().startsWith("#")) {
                            list.add(score);
                        }
                    }

                    if (list.size() > 15) {
                        scores = Lists.newArrayList(Iterables.skip(list, scores.size() - 15));
                    } else {
                        scores = list;
                    }

                    var5 = scores.iterator();

                    while (var5.hasNext()) {
                        score = (Score) var5.next();
                        ScorePlayerTeam team = scoreboard.getPlayersTeam(score.getPlayerName());
                        lines.add(ScorePlayerTeam.formatPlayerName(team, score.getPlayerName()));
                    }

                    return lines;
                }
            }
        }
    }

    public static void rsa() {
        EntityPlayerSP p = mc.thePlayer;
        int armSwingEnd = p.isPotionActive(Potion.digSpeed) ? 6 - (1 + p.getActivePotionEffect(Potion.digSpeed).getAmplifier()) : (p.isPotionActive(Potion.digSlowdown) ? 6 + (1 + p.getActivePotionEffect(Potion.digSlowdown).getAmplifier()) * 2 : 6);
        if (!p.isSwingInProgress || p.swingProgressInt >= armSwingEnd / 2 || p.swingProgressInt < 0) {
            p.swingProgressInt = -1;
            p.isSwingInProgress = true;
        }

    }

    public static String uf(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    public static boolean overAir() {
        if (!Utils.nullCheck()) return false;
        return mc.theWorld.isAirBlock(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ));
    }

    public static boolean overPlaceable(double yOffset) {
        BlockPos playerPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY + yOffset, mc.thePlayer.posZ);
        return BlockUtils.replaceable(playerPos) || BlockUtils.isFluid(BlockUtils.getBlock(playerPos));
    }

    public static boolean holdingWeapon() {
        ItemStack item = SlotHandler.getHeldItem();
        if (item == null) {
            return false;
        }
        Item getItem = item.getItem();
        return (!Settings.weaponSword.isToggled() || getItem instanceof ItemSword) || (Settings.weaponAxe.isToggled() && getItem instanceof ItemAxe) || (Settings.weaponRod.isToggled() && getItem instanceof ItemFishingRod) || (Settings.weaponStick.isToggled() && getItem == Items.stick);
    }

    public static boolean holdingSword() {
        if (SlotHandler.getHeldItem() == null) {
            return false;
        }
        return SlotHandler.getHeldItem().getItem() instanceof ItemSword;
    }

    public static double getDamage(final ItemStack itemStack) {
        double getAmount = 0;
        for (final Map.Entry<String, AttributeModifier> entry : itemStack.getAttributeModifiers().entries()) {
            if (entry.getKey().equals("generic.attackDamage")) {
                getAmount = entry.getValue().getAmount();
                break;
            }
        }
        return getAmount + EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, itemStack) * 1.25;
    }

    public static @NotNull String getUnformatedString(@NotNull String s) {
        StringBuilder stringbuilder = new StringBuilder();

        boolean doIgnore = false;
        forEach:
        for (char c : s.toCharArray()) {
            if (doIgnore) {
                doIgnore = false;
                for (ChatFormatting format : ChatFormatting.values())
                    if (format.getChar() == c)
                        continue forEach;
            }
            if (c == '§') {
                doIgnore = true;
                continue;
            }

            stringbuilder.append(c);
        }

        return stringbuilder.toString();
    }

    public static keystrokesmod.script.classes.Vec3 getEyePos(@NotNull Entity entity, keystrokesmod.script.classes.@NotNull Vec3 position) {
        return position.add(new keystrokesmod.script.classes.Vec3(0, entity.getEyeHeight(), 0));
    }

    public static keystrokesmod.script.classes.Vec3 getEyePos(Entity entity) {
        return getEyePos(entity, new keystrokesmod.script.classes.Vec3(entity));
    }

    public static keystrokesmod.script.classes.Vec3 getEyePos() {
        return getEyePos(mc.thePlayer);
    }

    public static boolean isTargetNearby() {
        return mc.theWorld.playerEntities.stream()
                .filter(target -> target != mc.thePlayer)
                .anyMatch(target -> new keystrokesmod.script.classes.Vec3(target).distanceTo(mc.thePlayer) < 6);
    }

    /**
     * Checks if the player is in a liquid
     *
     * @return in liquid
     */
    public static boolean inLiquid() {
        return mc.thePlayer.isInWater() || mc.thePlayer.isInLava();
    }

    public static boolean isLobby() {
        if (Utils.isHypixel()) {
            List<String> sidebarLines = Utils.getSidebarLines();
            if (!sidebarLines.isEmpty()) {
                try {
                    String[] parts = Utils.stripColor(sidebarLines.get(1)).split(" {2}");
                    return parts.length > 1 && parts[1].charAt(0) == 'L';
                } catch (IndexOutOfBoundsException ignored) {
                }
            }
        }
        return false;
    }

    public static String getKeyName(int keyCode) {
        if (keyCode >= 1000)
            return "M" + (keyCode - 1000);
        try {
            return Keyboard.getKeyName(keyCode);
        } catch (Exception e) {
            return "Unknown";
        }
    }

    public static int getKeyCode(@NotNull String keyName) {
        try {
            if (keyName.startsWith("M") && keyName.length() > 1) {
                final String number = keyName.substring(1);
                if (number.chars().allMatch(Character::isDigit)) {
                    return Integer.parseInt(number) + 1000;
                }
            }

            return Keyboard.getKeyIndex(keyName);
        } catch (Exception e) {
            return Keyboard.KEY_NONE;
        }
    }

    public static boolean isInRange(double value, double target, double range) {
        return value - range <= target && value + range >= target;

    }

    /**
     * Sends a click to Minecraft legitimately
     */
    public static void sendClick(final int button, final boolean state) {
        final int keyBind = button == 0 ? mc.gameSettings.keyBindAttack.getKeyCode() : mc.gameSettings.keyBindUseItem.getKeyCode();

        KeyBinding.setKeyBindState(keyBind, state);

        if (state) {
            KeyBinding.onTick(keyBind);
        }
    }

    public static void inventoryClick(@NotNull GuiScreen s) {
        int x = Mouse.getX() * s.width / mc.displayWidth;
        int y = s.height - Mouse.getY() * s.height / mc.displayHeight - 1;

        ClickEvent event = new ClickEvent();
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled())
            return;

        ((GuiScreenAccessor) s).mouseClicked(x, y, 0);
    }
}
