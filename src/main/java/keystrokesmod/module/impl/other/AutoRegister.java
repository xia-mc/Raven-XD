package keystrokesmod.module.impl.other;

import keystrokesmod.Raven;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.client.Notifications;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

public class AutoRegister extends Module {
    public static final char[] CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    private final ModeSetting message;
    private final SliderSetting minDelay;
    private final SliderSetting maxDelay;

    public AutoRegister() {
        super("AutoRegister", category.other);
        this.registerSetting(message = new ModeSetting("Message", new String[]{"/register <p>", "/register <p> <p>"}, 0));
        this.registerSetting(minDelay = new SliderSetting("Min delay", 1500, 0, 5000, 500));
        this.registerSetting(maxDelay = new SliderSetting("Max delay", 3000, 0, 5000, 500));
    }

    public static @NotNull String generatePassword(@NotNull String gameId) {
        final MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            Utils.sendMessage("Your environment doesn't support this.");
            throw new RuntimeException(e);
        }
        byte[] hashBytes = digest.digest(gameId.getBytes());

        final StringBuilder passwordBuilder = new StringBuilder();

        for (byte b : hashBytes) {
            int value = b & 0xFF;
            char selectedChar = CHARS[value % CHARS.length];
            passwordBuilder.append(selectedChar);

            if (passwordBuilder.length() >= 12) {
                break;
            }
        }

        return passwordBuilder.toString();
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S02PacketChat) {
            final String text = ((S02PacketChat) event.getPacket()).getChatComponent().getUnformattedText();

            if (text.contains("/register")) {
                handle(true);
            } else if (text.contains("/login")) {
                handle(false);
            }
        }
    }

    private void handle(boolean register) {
        final long time = Utils.randomizeInt(minDelay.getInput(), maxDelay.getInput());
        Notifications.sendNotification(
                Notifications.NotificationTypes.INFO,
                (register ? "Registering" : "Logging in") + "... (" + time + "ms)"
        );

        Raven.getExecutor().schedule(() -> {
            final String pwd = generatePassword(mc.thePlayer.getName());
            final String text;
            if (register) {
                text = message.getOptions()[(int) message.getInput()].replace("<p>", pwd);
            } else {
                text = "/login " + pwd;
            }
            PacketUtils.sendPacket(new C01PacketChatMessage(text));
        }, time, TimeUnit.MILLISECONDS);
    }
}
