package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.player.Freecam;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class Nametags extends Module { // skidded from raven source code as well
    private SliderSetting yOffset;
    private SliderSetting scale;
    private ButtonSetting autoScale;
    private ButtonSetting drawRect;
    private ButtonSetting showDistance;
    private ButtonSetting showHealth;
    private ButtonSetting showHitsToKill;
    private ButtonSetting showInvis;
    private ButtonSetting removeTags;
    private ButtonSetting showArmor;
    private ButtonSetting showEnchants;
    private ButtonSetting showDurability;
    private ButtonSetting showStackSize;

    public Nametags() {
        super("Nametags", category.render, 0);
        this.registerSetting(yOffset = new SliderSetting("Y-Offset", 0.0D, -40.0D, 40.0D, 1.0D));
        this.registerSetting(scale = new SliderSetting("Scale", 1.0, 0.5, 5.0, 0.1));
        this.registerSetting(autoScale = new ButtonSetting("Auto-scale", true));
        this.registerSetting(drawRect = new ButtonSetting("Draw rect", true));
        this.registerSetting(showDistance = new ButtonSetting("Show distance", false));
        this.registerSetting(showHealth = new ButtonSetting("Show health", true));
        this.registerSetting(showHitsToKill = new ButtonSetting("Show hits to kill", false));
        this.registerSetting(showInvis = new ButtonSetting("Show invis", true));
        this.registerSetting(removeTags = new ButtonSetting("Remove tags", false));
        this.registerSetting(new DescriptionSetting("Armor settings"));
        this.registerSetting(showArmor = new ButtonSetting("Show armor", false));
        this.registerSetting(showEnchants = new ButtonSetting("Show enchants", true));
        this.registerSetting(showDurability = new ButtonSetting("Show durability", true));
        this.registerSetting(showStackSize = new ButtonSetting("Show stack size", true));
    }

    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Specials.Pre e) {
        if (e.entity instanceof EntityPlayer && e.entity != mc.thePlayer && e.entity.deathTime == 0) {
            final EntityPlayer entityPlayer = (EntityPlayer) e.entity;
            if (!showInvis.isToggled() && entityPlayer.isInvisible()) {
                return;
            }
            if (entityPlayer.getDisplayNameString().isEmpty() || AntiBot.isBot(entityPlayer)) {
                return;
            }

            e.setCanceled(true);
            String name;
            if (removeTags.isToggled()) {
                name = entityPlayer.getName();
            }
            else {
                name = entityPlayer.getDisplayName().getFormattedText();
            }
            if (showHealth.isToggled()) {
                name = name + " " + Utils.getHealthStr(entityPlayer);
            }
            if (showHitsToKill.isToggled()) {
                name = name + " " + Utils.getHitsToKill(entityPlayer, mc.thePlayer.getCurrentEquippedItem());
            }
            if (showDistance.isToggled()) {
                int distance = Math.round(mc.thePlayer.getDistanceToEntity(entityPlayer));
                String color = "§";
                if (distance <= 8) {
                    color += "c";
                }
                else if (distance <= 15) {
                    color += "6";
                }
                else if (distance <= 25) {
                    color += "e";
                }
                else {
                    color = "";
                }
                name = color + distance + "m§r " + name;
            }
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) e.x + 0.0f, (float) e.y + entityPlayer.height + 0.5f, (float) e.z);
            GL11.glNormal3f(0.0f, 1.0f, 0.0f);
            GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0f, 1.0f, 0.0f);
            GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0f, 0.0f, 0.0f);
            final float n = 0.02666667f;
            if (autoScale.isToggled()) {
                final float renderPartialTicks = Utils.getTimer().renderPartialTicks;
                final EntityPlayer o = (Freecam.freeEntity == null) ? mc.thePlayer : Freecam.freeEntity;
                final double n2 = o.lastTickPosX + (o.posX - o.lastTickPosX) * renderPartialTicks - (entityPlayer.lastTickPosX + (entityPlayer.posX - entityPlayer.lastTickPosX) * renderPartialTicks);
                final double n3 = o.lastTickPosY + (o.posY - o.lastTickPosY) * renderPartialTicks - (entityPlayer.lastTickPosY + (entityPlayer.posY - entityPlayer.lastTickPosY) * renderPartialTicks);
                final double n4 = o.lastTickPosZ + (o.posZ - o.lastTickPosZ) * renderPartialTicks - (entityPlayer.lastTickPosZ + (entityPlayer.posZ - entityPlayer.lastTickPosZ) * renderPartialTicks);
                final double n5 = MathHelper.sqrt_double(n2 * n2 + n3 * n3 + n4 * n4);
                final float n6 = (float) Math.max(n, 0.003 * n5 + 0.011);
                final float n7 = (float) (-(Math.max(0.07, -0.03866143897175789 + 0.018833419308066368 * n5 - 5.270970286801457E-4 * Math.pow(n5, 2.0) + 5.4459292186948005E-6 * Math.pow(n5, 3.0) - 1.9360259173595296E-8 * Math.pow(n5, 4.0)) * n5));
                GlStateManager.scale(-n6 * scale.getInput(), -n6 * scale.getInput(), n6 * scale.getInput());
                GlStateManager.translate(0.0f, n7, 0.0f);
            } else {
                final float n8 = (float) (n * scale.getInput());
                GlStateManager.scale(-n8, -n8, n8);
            }
            if (entityPlayer.isSneaking() && scale.getInput() == 1.0 && !autoScale.isToggled()) {
                GlStateManager.translate(0.0f, 9.374999f, 0.0f);
            }
            GlStateManager.disableLighting();
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            final int n9 = (int) (-yOffset.getInput());
            final int n10 = mc.fontRendererObj.getStringWidth(name) / 2;
            GlStateManager.disableTexture2D();
            int d = 1;
            int d2 = 1;
            if (Utils.isFriended(entityPlayer)) {
                d2 = 0;
            } else {
                d = 0;
            }
            if (d == 0 && d2 != 0 && !Utils.isEnemy(entityPlayer)) {
                d2 = 0;
            }
            if (drawRect.isToggled() || d != 0 || d2 != 0) {
                float n11 = 0.0f;
                float n12 = 0.0f;
                if (d != 0) {
                    n12 = 1.0f;
                } else if (d2 != 0) {
                    n11 = 1.0f;
                }
                final Tessellator getInstance = Tessellator.getInstance();
                final WorldRenderer getWorldRenderer = getInstance.getWorldRenderer();
                getWorldRenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
                getWorldRenderer.pos(-n10 - 1, -1 + n9, 0.0).color(n11, n12, 0.0f, 0.25f).endVertex();
                getWorldRenderer.pos(-n10 - 1, 8 + n9, 0.0).color(n11, n12, 0.0f, 0.25f).endVertex();
                getWorldRenderer.pos(n10 + 1, 8 + n9, 0.0).color(n11, n12, 0.0f, 0.25f).endVertex();
                getWorldRenderer.pos(n10 + 1, -1 + n9, 0.0).color(n11, n12, 0.0f, 0.25f).endVertex();
                getInstance.draw();
            }
            GlStateManager.enableTexture2D();
            mc.fontRendererObj.drawString(name, -n10, n9, -1);
            if (showArmor.isToggled()) {
                renderArmor(entityPlayer);
            }
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.popMatrix();
        }
    }

    private void renderArmor(EntityPlayer e) {
        int pos = 0;
        for (ItemStack is : e.inventory.armorInventory) {
            if (is != null) {
                pos -= 8;
            }
        }
        if (e.getHeldItem() != null) {
            pos -= 8;
            ItemStack item = e.getHeldItem().copy();
            if (item.hasEffect() && (item.getItem() instanceof ItemTool || item.getItem() instanceof ItemArmor)) {
                item.stackSize = 1;
            }
            renderItemStack(item, pos, (int) (-20 - yOffset.getInput()));
            pos += 16;
        }
        for (int i = 3; i >= 0; --i) {
            ItemStack stack = e.inventory.armorInventory[i];
            if (stack != null) {
                renderItemStack(stack, pos, (int) (-20 - yOffset.getInput()));
                pos += 16;
            }
        }
    }

    private void renderItemStack(ItemStack stack, int xPos, int yPos) {
        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        mc.getRenderItem().zLevel = -150.0F;
        mc.getRenderItem().renderItemAndEffectIntoGUI(stack, xPos, yPos);
        mc.getRenderItem().zLevel = 0.0F;
        GlStateManager.scale(0.5, 0.5, 0.5);
        renderText(stack, xPos, yPos);
        GlStateManager.scale(2, 2, 2);
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    private void renderText(ItemStack stack, int xPos, int yPos) {
        int newYPos = yPos - 24;
        int remainingDurability = stack.getMaxDamage() - stack.getItemDamage();
        if (showDurability.isToggled() && stack.getItem() instanceof ItemArmor) {
            mc.fontRendererObj.drawStringWithShadow(String.valueOf(remainingDurability), (float) (xPos * 2), (float) yPos, 16777215);
        }
        if (stack.getEnchantmentTagList() != null && stack.getEnchantmentTagList().tagCount() < 6 && showEnchants.isToggled()) {
            if (stack.getItem() instanceof ItemArmor) {
                int protection = EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack);
                int projectileProtection = EnchantmentHelper.getEnchantmentLevel(Enchantment.projectileProtection.effectId, stack);
                int blastProtectionLvL = EnchantmentHelper.getEnchantmentLevel(Enchantment.blastProtection.effectId, stack);
                int fireProtection = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireProtection.effectId, stack);
                int thornsLvl = EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack);
                int unbreakingLvl = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);

                if (protection > 0) {
                    mc.fontRendererObj.drawStringWithShadow("prot" + protection, (float) (xPos * 2), (float) newYPos, -1);
                    newYPos += 8;
                }

                if (projectileProtection > 0) {
                    mc.fontRendererObj.drawStringWithShadow("proj" + projectileProtection, (float) (xPos * 2), (float) newYPos, -1);
                    newYPos += 8;
                }

                if (blastProtectionLvL > 0) {
                    mc.fontRendererObj.drawStringWithShadow("bp" + blastProtectionLvL, (float) (xPos * 2), (float) newYPos, -1);
                    newYPos += 8;
                }

                if (fireProtection > 0) {
                    mc.fontRendererObj.drawStringWithShadow("frp" + fireProtection, (float) (xPos * 2), (float) newYPos, -1);
                    newYPos += 8;
                }

                if (thornsLvl > 0) {
                    mc.fontRendererObj.drawStringWithShadow("th" + thornsLvl, (float) (xPos * 2), (float) newYPos, -1);
                    newYPos += 8;
                }

                if (unbreakingLvl > 0) {
                    mc.fontRendererObj.drawStringWithShadow("ub" + unbreakingLvl, (float) (xPos * 2), (float) newYPos, -1);
                }
            }
            else if (stack.getItem() instanceof ItemBow) {
                int powerLvl = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
                int punchLvl = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack);
                int flameLvl = EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack);
                int unbreakingLvl = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
                if (powerLvl > 0) {
                    mc.fontRendererObj.drawStringWithShadow("pow" + powerLvl, (float) (xPos * 2), (float) newYPos, -1);
                    newYPos += 8;
                }

                if (punchLvl > 0) {
                    mc.fontRendererObj.drawStringWithShadow("pun" + punchLvl, (float) (xPos * 2), (float) newYPos, -1);
                    newYPos += 8;
                }

                if (flameLvl > 0) {
                    mc.fontRendererObj.drawStringWithShadow("flame" + flameLvl, (float) (xPos * 2), (float) newYPos, -1);
                    newYPos += 8;
                }

                if (unbreakingLvl > 0) {
                    mc.fontRendererObj.drawStringWithShadow("ub" + unbreakingLvl, (float) (xPos * 2), (float) newYPos, -1);
                }
            }
            else if (stack.getItem() instanceof ItemSword) {
                int sharpnessLvl = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack);
                int knockbackLvl = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, stack);
                int fireAspectLvl = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack);
                int unbreakingLvl = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
                if (sharpnessLvl > 0) {
                    mc.fontRendererObj.drawStringWithShadow("sh" + sharpnessLvl, (float) (xPos * 2), (float) newYPos, -1);
                    newYPos += 8;
                }

                if (knockbackLvl > 0) {
                    mc.fontRendererObj.drawStringWithShadow("kb" + knockbackLvl, (float) (xPos * 2), (float) newYPos, -1);
                    newYPos += 8;
                }

                if (fireAspectLvl > 0) {
                    mc.fontRendererObj.drawStringWithShadow("fire" + fireAspectLvl, (float) (xPos * 2), (float) newYPos, -1);
                    newYPos += 8;
                }

                if (unbreakingLvl > 0) {
                    mc.fontRendererObj.drawStringWithShadow("ub" + unbreakingLvl, (float) (xPos * 2), (float) newYPos, -1);
                }
            }
            else if (stack.getItem() instanceof ItemTool) {
                int unbreakingLvl = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack);
                int efficiencyLvl = EnchantmentHelper.getEnchantmentLevel(Enchantment.efficiency.effectId, stack);
                int fortuneLvl = EnchantmentHelper.getEnchantmentLevel(Enchantment.fortune.effectId, stack);
                int silkTouchLvl = EnchantmentHelper.getEnchantmentLevel(Enchantment.silkTouch.effectId, stack);
                if (efficiencyLvl > 0) {
                    mc.fontRendererObj.drawStringWithShadow("eff" + efficiencyLvl, (float) (xPos * 2), (float) newYPos, -1);
                    newYPos += 8;
                }

                if (fortuneLvl > 0) {
                    mc.fontRendererObj.drawStringWithShadow("fo" + fortuneLvl, (float) (xPos * 2), (float) newYPos, -1);
                    newYPos += 8;
                }

                if (silkTouchLvl > 0) {
                    mc.fontRendererObj.drawStringWithShadow("silk" + silkTouchLvl, (float) (xPos * 2), (float) newYPos, -1);
                    newYPos += 8;
                }

                if (unbreakingLvl > 0) {
                    mc.fontRendererObj.drawStringWithShadow("ub" + unbreakingLvl, (float) (xPos * 2), (float) newYPos, -1);
                }
            }
        }
        if (showStackSize.isToggled() && !(stack.getItem() instanceof ItemSword) && !(stack.getItem() instanceof ItemBow) && !(stack.getItem() instanceof ItemTool) && !(stack.getItem() instanceof ItemArmor)) {
            mc.fontRendererObj.drawStringWithShadow(stack.stackSize + "x", (float) (xPos * 2), (float) yPos, -1);
        }
    }
}
