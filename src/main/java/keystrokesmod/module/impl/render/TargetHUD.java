package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.combat.KillAura;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.RenderUtils;
import keystrokesmod.utility.Theme;
import keystrokesmod.utility.Timer;
import keystrokesmod.utility.Utils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;

import static keystrokesmod.utility.RenderUtils.jelloRender;

public class TargetHUD extends Module {
    private final DescriptionSetting description;
    private final SliderSetting theme;
    private final ButtonSetting renderEsp;
    private final ButtonSetting showStatus;
    private Timer fadeTimer;
    private Timer healthBarTimer = null;
    private EntityLivingBase target;
    private long lastAliveMS;
    private double lastHealth;
    private float lastHealthBar;

    public TargetHUD() {
        super("TargetHUD", category.render);
        this.registerSetting(description = new DescriptionSetting("Only works with KillAura."));
        this.registerSetting(theme = new SliderSetting("Theme", Theme.themes, 0));
        this.registerSetting(renderEsp = new ButtonSetting("Render ESP", true));
        this.registerSetting(showStatus = new ButtonSetting("Show win or loss", true));
    }

    public void onDisable() {
        reset();
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
                (healthBarTimer = new Timer(400)).start();
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
            EntityLivingBase target = KillAura.target;

            Color color = new Color(255, 255, 255, 128);

            jelloRender(target, target, color);
        }
    }

    private void drawTargetHUD(Timer cd, String string, double n) {
        if (showStatus.isToggled()) {
            string = string + " " + ((n <= Utils.getCompleteHealth(mc.thePlayer) / mc.thePlayer.getMaxHealth()) ? "§aW" : "§cL");
        }
        final ScaledResolution scaledResolution = new ScaledResolution(mc);
        final int n2 = 8;
        final int n3 = mc.fontRendererObj.getStringWidth(string) + n2;
        final int n4 = scaledResolution.getScaledWidth() / 2 - n3 / 2 + 70;
        final int n5 = scaledResolution.getScaledHeight() / 2 + 15 + 30;
        final int n6 = n4 - n2;
        final int n7 = n5 - n2;
        final int n8 = n4 + n3;
        final int n9 = n5 + (mc.fontRendererObj.FONT_HEIGHT + 5) - 6 + n2;
        final int n10 = (cd == null) ? 255 : (255 - cd.getValueInt(0, 255, 1));
        if (n10 > 0) {
            final int n11 = (n10 > 110) ? 110 : n10;
            final int n12 = (n10 > 210) ? 210 : n10;
            final int[] array = Theme.getGradients((int) theme.getInput());
            RenderUtils.drawRoundedGradientOutlinedRectangle((float) n6, (float) n7, (float) n8, (float) (n9 + 13), 10.0f, Utils.merge(Color.black.getRGB(), n11), Utils.merge(array[0], n10), Utils.merge(array[1], n10)); // outline
            final int n13 = n6 + 6;
            final int n14 = n8 - 6;
            final int n15 = n9;
            RenderUtils.drawRoundedRectangle((float) n13, (float) n15, (float) n14, (float) (n15 + 5), 4.0f, Utils.merge(Color.black.getRGB(), n11)); // background
            final int k = Utils.merge(array[0], n12);
            final int n16 = (n > 0.15) ? Utils.merge(array[1], n12) : k;
            float health = (float) (int) (n14 + (n13 - n14) * (1.0 - ((n < 0.05) ? 0.05 : n)));
            if (health - n13 < 3) { // if goes below, the rounded health bar glitches out
                health = n13 + 3;
            }
            if (health != lastHealthBar && lastHealthBar - n13 >= 3 && healthBarTimer != null ) {
                float diff = lastHealthBar - health;
                if (diff > 0) {
                    lastHealthBar = lastHealthBar - healthBarTimer.getValueFloat(0, diff, 1);
                }
                else {
                    lastHealthBar = healthBarTimer.getValueFloat(lastHealthBar, health, 1);
                }
            }
            else {
                lastHealthBar = health;
            }
            RenderUtils.drawRoundedGradientRectangle((float) n13, (float) n15, lastHealthBar, (float) (n15 + 5), 4.0f, k, k, n16, n16); // health bar
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
    }
}