package keystrokesmod.module.impl.other;

import keystrokesmod.Raven;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class AutoPlay extends Module {
    private final ModeSetting mode;
    private final SliderSetting delay;
    private static final String SkywarsWinMessage = "You won! Want to play again? Click here!";
    private static final String SkywarsLoseMessage = "You died! Want to play again? Click here!";
    private static final String BedwarsWinMessage = "This game has been recorded. Click here to watch the Replay!";
    private static final String BedwarsLoseMessage = "You have been eliminated!";
    private static final String DuelWinMessage = "YOU WON! Want to play again? CLICK HERE!";

    public AutoPlay() {
        super("AutoPlay", category.other);
        this.registerSetting(new DescriptionSetting("Auto takes you to the next game."));
        this.registerSetting(mode = new ModeSetting("Mode", new String[]{"Skywars Solo Normal", "Skywars Solo Insane", "Skywars Teams Normal", "Skywars Teams Insane", "Bedwars Solo", "Bedwars Doubles", "Bedwars 3v3v3v3", "Bedwars 4v4v4v4", "Bedwars 4v4"}, 0));
        this.registerSetting(delay = new SliderSetting("Delay", 1.5, 0.5, 5, 0.5, "s"));
    }

    @SubscribeEvent
    public void onReceive(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S02PacketChat) {
            S02PacketChat packet = (S02PacketChat)event.getPacket();
            String message = packet.getChatComponent().getUnformattedText();
            if (message.contains(SkywarsWinMessage) && message.length() < SkywarsWinMessage.length() + 3
                    || message.contains(SkywarsLoseMessage) && message.length() < SkywarsLoseMessage.length() + 3
					|| message.contains(BedwarsWinMessage) && message.length() < BedwarsWinMessage.length() + 3
					|| message.contains(BedwarsLoseMessage) && message.length() < BedwarsLoseMessage.length() + 3) {
                Utils.sendModuleMessage(this, "Sending you to a new game.");

                Raven.getExecutor().schedule(() -> {
                    if (!ModuleManager.autoPlay.isEnabled()) return;

                    String command = "";
                    switch ((int) this.mode.getInput()) { // list of commands comes from https://hypixel.net/threads/guide-play-commands-useful-tools-mods-more-new-pixel-party-play-command.1025608/
						case 0:
                            command = "/play solo_normal";
                            break;
                        case 1:
                            command = "/play solo_insane";
                            break;
						case 2:
							command = "/play teams_normal";
							break;
						case 3:
							command = "/play teams_insane";
							break;
						case 4:
							command = "/play bedwars_eight_one";
							break;
						case 5:
							command = "/play bedwars_eight_two";
							break;
						case 6:
							command = "/play bedwars_four_three";
							break;
						case 7:
							command = "/play bedwars_four_four";
							break;
						case 8:
							command = "/play bedwars_two_four";
							break;
						default:
                            command = "/play solo_normal";
							break;
                    }
                    mc.thePlayer.sendChatMessage(command);
                }, (long) (delay.getInput() * 1000), TimeUnit.MILLISECONDS);
            }
        }

    }
}
