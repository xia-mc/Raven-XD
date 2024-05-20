package keystrokesmod.module.impl.combat;

import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;

public class HitBox extends Module {
    public static SliderSetting multiplier;
    public ButtonSetting showHitbox;
    public ButtonSetting playersOnly;
    public ButtonSetting weaponOnly;
    private Entity pointedEntity;
    private MovingObjectPosition mv;

    public HitBox() {
        super("HitBox", category.combat, 0);
        this.registerSetting(multiplier = new SliderSetting("Multiplier", 1.2, 1.0, 5.0, 0.05, "x"));
        this.registerSetting(playersOnly = new ButtonSetting("Players only", true));
        this.registerSetting(showHitbox = new ButtonSetting("Show new hitbox", false));
        this.registerSetting(weaponOnly = new ButtonSetting("Weapon only", false));
    }

    @Override
    public String getInfo() {
        return ((int) multiplier.getInput() == multiplier.getInput() ? (int) multiplier.getInput() + "" : multiplier.getInput()) + multiplier.getInfo();
    }

    @SubscribeEvent
    public void m(MouseEvent e) {
        if (Utils.nullCheck()) {
            if (e.button != 0 || !e.buttonstate || !Utils.nullCheck() || multiplier.getInput() == 1 || mc.thePlayer.isBlocking() || mc.currentScreen != null) {
                return;
            }
            if (weaponOnly.isToggled() && !Utils.holdingWeapon()) {
                return;
            }
            EntityLivingBase c = getEntity(1.0F);
            if (c == null) {
                return;
            }
            if (c instanceof EntityPlayer) {
                if (Utils.isFriended((EntityPlayer) c)) {
                    return;
                }
            } else if (playersOnly.isToggled()) {
                return;
            }
            mc.objectMouseOver = mv;
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent e) {
        if (showHitbox.isToggled() && Utils.nullCheck()) {
            for (Entity en : mc.theWorld.loadedEntityList) {
                if (en != mc.thePlayer && en instanceof EntityLivingBase && ((EntityLivingBase) en).deathTime == 0 && !(en instanceof EntityArmorStand) && !en.isInvisible()) {
                    this.rh(en, Color.WHITE);
                }
            }
        }
    }

    public static double getExpand(Entity en) {
        return multiplier.getInput();
    }

    public EntityLivingBase getEntity(float partialTicks) {
        if (mc.getRenderViewEntity() != null && mc.theWorld != null) {
            mc.pointedEntity = null;
            pointedEntity = null;
            double d0 = mc.playerController.extendedReach() ? 6.0 : (ModuleManager.reach.isEnabled() ? Utils.getRandomValue(Reach.min, Reach.max, Utils.getRandom()) : 3.0);
            mv = mc.getRenderViewEntity().rayTrace(d0, partialTicks);
            double d2 = d0;
            Vec3 vec3 = mc.getRenderViewEntity().getPositionEyes(partialTicks);

            if (mv != null) {
                d2 = mv.hitVec.distanceTo(vec3);
            }

            Vec3 vec4 = mc.getRenderViewEntity().getLook(partialTicks);
            Vec3 vec5 = vec3.addVector(vec4.xCoord * d0, vec4.yCoord * d0, vec4.zCoord * d0);
            Vec3 vec6 = null;
            float f1 = 1.0F;
            List list = mc.theWorld.getEntitiesWithinAABBExcludingEntity(mc.getRenderViewEntity(), mc.getRenderViewEntity().getEntityBoundingBox().addCoord(vec4.xCoord * d0, vec4.yCoord * d0, vec4.zCoord * d0).expand(f1, f1, f1));
            double d3 = d2;

            for (Object o : list) {
                Entity entity = (Entity) o;
                if (entity.canBeCollidedWith()) {
                    float ex = (float) ((double) entity.getCollisionBorderSize() * getExpand(entity));
                    AxisAlignedBB ax = entity.getEntityBoundingBox().expand(ex, ex, ex);
                    MovingObjectPosition mop = ax.calculateIntercept(vec3, vec5);
                    if (ax.isVecInside(vec3)) {
                        if (0.0D < d3 || d3 == 0.0D) {
                            pointedEntity = entity;
                            vec6 = mop == null ? vec3 : mop.hitVec;
                            d3 = 0.0D;
                        }
                    } else if (mop != null) {
                        double d4 = vec3.distanceTo(mop.hitVec);
                        if (d4 < d3 || d3 == 0.0D) {
                            if (entity == mc.getRenderViewEntity().ridingEntity && !entity.canRiderInteract()) {
                                if (d3 == 0.0D) {
                                    pointedEntity = entity;
                                    vec6 = mop.hitVec;
                                }
                            } else {
                                pointedEntity = entity;
                                vec6 = mop.hitVec;
                                d3 = d4;
                            }
                        }
                    }
                }
            }

            if (pointedEntity != null && (d3 < d2 || mv == null)) {
                mv = new MovingObjectPosition(pointedEntity, vec6);
                if (pointedEntity instanceof EntityLivingBase || pointedEntity instanceof EntityItemFrame) {
                    return (EntityLivingBase) pointedEntity;
                }
            }
        }
        return null;
    }

    private void rh(Entity e, Color c) {
        if (e instanceof EntityLivingBase) {
            double x = e.lastTickPosX + (e.posX - e.lastTickPosX) * (double) Utils.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosX;
            double y = e.lastTickPosY + (e.posY - e.lastTickPosY) * (double) Utils.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosY;
            double z = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * (double) Utils.getTimer().renderPartialTicks - mc.getRenderManager().viewerPosZ;
            float ex = (float) ((double) e.getCollisionBorderSize() * multiplier.getInput());
            AxisAlignedBB bbox = e.getEntityBoundingBox().expand(ex, ex, ex);
            AxisAlignedBB axis = new AxisAlignedBB(bbox.minX - e.posX + x, bbox.minY - e.posY + y, bbox.minZ - e.posZ + z, bbox.maxX - e.posX + x, bbox.maxY - e.posY + y, bbox.maxZ - e.posZ + z);
            GL11.glBlendFunc(770, 771);
            GL11.glEnable(3042);
            GL11.glDisable(3553);
            GL11.glDisable(2929);
            GL11.glDepthMask(false);
            GL11.glLineWidth(2.0F);
            GL11.glColor3d(c.getRed(), c.getGreen(), c.getBlue());
            RenderGlobal.drawSelectionBoundingBox(axis);
            GL11.glEnable(3553);
            GL11.glEnable(2929);
            GL11.glDepthMask(true);
            GL11.glDisable(3042);
        }
    }
}
