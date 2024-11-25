package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.render.RenderUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.*;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class MobESP extends Module {
    private static ButtonSetting blaze, creeper, enderman, ghast, silverfish, skeleton, slime, spider, zombie, zombiePigman;

    public MobESP() {
        super("MobESP", category.render);
        this.registerSetting(blaze = new ButtonSetting("Blaze", true));
        this.registerSetting(creeper = new ButtonSetting("Creeper", true));
        this.registerSetting(enderman = new ButtonSetting("Enderman", true));
        this.registerSetting(ghast = new ButtonSetting("Ghast", true));
        this.registerSetting(silverfish = new ButtonSetting("Silverfish", true));
        this.registerSetting(skeleton = new ButtonSetting("Skeleton", true));
        this.registerSetting(slime = new ButtonSetting("Slime", true));
        this.registerSetting(spider = new ButtonSetting("Spider", true));
        this.registerSetting(zombie = new ButtonSetting("Zombie", true));
        this.registerSetting(zombiePigman = new ButtonSetting("Zombie Pigman", true));
    }

    private void renderMob(Entity entity, int n) {
        RenderUtils.renderEntity(entity, 2, 0.0, 0.0, n, false);
    }

    private void renderer(final Entity entity) {
        if (entity instanceof EntityBlaze) {
            if (!blaze.isToggled()) {
                return;
            }
            this.renderMob(entity, Color.orange.getRGB());
        } else if (entity instanceof EntityCreeper) {
            if (!creeper.isToggled()) {
                return;
            }
            this.renderMob(entity, Color.green.getRGB());
        } else if (entity instanceof EntityEnderman) {
            if (!enderman.isToggled()) {
                return;
            }
            this.renderMob(entity, Color.black.getRGB());
        } else if (entity instanceof EntityGhast) {
            if (!ghast.isToggled()) {
                return;
            }
            this.renderMob(entity, -1);
        } else if (entity instanceof EntitySilverfish) {
            if (!silverfish.isToggled()) {
                return;
            }
            this.renderMob(entity, Color.gray.getRGB());
        } else if (entity instanceof EntitySkeleton) {
            if (!skeleton.isToggled()) {
                return;
            }
            this.renderMob(entity, -1);
        } else if (entity instanceof EntitySlime) {
            if (!slime.isToggled()) {
                return;
            }
            this.renderMob(entity, Color.green.getRGB());
        } else if (entity instanceof EntitySpider || entity instanceof EntityCaveSpider) {
            if (!spider.isToggled()) {
                return;
            }
            this.renderMob(entity, Color.black.getRGB());
        } else if (entity instanceof EntityPigZombie) {
            if (!zombiePigman.isToggled()) {
                return;
            }
            this.renderMob(entity, Color.pink.getRGB());
        } else if (entity instanceof EntityZombie) {
            if (!zombie.isToggled()) {
                return;
            }
            this.renderMob(entity, Color.blue.getRGB());
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent e) {
        if (!Utils.nullCheck()) {
            return;
        }
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity instanceof EntityLivingBase && entity != mc.thePlayer) {
                if (((EntityLivingBase) entity).deathTime != 0) {
                    continue;
                }
                this.renderer(entity);
            }
        }
    }
}
