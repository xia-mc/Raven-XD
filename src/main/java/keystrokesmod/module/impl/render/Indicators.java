package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Reflection;
import keystrokesmod.utility.RenderUtils;
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

import java.awt.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Indicators extends Module {
    private ButtonSetting renderArrows;
    private ButtonSetting renderPearls;
    private ButtonSetting renderFireballs;
    private ButtonSetting renderPlayers;
    private SliderSetting radius;
    private ButtonSetting itemColors;
    private ButtonSetting renderItem;
    private ButtonSetting threatsOnly;
    private ButtonSetting showInChat;
    private HashSet<Entity> threats = new HashSet<>();
    private Map<String, String> lastHeldItems = new ConcurrentHashMap<>();
    private int pearlColor = new Color(173, 12, 255).getRGB();
    private int fireBallColor = new Color(255, 109, 0).getRGB();

    public Indicators() {
        super("Indicators", category.render);
        this.registerSetting(renderArrows = new ButtonSetting("Render arrows", true));
        this.registerSetting(renderPearls = new ButtonSetting("Render ender pearls", true));
        this.registerSetting(renderFireballs = new ButtonSetting("Render fireballs", true));
        this.registerSetting(renderPlayers = new ButtonSetting("Render players", true));
        this.registerSetting(radius = new SliderSetting("Circle radius", 50, 5, 250, 2));
        this.registerSetting(itemColors = new ButtonSetting("Item colors", true));
        this.registerSetting(renderItem = new ButtonSetting("Render item", true));
        this.registerSetting(threatsOnly = new ButtonSetting("Render only threats", true));
        this.registerSetting(showInChat = new ButtonSetting("Show in chat", false));
    }

    public void onDisable() {
        this.threats.clear();
        this.lastHeldItems.clear();
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (mc.currentScreen != null || !Utils.nullCheck()) {
            return;
        }
        if (threats.isEmpty()) {
            return;
        }
        try {
            Iterator<Entity> iterator = threats.iterator();
            while (iterator.hasNext()) {
                Entity e = iterator.next();
                if (e == null || !mc.theWorld.loadedEntityList.contains(e) || !canRender(e) || (e instanceof EntityArrow && Reflection.inGround.getBoolean(e))) {
                    iterator.remove();
                    continue;
                }
                float yaw = Utils.getYaw(e) - mc.thePlayer.rotationYaw;
                ScaledResolution sr = new ScaledResolution(mc);
                float x = sr.getScaledWidth() / 2;
                float y = sr.getScaledHeight() / 2;
                GL11.glPushMatrix();
                GL11.glTranslated(x, y, 0.0);

                int color = -1;
                if (renderItem.isToggled()) {
                    ItemStack entityItem = null;
                    if (e instanceof EntityEnderPearl) {
                        color = pearlColor;
                        entityItem = new ItemStack(Items.ender_pearl);
                    } else if (e instanceof EntityArrow) {
                        entityItem = new ItemStack(Items.arrow);
                    } else if (e instanceof EntityFireball) {
                        color = fireBallColor;
                        entityItem = new ItemStack(Items.fire_charge);
                    }
                    if (entityItem != null) {
                        GL11.glPushMatrix();
                        GL11.glRotatef(yaw, 0.0f, 0.0f, 1.0f);
                        mc.getRenderItem().renderItemAndEffectIntoGUI(entityItem, -8, (int) -radius.getInput());
                        GL11.glPopMatrix();
                    }
                }

                GL11.glPushMatrix();
                GL11.glRotatef(yaw, 0.0f, 0.0f, 1.0f);
                String distanceStr = (int) mc.thePlayer.getDistanceToEntity(e) + "m";
                float textWidth = mc.fontRendererObj.getStringWidth(distanceStr);
                mc.fontRendererObj.drawStringWithShadow(distanceStr, -textWidth / 2, (float) (-radius.getInput() - 10), -1);
                GL11.glPopMatrix();

                GL11.glPushMatrix();
                GL11.glRotatef(yaw, 0.0f, 0.0f, 1.0f);
                RenderUtils.drawArrow(-5f, (float) -radius.getInput() - 20, color, 3, 5);
                GL11.glPopMatrix();

                GL11.glPopMatrix();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
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
