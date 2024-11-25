package keystrokesmod.module.impl.minigames;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoWho extends Module {
    private final DescriptionSetting description;
    private final DescriptionSetting nickDescription;
    private final ButtonSetting artifical;
    private final ButtonSetting hideMessage;
    private final ButtonSetting removeBots;

    public AutoWho() {
        super("AutoWho", category.minigames);
        this.registerSetting(description = new DescriptionSetting("Automatically execute /who."));
        this.registerSetting(nickDescription = new DescriptionSetting(Utils.formatColor("Use '&enick [nick]&r' when nicked.")));
        this.registerSetting(artifical = new ButtonSetting("Artificial", false));
        this.registerSetting(hideMessage = new ButtonSetting("Hide message", false));
        this.registerSetting(removeBots = new ButtonSetting("Remove bots", true));
    }

    @SubscribeEvent
    public void onChatReceive(ClientChatReceivedEvent e) {
        if (e.type == 2 || !Utils.nullCheck()) {
            return;
        }
        final String r = Utils.stripColor(e.message.getUnformattedText());
        if (r.isEmpty()) {
            return;
        }
        if (r.startsWith(Utils.getServerName() + " has joined (")) {
            this.artificial();
        } else if (hideMessage.isToggled() && r.startsWith("ONLINE: ")) {
            e.setCanceled(true);
            Utils.log.info("[CHAT] " + r);
        }
    }

    private void artificial() {
        if (artifical.isToggled()) {
            String online = hideMessage.isToggled() ? "ONLINE: " : "&b&lONLINE: &r";
            for (NetworkPlayerInfo networkPlayerInfo : Utils.getTablist()) {
                if (removeBots.isToggled() && networkPlayerInfo.getResponseTime() > 1) {
                    continue;
                }
                if (hideMessage.isToggled()) {
                    online = online + networkPlayerInfo.getGameProfile().getName() + ", ";
                } else {
                    online = online + ScorePlayerTeam.formatPlayerName(networkPlayerInfo.getPlayerTeam(), networkPlayerInfo.getGameProfile().getName()) + "�" + "7, ";
                }
            }
            if (hideMessage.isToggled()) {
                Utils.log.info("[CHAT] " + (online + mc.thePlayer.getName()));
                return;
            }
            Utils.sendRawMessage(online + mc.thePlayer.getDisplayName().getFormattedText());
        } else {
            mc.thePlayer.sendChatMessage("/who");
        }
    }
}
