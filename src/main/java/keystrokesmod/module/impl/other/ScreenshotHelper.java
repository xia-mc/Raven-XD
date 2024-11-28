package keystrokesmod.module.impl.other;

import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScreenshotHelper extends Module {
    private static final String SkywarsWinMessage = "You won! Want to play again? Click here!";
    private static final String BedwarsWinMessage = "This game has been recorded. Click here to watch the Replay!";
    private static final String DuelWinMessage = "YOU WON! Want to play again? CLICK HERE!";

    private final SliderSetting delay;
    //    private static final ButtonSetting AntiLag = new ButtonSetting("Anti Lag", true); // Unfinished, but you can use the Essential mod to take Anti-Lag screenshots.
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public ScreenshotHelper() {
        super("ScreenshotHelper", category.other);
        this.registerSetting(new DescriptionSetting("Auto Screenshot on win."));
//        this.registerSetting(AntiLag);
        this.registerSetting(delay = new SliderSetting("Delay", 0.25, 0.1, 3, 0.05, "s"));
    }

    @SubscribeEvent
    public void onReceive(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S02PacketChat) {
            S02PacketChat packet = (S02PacketChat) event.getPacket();
            String message = packet.getChatComponent().getUnformattedText();
            if (message.contains(SkywarsWinMessage) && message.length() < SkywarsWinMessage.length() + 3
                    || message.contains(BedwarsWinMessage) && message.length() < BedwarsWinMessage.length() + 3
                    || message.contains(DuelWinMessage) && message.length() < DuelWinMessage.length() + 3) {
                scheduleScreenshot();
            }
        }
    }

    private void scheduleScreenshot() {
        long delaySeconds = (long) delay.getInput();
        scheduler.schedule(this::takeScreenshot, delaySeconds, TimeUnit.SECONDS);
    }

    private void takeScreenshot() {
        Minecraft mc = Minecraft.getMinecraft();
        mc.addScheduledTask(() -> {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date());
            File screenshotFile = new File(mc.mcDataDir, timestamp + ".png");

            mc.ingameGUI.getChatGUI().printChatMessage(new ChatComponentText("§5[ScreenshotHelper] §aScreenshot has been saved."));
            net.minecraft.util.ScreenShotHelper.saveScreenshot(screenshotFile.getParentFile(), screenshotFile.getName(), mc.displayWidth, mc.displayHeight, mc.getFramebuffer());
        });
    }
}
