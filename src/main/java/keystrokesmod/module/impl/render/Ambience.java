package keystrokesmod.module.impl.render;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;

public class Ambience extends Module {
    private final SliderSetting time;
    private final SliderSetting speed;
    private final ModeSetting weather;
    private final ButtonSetting fogColor;
    private final SliderSetting fogColorRed;
    private final SliderSetting fogColorGreen;
    private final SliderSetting fogColorBlue;

    public Ambience() {
        super("Ambience", category.render);
        this.registerSetting(time = new SliderSetting("Time", 0, 0, 24000, 10));
        this.registerSetting(speed = new SliderSetting("Speed", 0, 0, 20, 1));

        String[] MODES = new String[]{"Unchanged", "Clear", "Rain"};
        this.registerSetting(weather = new ModeSetting("Weather", MODES, 0));

        this.registerSetting(fogColor = new ButtonSetting("Fog color", false));
        this.registerSetting(fogColorRed = new SliderSetting("Fog color red", 100, 0, 255, 1, fogColor::isToggled));
        this.registerSetting(fogColorGreen = new SliderSetting("Fog color green", 100, 0, 255, 1, fogColor::isToggled));
        this.registerSetting(fogColorBlue = new SliderSetting("Fog color blue", 100, 0, 255, 1, fogColor::isToggled));
    }

    @Override
    public void onDisable() {
        if (!Utils.nullCheck()) return;
        reset();
    }

    private void reset() {
        mc.theWorld.setRainStrength(0);
        mc.theWorld.getWorldInfo().setCleanWeatherTime(Integer.MAX_VALUE);
        mc.theWorld.getWorldInfo().setRainTime(0);
        mc.theWorld.getWorldInfo().setThunderTime(0);
        mc.theWorld.getWorldInfo().setRaining(false);
        mc.theWorld.getWorldInfo().setThundering(false);
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (!Utils.nullCheck()) return;
        if (mc.thePlayer.ticksExisted % 20 == 0) {

            switch ((int) this.weather.getInput()) {
                case 1:
                    reset();
                    break;
                case 2:
                    mc.theWorld.setRainStrength(1);
                    mc.theWorld.getWorldInfo().setCleanWeatherTime(0);
                    mc.theWorld.getWorldInfo().setRainTime(Integer.MAX_VALUE);
                    mc.theWorld.getWorldInfo().setThunderTime(Integer.MAX_VALUE);
                    mc.theWorld.getWorldInfo().setRaining(true);
                    mc.theWorld.getWorldInfo().setThundering(false);
                    break;
            }
        }
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S03PacketTimeUpdate) {
            event.setCanceled(true);
        } else if (event.getPacket() instanceof S2BPacketChangeGameState && this.weather.getInput() != 0) {
            S2BPacketChangeGameState s2b = (S2BPacketChangeGameState) event.getPacket();

            if (s2b.getGameState() == 1 || s2b.getGameState() == 2) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (!Utils.nullCheck() || event.phase != TickEvent.Phase.END) return;
        mc.theWorld.setWorldTime((long) (time.getInput() + (System.currentTimeMillis() * speed.getInput())));
    }

    @SubscribeEvent
    public void onFogColors(EntityViewRenderEvent.@NotNull FogColors event) {
        if (!fogColor.isToggled()) return;

        event.red = (float) fogColorRed.getInput();
        event.green = (float) fogColorGreen.getInput();
        event.blue = (float) fogColorBlue.getInput();
    }
}
