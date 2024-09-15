package keystrokesmod.module.impl.combat;

import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.RotationUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Reach extends Module {
    public static SliderSetting min;
    public static SliderSetting max;
    public static SliderSetting chance;
    public static ButtonSetting weaponOnly;
    public static ButtonSetting movingOnly;
    public static ButtonSetting sprintOnly;
    public static ButtonSetting hitThroughBlocks;

    public Reach() {
        super("Reach", Module.category.combat, 0);
        this.registerSetting(min = new SliderSetting("Min", 3.1, 0, 6, 0.05));
        this.registerSetting(max = new SliderSetting("Max", 3.3, 0, 6, 0.05));
        this.registerSetting(chance = new SliderSetting("Chance", 100, 0, 100, 1, "%"));
        this.registerSetting(weaponOnly = new ButtonSetting("Weapon only", false));
        this.registerSetting(movingOnly = new ButtonSetting("Moving only", false));
        this.registerSetting(sprintOnly = new ButtonSetting("Sprint only", false));
        this.registerSetting(hitThroughBlocks = new ButtonSetting("Hit through blocks", false));
    }

    public void guiUpdate() {
        Utils.correctValue(min, max);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void e(@NotNull MouseEvent ev) {
        if (ev.button >= 0 && ev.buttonstate && Utils.nullCheck()) {
            call();
        }
    }

    public static void call() {
        if (!ModuleManager.reach.isEnabled()) return;
        if (Utils.nullCheck()
                && (!weaponOnly.isToggled() || Utils.holdingWeapon())
                && (!movingOnly.isToggled() || Utils.isMoving())
                && (!sprintOnly.isToggled() || mc.thePlayer.isSprinting())
                && (chance.getInput() == 100 || Math.random() * 100 > chance.getInput())
        ) {

            double r = Utils.getRandomValue(min, max, Utils.getRandom());
            Object[] o = getEntity(r);

            if (o != null) {
                if (!RotationUtils.rayCastIgnoreWall(mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, (EntityLivingBase) o[0]))
                    return;
                if (!hitThroughBlocks.isToggled() && RotationUtils.rayCast(r, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch) != null)
                    return;

                Entity en = (Entity) o[0];
                mc.objectMouseOver = new MovingObjectPosition(en, (Vec3) o[1]);
                mc.pointedEntity = en;
            }
        }
    }

    private static Object[] getEntity(double reach) {
        if (!ModuleManager.reach.isEnabled()) {
            reach = mc.playerController.extendedReach() ? 6.0D : 3.0D;
        }
        return getEntity(reach, 0.0, null);
    }

    public static Object[] getEntity(double reach, double expand, float[] rotations) {
        Entity zz2 = mc.getRenderViewEntity();
        Entity entity = null;
        if (zz2 == null) {
            return null;
        } else {
            mc.mcProfiler.startSection("pick");
            Vec3 zz3 = zz2.getPositionEyes(1.0F);
            Vec3 zz4;
            if (rotations != null) {
                zz4 = RotationUtils.getVectorForRotation(rotations[1], rotations[0]);
            }
            else {
                zz4 = zz2.getLook(1.0F);
            }
            Vec3 zz5 = zz3.addVector(zz4.xCoord * reach, zz4.yCoord * reach, zz4.zCoord * reach);
            Vec3 hitVec = null;
            List zz8 = mc.theWorld.getEntitiesWithinAABBExcludingEntity(zz2, zz2.getEntityBoundingBox().addCoord(zz4.xCoord * reach, zz4.yCoord * reach, zz4.zCoord * reach).expand(1.0D, 1.0D, 1.0D));
            double zz9 = reach;

            for (int zz10 = 0; zz10 < zz8.size(); ++zz10) {
                Entity zz11 = (Entity) zz8.get(zz10);
                if (zz11.canBeCollidedWith()) {
                    float ex = (float) ((double) zz11.getCollisionBorderSize() * HitBox.getExpand(zz11));
                    AxisAlignedBB zz13 = zz11.getEntityBoundingBox().expand(ex, ex, ex);
                    zz13 = zz13.expand(expand, expand, expand);
                    MovingObjectPosition zz14 = zz13.calculateIntercept(zz3, zz5);
                    if (zz13.isVecInside(zz3)) {
                        if (0.0D < zz9 || zz9 == 0.0D) {
                            entity = zz11;
                            hitVec = zz14 == null ? zz3 : zz14.hitVec;
                            zz9 = 0.0D;
                        }
                    } else if (zz14 != null) {
                        double zz15 = zz3.distanceTo(zz14.hitVec);
                        if (zz15 < zz9 || zz9 == 0.0D) {
                            if (zz11 == zz2.ridingEntity) {
                                if (zz9 == 0.0D) {
                                    entity = zz11;
                                    hitVec = zz14.hitVec;
                                }
                            } else {
                                entity = zz11;
                                hitVec = zz14.hitVec;
                                zz9 = zz15;
                            }
                        }
                    }
                }
            }

            if (zz9 < reach && !(entity instanceof EntityLivingBase) && !(entity instanceof EntityItemFrame)) {
                entity = null;
            }

            mc.mcProfiler.endSection();
            if (entity != null && hitVec != null) {
                return new Object[]{entity, hitVec};
            } else {
                return null;
            }
        }
    }
}