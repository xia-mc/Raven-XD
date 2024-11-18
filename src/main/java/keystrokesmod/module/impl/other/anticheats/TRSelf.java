package keystrokesmod.module.impl.other.anticheats;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class TRSelf extends TRPlayer {
    private static TRSelf instance = null;
    public EntityPlayerSP fabricPlayer;
    public @NotNull ScheduledExecutorService timeTask = Executors.newScheduledThreadPool(8);
    public TRSelf(@NotNull EntityPlayerSP player) {
        super(player, true);
        this.fabricPlayer = player;
        instance = this;
    }

    public static TRSelf getInstance() {
        return instance;
    }

    public static void onDisconnect() {
        instance = null;
    }

    @Override
    public void update(AbstractClientPlayer player) {
        if (player instanceof EntityPlayerSP) {
            this.fabricPlayer = (EntityPlayerSP) player;
        } else throw new RuntimeException("Trying update TRSelf with non-local player!");

        super.update(player);
    }
}
