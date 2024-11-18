package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ModeSetting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class FullBright extends Module {
    private static final float brightness = 15.0f;
    private final ModeSetting mode;
    private float originalGamma;
    private boolean nightVisionEnabled = false;

    private int lastMode = -1;

    public FullBright() {
        super("FullBright", Module.category.render, 0);
        this.registerSetting(mode = new ModeSetting("Mode", new String[]{"Night Vision", "Gamma"}, 0));
    }

    @Override
    public void onUpdate() {
        if (lastMode != (int) mode.getInput()) {
            switch ((int) mode.getInput()) {
                case 0:
                    disableGamma();
                    enableNightVision();
                    break;
                case 1:
                    disableNightVision();
                    enableGamma();
                    break;
            }
        }
        lastMode = (int) mode.getInput();
    }

    @Override
    public void onDisable() {
        disableGamma();
        disableNightVision();
        lastMode = -1;
    }

    private void enableGamma() {
        originalGamma = mc.gameSettings.gammaSetting;
        mc.gameSettings.gammaSetting = MathHelper.clamp_float((float) (originalGamma + 0.1), 0.0F, brightness);
    }

    private void disableGamma() {
        mc.gameSettings.gammaSetting = originalGamma;
    }

    private void enableNightVision() {
        EntityPlayer player = mc.thePlayer;
        if (player != null) {
            player.addPotionEffect(new PotionEffect(Potion.nightVision.getId(), 999999, 1, false, false));
            nightVisionEnabled = true;
        }
    }

    private void disableNightVision() {
        if (nightVisionEnabled) {
            EntityPlayer player = mc.thePlayer;
            if (player != null) {
                player.removePotionEffect(Potion.nightVision.getId());
            }
            nightVisionEnabled = false;
        }
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event) {
        if (mode.getInput() == 1) {
            if (mc.gameSettings.gammaSetting < brightness) {
                mc.gameSettings.gammaSetting = (float) Math.min(mc.gameSettings.gammaSetting + 0.1, brightness);
            }
        }
    }
}
