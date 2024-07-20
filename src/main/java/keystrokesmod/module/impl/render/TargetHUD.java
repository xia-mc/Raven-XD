package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.combat.KillAura;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.render.RenderUtils;
import keystrokesmod.utility.Theme;
import keystrokesmod.utility.Timer;
import keystrokesmod.utility.Utils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.awt.*;

import static keystrokesmod.utility.render.RenderUtils.renderMode;

public class TargetHUD extends Module {
    public static int posX = 70;
    public static int posY = 30;
    public static final Color jellocolor = new Color(255, 255, 255, 128);
    private static final ModeSetting mode = new ModeSetting("Mode", new String[]{"Raven"}, 0);
    private final ButtonSetting onlyKillAura;
    private final SliderSetting maxDistance;
    private static final ModeSetting theme = new ModeSetting("Theme", Theme.themes, 0);
    private final ButtonSetting renderEsp;
    private static final ModeSetting targetEspMode = new ModeSetting("Target ESP Mode", renderMode, 0);
    private static final ButtonSetting showStatus = new ButtonSetting("Show win or loss", true);
    private static final ButtonSetting healthColor = new ButtonSetting("Traditional health color", false);
    private Timer fadeTimer;
    private static Timer healthBarTimer = null;
    private static EntityLivingBase target;
    private long lastAliveMS;
    private double lastHealth;
    private static float lastHealthBar;
    public EntityLivingBase renderEntity;

    public static int current$minX;
    public static int current$maxX;
    public static int current$minY;
    public static int current$maxY;

    public TargetHUD() {
        super("TargetHUD", category.render);
        this.registerSetting(mode);
        this.registerSetting(onlyKillAura = new ButtonSetting("Only KillAura", true));
        this.registerSetting(maxDistance = new SliderSetting("Max distance", 6, 3, 20, 1, "blocks", () -> !onlyKillAura.isToggled()));
        this.registerSetting(theme);
        this.registerSetting(renderEsp = new ButtonSetting("Render ESP", true));
        this.registerSetting(targetEspMode);
        this.registerSetting(showStatus);
        this.registerSetting(healthColor);
    }

    public void onDisable() {
        reset();
    }

    @SubscribeEvent
    public void onAttack(@NotNull AttackEntityEvent event) {
        if (!onlyKillAura.isToggled() && event.target instanceof EntityLivingBase)
            renderEntity = (EntityLivingBase) event.target;
    }

    @Override
    public void onUpdate() {
        if (renderEntity != null) {
            if (new Vec3(renderEntity).distanceTo(mc.thePlayer) > maxDistance.getInput())
                renderEntity = null;
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent ev) {
        if (!Utils.nullCheck()) {
            reset();
            return;
        }
        if (ev.phase == TickEvent.Phase.END) {
            if (mc.currentScreen != null) {
                reset();
                return;
            }
            if (KillAura.target != null) {
                target = KillAura.target;
                lastAliveMS = System.currentTimeMillis();
                fadeTimer = null;
            } else if (renderEntity != null) {
                target = renderEntity;
                lastAliveMS = System.currentTimeMillis();
                fadeTimer = null;
            } else if (target != null) {
                if (System.currentTimeMillis() - lastAliveMS >= 200 && fadeTimer == null) {
                    (fadeTimer = new Timer(400)).start();
                }
            }
            else {
                return;
            }
            String playerInfo = target.getDisplayName().getFormattedText();
            double health = target.getHealth() / target.getMaxHealth();
            if (health != lastHealth) {
                (healthBarTimer = new Timer(350)).start();
            }
            lastHealth = health;
            playerInfo += " " + Utils.getHealthStr(target);
            drawTargetHUD(fadeTimer, playerInfo, health);
        }
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent renderWorldLastEvent) {
        if (!renderEsp.isToggled() || !Utils.nullCheck()) {
            return;
        }
        if (KillAura.target != null) {
            renderTarget(KillAura.target);
        } else if (renderEntity != null) {
            renderTarget(renderEntity);
        }
    }

    private void renderTarget(EntityLivingBase target) {
        int modeIndex = (int) targetEspMode.getInput();
        String mode = renderMode[modeIndex];

        switch (mode) {
            case "Default":
                RenderUtils.renderEntity(target, 2, 0.0, 0.0, Theme.getGradient((int) theme.getInput(), 0), false);
                break;
            case "Jello":
                RenderUtils.jelloRender(target, target, jellocolor);
                break;
        }
    }

    public static void drawTargetHUD(Timer cd, String string, @Range(from = 0, to = 1) double health) {
        switch ((int) mode.getInput()) {
            case 0:
                drawRavenTargetHUD(cd, string, health);
                break;
        }
    }

    private static void drawRavenTargetHUD(Timer cd, String string, double health) {
        if (showStatus.isToggled()) {
            string = string + " " + ((health <= Utils.getCompleteHealth(mc.thePlayer) / mc.thePlayer.getMaxHealth()) ? "§aW" : "§cL");
        }
        final ScaledResolution scaledResolution = new ScaledResolution(mc);
        final int n2 = 8;
        final int n3 = mc.fontRendererObj.getStringWidth(string) + n2;
        final int n4 = scaledResolution.getScaledWidth() / 2 - n3 / 2 + posX;
        final int n5 = scaledResolution.getScaledHeight() / 2 + 15 + posY;
        current$minX = n4 - n2;  // minX
        current$minY = n5 - n2;  // minY
        current$maxX = n4 + n3;  // maxX
        current$maxY = n5 + (mc.fontRendererObj.FONT_HEIGHT + 5) - 6 + n2;  // maxY
        final int n10 = (cd == null) ? 255 : (255 - cd.getValueInt(0, 255, 1));
        if (n10 > 0) {
            final int n11 = (n10 > 110) ? 110 : n10;
            final int n12 = (n10 > 210) ? 210 : n10;
            final int[] array = Theme.getGradients((int) theme.getInput());
            RenderUtils.drawRoundedGradientOutlinedRectangle((float) current$minX, (float) current$minY, (float) current$maxX, (float) (current$maxY + 13), 10.0f, Utils.merge(Color.black.getRGB(), n11), Utils.merge(array[0], n10), Utils.merge(array[1], n10)); // outline
            final int n13 = current$minX + 6;
            final int n14 = current$maxX - 6;
            final int n15 = current$maxY;
            RenderUtils.drawRoundedRectangle((float) n13, (float) n15, (float) n14, (float) (n15 + 5), 4.0f, Utils.merge(Color.black.getRGB(), n11)); // background
            int k = Utils.merge(array[0], n12);
            int n16 = Utils.merge(array[1], n12);
            float healthBar = (float) (int) (n14 + (n13 - n14) * (1.0 - ((health < 0.05) ? 0.05 : health)));
            if (healthBar - n13 < 3) { // if goes below, the rounded health bar glitches out
                healthBar = n13 + 3;
            }
            if (healthBar != lastHealthBar && lastHealthBar - n13 >= 3 && healthBarTimer != null ) {
                float diff = lastHealthBar - healthBar;
                if (diff > 0) {
                    lastHealthBar = lastHealthBar - healthBarTimer.getValueFloat(0, diff, 1);
                }
                else {
                    lastHealthBar = healthBarTimer.getValueFloat(lastHealthBar, healthBar, 1);
                }
            }
            else {
                lastHealthBar = healthBar;
            }
            if (healthColor.isToggled()) {
                k = n16 = Utils.merge(Utils.getColorForHealth(health), n12);
            }
            RenderUtils.drawRoundedGradientRect((float) n13, (float) n15, lastHealthBar, (float) (n15 + 5), 4.0f, k, k, k, n16); // health bar
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            mc.fontRendererObj.drawString(string, (float) n4, (float) n5, (new Color(220, 220, 220, 255).getRGB() & 0xFFFFFF) | Utils.clamp(n10 + 15) << 24, true);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
        else {
            target = null;
            healthBarTimer = null;
        }
    }

    private void reset() {
        fadeTimer = null;
        target = null;
        healthBarTimer = null;
        renderEntity = null;
    }
}
