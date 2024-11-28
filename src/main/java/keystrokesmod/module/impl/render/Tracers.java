package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.render.RenderUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.Iterator;

public class Tracers extends Module {
    public ButtonSetting a;
    public SliderSetting b;
    public SliderSetting c;
    public SliderSetting d;
    public ButtonSetting e;
    public SliderSetting f;
    public ButtonSetting distanceColor;
    private boolean g;
    private int rgb_c = 0;

    public Tracers() {
        super("Tracers", Module.category.render, 0);
        this.registerSetting(a = new ButtonSetting("Show invis", true));
        this.registerSetting(f = new SliderSetting("Line Width", 1.0D, 1.0D, 5.0D, 1.0D));
        this.registerSetting(b = new SliderSetting("Red", 0.0D, 0.0D, 255.0D, 1.0D));
        this.registerSetting(c = new SliderSetting("Green", 255.0D, 0.0D, 255.0D, 1.0D));
        this.registerSetting(d = new SliderSetting("Blue", 0.0D, 0.0D, 255.0D, 1.0D));
        this.registerSetting(e = new ButtonSetting("Rainbow", false));
        this.registerSetting(distanceColor = new ButtonSetting("Distance Color", false));
    }

    public void onEnable() {
        this.g = mc.gameSettings.viewBobbing;
        if (this.g) {
            mc.gameSettings.viewBobbing = false;
        }
    }

    public void onDisable() {
        mc.gameSettings.viewBobbing = this.g;
    }

    public void onUpdate() {
        if (mc.gameSettings.viewBobbing) {
            mc.gameSettings.viewBobbing = false;
        }
    }

    public void guiUpdate() {
        this.rgb_c = (new Color((int) b.getInput(), (int) c.getInput(), (int) d.getInput())).getRGB();
    }

    @SubscribeEvent
    public void o(RenderWorldLastEvent ev) {
        if (Utils.nullCheck()) {
            Iterator<EntityPlayer> var3 = mc.theWorld.playerEntities.iterator();
            while (var3.hasNext()) {
                EntityPlayer en = var3.next();
                if (en != mc.thePlayer && en.deathTime == 0 && (a.isToggled() || !en.isInvisible()) && !AntiBot.isBot(en)) {
                    int rgb;
                    if (distanceColor.isToggled()) {
                        double distance = mc.thePlayer.getDistanceToEntity(en);
                        rgb = getColorBasedOnDistance(distance);
                    } else {
                        rgb = e.isToggled() ? Utils.getChroma(2L, 0L) : this.rgb_c;
                    }
                    RenderUtils.dtl(en, rgb, (float) f.getInput());
                }
            }
        }
    }

    private int getColorBasedOnDistance(double distance) {
        float maxDistance = 40.0f;
        float ratio = (float) Math.min(distance / maxDistance, 1.0);
        int red = (int) (255 * ratio);
        int green = (int) (255 * (1 - ratio));
        return new Color(green, red, 0).getRGB();
    }
}