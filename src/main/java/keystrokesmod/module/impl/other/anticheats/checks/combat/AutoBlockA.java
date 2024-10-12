package keystrokesmod.module.impl.other.anticheats.checks.combat;

import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.impl.other.Anticheat;
import keystrokesmod.module.impl.other.anticheats.Check;
import keystrokesmod.module.impl.other.anticheats.TRPlayer;
import keystrokesmod.module.impl.other.anticheats.config.AdvancedConfig;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class AutoBlockA extends Check {
    public AutoBlockA(@NotNull TRPlayer player) {
        super("AutoBlockA", player);
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S0BPacketAnimation) {
            if (((S0BPacketAnimation) event.getPacket()).getEntityID() == player.fabricPlayer.getEntityId()) {
                if (player.fabricPlayer.isBlocking())
                    flag("Impossible hit.");
            }
        }
    }

    @Override
    public int getAlertBuffer() {
        return AdvancedConfig.autoBlockAAlertBuffer;
    }

    @Override
    public boolean isDisabled() {
        return !Anticheat.getCombatCheck().isToggled();
    }
}
