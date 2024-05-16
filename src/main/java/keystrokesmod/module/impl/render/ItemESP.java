package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.utility.RenderUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ItemESP extends Module { // entirely skidded from raven b4 source leak
    private final ButtonSetting renderIron;
    private final ButtonSetting renderGold;

    public ItemESP() {
        super("ItemESP", category.render);
        this.registerSetting(renderIron = new ButtonSetting("Render iron", true));
        this.registerSetting(renderGold = new ButtonSetting("Render gold", true));
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent e) {
        HashMap<Item, ArrayList<EntityItem>> hashMap = new HashMap<>();
        HashMap<Double, Integer> hashMap2 = new HashMap<>();
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityItem) {
                if (entity.ticksExisted < 3) {
                    continue;
                }
                EntityItem entityItem = (EntityItem) entity;
                if (entityItem.getEntityItem().stackSize == 0) {
                    continue;
                }
                Item getItem = entityItem.getEntityItem().getItem();
                if (getItem == null) {
                    continue;
                }
                int stackSize = entityItem.getEntityItem().stackSize;
                double a = a(getItem, entity.posX, entity.posY, entity.posZ);
                Integer n = hashMap2.get(a);
                int n2;
                if (n == null) {
                    n2 = stackSize;
                    ArrayList<EntityItem> list = hashMap.get(getItem);
                    if (list == null) {
                        list = new ArrayList<>();
                    }
                    list.add(entityItem);
                    hashMap.put(getItem, list);
                } else {
                    n2 = n + stackSize;
                }
                hashMap2.put(a, n2);
            }
        }
        if (!hashMap.isEmpty()) {
            float renderPartialTicks = Utils.getTimer().renderPartialTicks;
            for (Map.Entry<Item, ArrayList<EntityItem>> entry : hashMap.entrySet()) {
                Item item = entry.getKey();
                int n4;
                int n3;
                if (item == Items.iron_ingot && renderIron.isToggled()) {
                    n3 = (n4 = -1);
                } else if (item == Items.gold_ingot && renderGold.isToggled()) {
                    n4 = -331703;
                    n3 = -152;
                } else if (item == Items.diamond) {
                    n4 = -10362113;
                    n3 = -7667713;
                } else {
                    if (item != Items.emerald) {
                        continue;
                    }
                    n4 = -15216030;
                    n3 = -14614644;
                }
                for (EntityItem entityItem2 : entry.getValue()) {
                    double a2 = a(item, entityItem2.posX, entityItem2.posY, entityItem2.posZ);
                    double n5 = entityItem2.lastTickPosX + (entityItem2.posX - entityItem2.lastTickPosX) * renderPartialTicks;
                    double n6 = entityItem2.lastTickPosY + (entityItem2.posY - entityItem2.lastTickPosY) * renderPartialTicks;
                    double n7 = entityItem2.lastTickPosZ + (entityItem2.posZ - entityItem2.lastTickPosZ) * renderPartialTicks;
                    double n8 = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * renderPartialTicks - n5;
                    double n9 = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * renderPartialTicks - n6;
                    double n10 = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * renderPartialTicks - n7;
                    GlStateManager.pushMatrix();
                    c(n4, n3, hashMap2.get(a2), n5, n6, n7, MathHelper.sqrt_double(n8 * n8 + n9 * n9 + n10 * n10));
                    GlStateManager.popMatrix();
                }
            }
        }
    }

    public static double c(double n, double n2, double n3) {
        if (n == 0.0) {
            n = 1.0;
        }
        if (n2 == 0.0) {
            n2 = 1.0;
        }
        if (n3 == 0.0) {
            n3 = 1.0;
        }
        return Math.round((n + 1.0) * Math.floor(n2) * (n3 + 2.0));
    }

    private static double a(Item item, double n, double n2, double n3) {
        double c = c(n, n2, n3);
        if (item == Items.iron_ingot) {
            c += 0.155;
        } else if (item == Items.gold_ingot) {
            c += 0.255;
        } else if (item == Items.diamond) {
            c += 0.355;
        } else if (item == Items.emerald) {
            c += 0.455;
        }
        return c;
    }

    public static void c(int n, int n2, int n3, double n4, double n5, double n6, double n7) {
        n4 -= mc.getRenderManager().viewerPosX;
        n5 -= mc.getRenderManager().viewerPosY;
        n6 -= mc.getRenderManager().viewerPosZ;
        GL11.glPushMatrix();
        GL11.glBlendFunc(770, 771);
        GL11.glEnable(3042);
        GL11.glLineWidth(2.0f);
        GL11.glDisable(3553);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        float n8 = (n >> 16 & 0xFF) / 255.0f;
        float n9 = (n >> 8 & 0xFF) / 255.0f;
        float n10 = (n & 0xFF) / 255.0f;
        float min = Math.min(Math.max(0.2f, (float) (0.009999999776482582 * n7)), 0.4f);
        RenderUtils.drawBoundingBox(new AxisAlignedBB(n4 - min, n5, n6 - min, n4 + min, n5 + min * 2.0f, n6 + min), n8, n9, n10, 0.35f);
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) n4, (float) n5 + 0.3, (float) n6);
        GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
        GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0f, 0.0f, 0.0f);
        float min2 = Math.min(Math.max(0.02266667f, (float) (0.001500000013038516 * n7)), 0.07f);
        GlStateManager.scale(-min2, -min2, -min2);
        GlStateManager.depthMask(false);
        GlStateManager.disableDepth();
        String value = String.valueOf(n3);
        mc.fontRendererObj.drawString(value, -(mc.fontRendererObj.getStringWidth(value) / 2) + min2 * 3.5f, -(123.805f * min2 - 2.47494f), n2, true);
        GlStateManager.enableDepth();
        GlStateManager.depthMask(true);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.popMatrix();
    }
}
