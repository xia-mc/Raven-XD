package keystrokesmod.module.impl.other.anticheats.checks.combat;

import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.impl.other.Anticheat;
import keystrokesmod.module.impl.other.anticheats.Check;
import keystrokesmod.module.impl.other.anticheats.TRPlayer;
import keystrokesmod.module.impl.other.anticheats.config.AdvancedConfig;
import keystrokesmod.utility.RotationUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import static keystrokesmod.Raven.mc;

public class AutoBlockA extends Check {
    private boolean needToCheck = false;

    public AutoBlockA(@NotNull TRPlayer player) {
        super("AutoBlockA", player);
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S0BPacketAnimation) {
            if (((S0BPacketAnimation) event.getPacket()).getEntityID() == player.fabricPlayer.getEntityId()) {
                if (RotationUtils.rayCast(
                        Utils.getEyePos(player.fabricPlayer).toVec3(), 3,
                        player.fabricPlayer.rotationYaw, player.fabricPlayer.rotationPitch) == null
                ) {
                    needToCheck = false;
                    return;
                }

                needToCheck = true;
            }
        } else if (event.getPacket() instanceof S14PacketEntity) {
            if (((S14PacketEntity) event.getPacket()).getEntity(mc.theWorld) == player.fabricPlayer
                    && player.fabricPlayer.isBlocking() && needToCheck)
                flag("Impossible hit.");
        }
    }

    @Override
    public int getAlertBuffer() {
        return AdvancedConfig.autoBlockAAlertBuffer;
    }

    @Override
    public boolean isDisabled() {
        return !Anticheat.getCombatCheck().isToggled() || !Anticheat.getCombatCheckAutoBlockA().isToggled();
    }
}
