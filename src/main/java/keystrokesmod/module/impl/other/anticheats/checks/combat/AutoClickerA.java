package keystrokesmod.module.impl.other.anticheats.checks.combat;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.impl.other.Anticheat;
import keystrokesmod.module.impl.other.anticheats.Check;
import keystrokesmod.module.impl.other.anticheats.TRPlayer;
import net.minecraft.network.play.server.S0BPacketAnimation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class AutoClickerA extends Check {
    private final LongArrayFIFOQueue clicks = new LongArrayFIFOQueue(15);

    public AutoClickerA(@NotNull TRPlayer player) {
        super("AutoClickerA", player);
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S0BPacketAnimation) {
            if (((S0BPacketAnimation) event.getPacket()).getEntityID() == player.fabricPlayer.getEntityId()) {
                onClick();
            }
        }
    }

    private void onClick() {
        final long time = System.currentTimeMillis();
        if (clicks.size() >= 15) {
            final long lastClick = clicks.dequeueLong();
            final double lastCPS = (time - lastClick) / 1000.0 * 15;
            if (time - lastClick < 1000 && lastCPS >= 15) {
                flag(String.format("High cps: %.1f", lastCPS));
                clicks.clear();
            }
        }

        clicks.enqueue(time);
    }

    @Override
    public int getAlertBuffer() {
        return 2;
    }

    @Override
    public boolean isDisabled() {
        return !Anticheat.getCombatCheck().isToggled() || !Anticheat.getCombatCheckAutoClickerA().isToggled();
    }
}
