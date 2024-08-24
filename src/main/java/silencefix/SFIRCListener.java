package silencefix;

import keystrokesmod.Raven;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.concurrent.TimeUnit;

import static keystrokesmod.Raven.mc;
import static silencefix.SilenceFixIRC.Instance;

public class SFIRCListener {

    public static void init() {
        Raven.getExecutor().scheduleWithFixedDelay(() -> {
            if (Instance.ircUser == null) return;
            Instance.sendPacket(Messages.createSetMinecraftProfile(getUUID()));
            for (Entity entity : mc.theWorld.loadedEntityList) {
                if (entity instanceof EntityPlayer) {
                    Instance.sendPacket(Messages.createQueryPlayer(entity.getUniqueID().toString(), 0));
                }
            }
        }, 0, 2000, TimeUnit.MILLISECONDS);
    }

    private static String getUUID() {
        return mc.thePlayer == null ? mc.getSession().getPlayerID() : mc.thePlayer.getUniqueID().toString();
    }
}
