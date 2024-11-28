package keystrokesmod.module.impl.fun;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;

import java.util.Iterator;

public class SlyPort extends Module {
    public DescriptionSetting f;
    public SliderSetting r;
    public ButtonSetting b;
    public ButtonSetting playersOnly;
    public ButtonSetting e;

    public SlyPort() {
        super("SlyPort", Module.category.fun, 0);
        this.registerSetting(f = new DescriptionSetting("Teleport behind enemies."));
        this.registerSetting(r = new SliderSetting("Range", 6.0D, 2.0D, 15.0D, 1.0D));
        this.registerSetting(e = new ButtonSetting("Aim", true));
        this.registerSetting(b = new ButtonSetting("Play sound", true));
        this.registerSetting(playersOnly = new ButtonSetting("Players only", true));
    }

    public void onEnable() {
        Entity en = this.ge();
        if (en != null) {
            this.tp(en);
        }

        this.disable();
    }

    private void tp(Entity en) {
        if (b.isToggled()) {
            mc.thePlayer.playSound("mob.endermen.portal", 1.0F, 1.0F);
        }

        Vec3 vec = en.getLookVec();
        double x = en.posX - vec.xCoord * 2.5D;
        double z = en.posZ - vec.zCoord * 2.5D;
        mc.thePlayer.setPosition(x, mc.thePlayer.posY, z);
        if (e.isToggled()) {
            Utils.aim(en, 0.0F);
        }

    }

    private Entity ge() {
        Entity en = null;
        double r = Math.pow(this.r.getInput(), 2.0D);
        double dist = r + 1.0D;
        Iterator<Entity> var6 = mc.theWorld.loadedEntityList.iterator();

        while (true) {
            Entity ent;
            do {
                do {
                    do {
                        do {
                            if (!var6.hasNext()) {
                                return en;
                            }

                            ent = var6.next();
                        } while (ent == mc.thePlayer);
                    } while (!(ent instanceof EntityLivingBase));
                } while (((EntityLivingBase) ent).deathTime != 0);
            } while (this.playersOnly.isToggled() && !(ent instanceof EntityPlayer));

            if (!AntiBot.isBot(ent)) {
                double d = mc.thePlayer.getDistanceSqToEntity(ent);
                if (!(d > r) && !(dist < d)) {
                    dist = d;
                    en = ent;
                }
            }
        }
    }
}