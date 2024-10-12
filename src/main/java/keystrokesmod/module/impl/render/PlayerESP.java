package keystrokesmod.module.impl.render;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
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
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;

public class PlayerESP extends Module {
    public SliderSetting red;
    public SliderSetting green;
    public SliderSetting blue;
    public ButtonSetting colorByName;
    public ButtonSetting rainbow;
    private final ButtonSetting twoD;
    private final ButtonSetting arrow;
    private final ButtonSetting box;
    private final ButtonSetting health;
    public ButtonSetting outline;
    private final ButtonSetting shaded;
    private final ButtonSetting ring;
    private final SliderSetting expand;
    private final SliderSetting xShift;
    private final ButtonSetting redOnDamage;
    private final ButtonSetting showInvis;
    private int rgb = 0;

    private final Object2IntOpenHashMap<EntityLivingBase> targets = new Object2IntOpenHashMap<>(5);

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

    @Override
    public void onDisable() {
        RenderUtils.ring_c = false;
        targets.clear();
    }

    @Override
    public void onUpdate() {
        this.rgb = rainbow.isToggled()
                ? Utils.getChroma(2L, 0L)
                : new Color((int) red.getInput(), (int) green.getInput(), (int) blue.getInput()).getRGB();

        targets.clear();
        if (Utils.nullCheck()) {
            if (Raven.debugger) {
                mc.theWorld.loadedEntityList.parallelStream()
                        .filter(entity -> entity instanceof EntityLivingBase && entity != mc.thePlayer)
                        .forEach(entity -> {
                            if (colorByName.isToggled()) {
                                rgb = getColorFromTags(entity.getDisplayName().getFormattedText());
                            }
                            targets.put((EntityLivingBase) entity, rgb);
                        });
                return;
            }
            mc.theWorld.playerEntities.parallelStream()
                    .filter(player -> player != mc.thePlayer)
                    .filter(player -> player.deathTime == 0)
                    .filter(player -> showInvis.isToggled() || !player.isInvisible())
                    .filter(player -> !AntiBot.isBot(player))
                    .forEach(player -> {
                        if (colorByName.isToggled()) {
                            rgb = getColorFromTags(player.getDisplayName().getFormattedText());
                        }
                        targets.put(player, rgb);
                    });
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent e) {
        targets.forEach((target, color) -> {
            try {
                render(target, color);
            } catch (Exception ignored) {
            }
        });
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
