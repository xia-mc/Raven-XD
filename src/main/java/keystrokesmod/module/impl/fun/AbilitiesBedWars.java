package keystrokesmod.module.impl.fun;

import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.utility.Utils;
import net.minecraft.network.play.server.S3BPacketScoreboardObjective;
import net.minecraft.network.play.server.S3CPacketUpdateScore;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class AbilitiesBedWars extends Module {
    public AbilitiesBedWars() {
        super("AbilitiesBedWars", category.fun);
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S3BPacketScoreboardObjective) {
            S3BPacketScoreboardObjective packet = (S3BPacketScoreboardObjective) event.getPacket();

            Utils.sendMessage(String.format("objectiveName:%s, objectiveValue:%s, type:%s, field_149342_c:%s",
                    packet.func_149339_c(), packet.func_149337_d(), packet.func_179817_d().name(), packet.func_149338_e()
            ));
        } else if (event.getPacket() instanceof S3CPacketUpdateScore) {
            S3CPacketUpdateScore packet = (S3CPacketUpdateScore) event.getPacket();

            Utils.sendMessage(String.format("objectiveName:%s, playerName:%s, action:%s, scoreValue:%s",
                    packet.getObjectiveName(), packet.getPlayerName(), packet.getScoreAction().name(), packet.getScoreValue()
            ));
        }
        // WTFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF
    }
}
