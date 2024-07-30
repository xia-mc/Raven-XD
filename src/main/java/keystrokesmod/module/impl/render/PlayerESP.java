package keystrokesmod.module.impl.render;

import keystrokesmod.Raven;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.render.ColorUtils;
import keystrokesmod.utility.render.RenderUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class PlayerESP extends Module {
    public SliderSetting red;
    public SliderSetting green;
    public SliderSetting blue;
    public ButtonSetting colorByName;
    public ButtonSetting rainbow;
    private ButtonSetting twoD;
    private ButtonSetting arrow;
    private ButtonSetting box;
    private ButtonSetting health;
    public ButtonSetting outline;
    private ButtonSetting shaded;
    private ButtonSetting ring;
    private SliderSetting expand;
    private SliderSetting xShift;
    private ButtonSetting redOnDamage;
    private ButtonSetting showInvis;
    private int rgb_c = 0;

    public PlayerESP() {
        super("PlayerESP", category.render, 0);
        this.registerSetting(red = new SliderSetting("Red", 0.0D, 0.0D, 255.0D, 1.0D));
        this.registerSetting(green = new SliderSetting("Green", 255.0D, 0.0D, 255.0D, 1.0D));
        this.registerSetting(blue = new SliderSetting("Blue", 0.0D, 0.0D, 255.0D, 1.0D));
        this.registerSetting(rainbow = new ButtonSetting("Rainbow", false));
        this.registerSetting(colorByName = new ButtonSetting("Color by name", false));
        this.registerSetting(new DescriptionSetting("ESP Types"));
        this.registerSetting(twoD = new ButtonSetting("2D", false));
        this.registerSetting(arrow = new ButtonSetting("Arrow", false));
        this.registerSetting(box = new ButtonSetting("Box", false));
        this.registerSetting(health = new ButtonSetting("Health", true));
        this.registerSetting(outline = new ButtonSetting("Outline", false));
        this.registerSetting(ring = new ButtonSetting("Ring", false));
        this.registerSetting(shaded = new ButtonSetting("Shaded", false));
        this.registerSetting(expand = new SliderSetting("Expand", 0.0D, -0.3D, 2.0D, 0.1D));
        this.registerSetting(xShift = new SliderSetting("X-Shift", 0.0D, -35.0D, 10.0D, 1.0D));
        this.registerSetting(redOnDamage = new ButtonSetting("Red on damage", true));
        this.registerSetting(showInvis = new ButtonSetting("Show invis", true));
    }

    public void onDisable() {
        RenderUtils.ring_c = false;
    }

    public void guiUpdate() {
        this.rgb_c = (new Color((int) red.getInput(), (int) green.getInput(), (int) blue.getInput())).getRGB();
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent e) {
        if (Utils.nullCheck()) {
            int rgb = rainbow.isToggled() ? Utils.getChroma(2L, 0L) : this.rgb_c;
            if (Raven.debugger) {
                for (final Entity entity : mc.theWorld.loadedEntityList) {
                    if (entity instanceof EntityLivingBase && entity != mc.thePlayer) {
                        if (colorByName.isToggled()) {
                            rgb = getColorFromTags(entity.getDisplayName().getFormattedText());
                        }
                        this.render(entity, rgb);
                    }
                }
                return;
            }
            for (EntityPlayer player : mc.theWorld.playerEntities) {
                if (player != mc.thePlayer) {
                    if (player.deathTime != 0) {
                        continue;
                    }
                    if (!showInvis.isToggled() && player.isInvisible()) {
                        continue;
                    }
                    if (AntiBot.isBot(player)) {
                        continue;
                    }
                    if (colorByName.isToggled()) {
                        rgb = getColorFromTags(player.getDisplayName().getFormattedText());
                    }
                    this.render(player, rgb);
                }
            }
        }
    }

    private void render(Entity en, int rgb) {
        if (box.isToggled()) {
            RenderUtils.renderEntity(en, 1, expand.getInput(), xShift.getInput(), rgb, redOnDamage.isToggled());
        }

        if (shaded.isToggled()) {
            if (ModuleManager.murderMystery == null || !ModuleManager.murderMystery.isEnabled() || ModuleManager.murderMystery.isEmpty()) {
                RenderUtils.renderEntity(en, 2, expand.getInput(), xShift.getInput(), rgb, redOnDamage.isToggled());
            }
        }

        if (twoD.isToggled()) {
            RenderUtils.renderEntity(en, 3, expand.getInput(), xShift.getInput(), rgb, redOnDamage.isToggled());
        }

        if (health.isToggled()) {
            RenderUtils.renderEntity(en, 4, expand.getInput(), xShift.getInput(), rgb, redOnDamage.isToggled());
        }

        if (arrow.isToggled()) {
            RenderUtils.renderEntity(en, 5, expand.getInput(), xShift.getInput(), rgb, redOnDamage.isToggled());
        }

        if (ring.isToggled()) {
            RenderUtils.renderEntity(en, 6, expand.getInput(), xShift.getInput(), rgb, redOnDamage.isToggled());
        }
    }

    public static int getColorFromTags(String displayName) {
        displayName = Utils.removeFormatCodes(displayName);
        if (displayName.isEmpty() || !displayName.startsWith("§") || displayName.charAt(1) == 'f') {
            return new Color(255, 255, 255).getRGB();
        }
        return ColorUtils.getColorFromCode(displayName).getRGB();
    }
}
