package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Reflection;
import keystrokesmod.utility.Utils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Indicators extends Module {
    private final ButtonSetting renderArrows;
    private final ButtonSetting renderPearls;
    private final ButtonSetting renderFireballs;
    private final ButtonSetting renderPlayers;
    private final SliderSetting radius;
    private final ButtonSetting renderItem;
    private final ButtonSetting threatsOnly;
    private final ButtonSetting showInChat;
    private final HashSet<Entity> threats = new HashSet<>();
    private final Map<String, String> lastHeldItems = new ConcurrentHashMap<>();

    public Indicators() {
        super("Indicators", category.render);
        this.registerSetting(renderArrows = new ButtonSetting("Render arrows", true));
        this.registerSetting(renderPearls = new ButtonSetting("Render ender pearls", true));
        this.registerSetting(renderFireballs = new ButtonSetting("Render fireballs", true));
        this.registerSetting(renderPlayers = new ButtonSetting("Render players", true));
        this.registerSetting(radius = new SliderSetting("Circle radius", 50, 5, 250, 2));
        this.registerSetting(renderItem = new ButtonSetting("Render item", true));
        this.registerSetting(threatsOnly = new ButtonSetting("Render only threats", true));
        this.registerSetting(showInChat = new ButtonSetting("Show in chat", false));
    }

    public void onDisable() {
        this.threats.clear();
        this.lastHeldItems.clear();
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent e) {
        if (e.phase != TickEvent.Phase.END) {
            return;
        }
        if (mc.currentScreen != null || !Utils.nullCheck()) {
            return;
        }
        if (threats.isEmpty()) {
            return;
        }
        Iterator<Entity> iterator = threats.iterator();
        while (iterator.hasNext()) {
            Entity entity = iterator.next();
            try {
                if (entity == null || !mc.theWorld.loadedEntityList.contains(entity) || (entity instanceof EntityArrow && Reflection.inGround.getBoolean(entity))) {
                    iterator.remove();
                    continue;
                }
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
                continue;
            }
            float rotationDegree = Utils.getYaw(entity) - mc.thePlayer.rotationYaw;
            ScaledResolution sr = new ScaledResolution(mc);
            float x = sr.getScaledWidth( )/ 2;
            float y = sr.getScaledHeight() / 2;
            GL11.glPushMatrix();
            GL11.glTranslatef(x, y, 0.0f);
            GL11.glRotatef(rotationDegree, 0.0f, 0.0f, 1.0f);
            if (renderItem.isToggled()) {
                ItemStack entityItem = null;
                if (entity instanceof EntityEnderPearl) {
                    entityItem = new ItemStack(Items.ender_pearl);
                } else if (entity instanceof EntityArrow) {
                    entityItem = new ItemStack(Items.arrow);
                } else if (entity instanceof EntityFireball) {
                    entityItem = new ItemStack(Items.fire_charge);
                }
                if (entityItem != null) {
                    GL11.glPushMatrix();
                    mc.getRenderItem().renderItemIntoGUI(entityItem, 0, (int) -radius.getInput());
                    GL11.glPopMatrix();
                }
            }
            GL11.glRotatef(-rotationDegree, 0.0f, 0.0f, 1.0f);
            GL11.glTranslatef(-x, -y, 0.0f);
            GL11.glPopMatrix();
        }
    }

    public void onUpdate() {
        if (!showInChat.isToggled()) {
            return;
        }
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player == mc.thePlayer) {
                continue;
            }
            if (AntiBot.isBot(player)) {
                continue;
            }
            ItemStack item = player.getHeldItem();
            String name = player.getName();
            if (item != null && item.getItem() instanceof ItemFireball && !lastHeldItems.containsKey(name)) {
                String itemType = item.getUnlocalizedName();
                lastHeldItems.put(name, itemType);
                double distance = Utils.rnd(mc.thePlayer.getDistanceToEntity(player), 1);
                Utils.sendMessage(player.getDisplayName().getFormattedText() + " &7is holding a §3Fireball &7(§d" + (int) distance +  "m&7)");
            } else if (lastHeldItems.containsKey(name) ) {
                String itemType = lastHeldItems.get(name);
                if (item == null || !itemType.equals(item.getUnlocalizedName())) {
                    lastHeldItems.remove(name);
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinWorldEvent e) {
        if (!Utils.nullCheck()) {
            return;
        }
        if (e.entity == mc.thePlayer) {
            this.threats.clear();
        } else if (canRender(e.entity) && (mc.thePlayer.getDistanceSqToEntity(e.entity) > 16.0 || !threatsOnly.isToggled())) {
            this.threats.add(e.entity);
        }
    }

    private boolean canRender(Entity entity) {
        try {
            if (entity instanceof EntityArrow && !Reflection.inGround.getBoolean(entity) && renderArrows.isToggled()) {
                return true;
            }
            else if (entity instanceof EntityFireball && renderFireballs.isToggled()) {
                return true;
            }
            else if (entity instanceof EntityEnderPearl && renderPearls.isToggled()) {
                return true;
            }
            else if (entity instanceof EntityPlayer && renderPlayers.isToggled()) {
                return true;
            }
        } catch (IllegalAccessException e) {
            Utils.sendMessage("&cIssue checking entity.");
            e.printStackTrace();
            return false;
        }
        return false;
    }
}
