package keystrokesmod.utility;

import keystrokesmod.event.WorldChangeEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Ping {
    private static boolean e = false;
    private static long s = 0L;

    @SubscribeEvent
    public void onChatMessageRecieved(ClientChatReceivedEvent event) {
        if (e && Utils.nullCheck()) {
            if (Utils.stripColor(event.message.getUnformattedText()).startsWith("Unknown")) {
                event.setCanceled(true);
                e = false;
                this.getPing();
            }
        }
    }

    public static void checkPing() {
        Commands.print("§3Checking...", 1);
        if (e) {
            Commands.print("§cPlease wait.", 0);
        } else {
            Utils.mc.thePlayer.sendChatMessage("/...");
            e = true;
            s = System.currentTimeMillis();
        }
    }

    private void getPing() {
        int ping = (int) (System.currentTimeMillis() - s) - 20;
        if (ping < 0) {
            ping = 0;
        }

        Commands.print("Your ping: " + ping + "ms", 0);
        rs();
    }

    public static void rs() {
        e = false;
        s = 0L;
    }

    @SubscribeEvent
    public void onWorldChange(WorldChangeEvent event) {
        e = false;
    }
}
