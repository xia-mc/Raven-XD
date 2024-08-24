package keystrokesmod.module.impl.other;

import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.client.Notifications;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import silencefix.Messages;
import silencefix.SFIRCListener;
import silencefix.SilenceFixIRC;

public class SilenceIRC extends Module {
    public static String qqId = null;
    private static boolean started = false;

    public SilenceIRC() {
        super("SilenceIRC", category.other);

    }

    @Override
    public void onEnable() {
        if (!started) {
            SilenceFixIRC.init();
            SFIRCListener.init();
            started = true;
        }

        if (qqId == null) {
            Notifications.sendNotification(Notifications.NotificationTypes.WARN, "QQId doesn't be set!", 3000);
            this.disable();
        }

        try {
            SilenceFixIRC.Instance.connect();
            SilenceFixIRC.Instance.sendPacket(Messages.createVerify());
            SilenceFixIRC.Instance.sendPacket(Messages.createRequestEmailCode(qqId));
        } catch (Exception ignore) {
        }
    }

    @Override
    public void onDisable() throws Exception {
        SilenceFixIRC.Instance.shutdown();
    }

    @SubscribeEvent
    public void onSendPacket(@NotNull SendPacketEvent event) {
        if (event.getPacket() instanceof C01PacketChatMessage) {
            C01PacketChatMessage packet = (C01PacketChatMessage) event.getPacket();
            if (packet.getMessage().startsWith("#")) {
                event.setCanceled(true);
                SilenceFixIRC.Instance.sendPacket(Messages.createChat(packet.getMessage().substring(1)));
            }
        }
    }
}
