package keystrokesmod.module.impl.movement.noslow;

import keystrokesmod.Raven;
import keystrokesmod.module.impl.movement.NoSlow;
import keystrokesmod.module.setting.impl.ButtonSetting;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import org.jetbrains.annotations.NotNull;

public class NCPNoSlow extends INoSlow {
    private final ButtonSetting oldHypixel;

    public NCPNoSlow(String name, @NotNull NoSlow parent) {
        super(name, parent);
        this.registerSetting(oldHypixel = new ButtonSetting("Old Hypixel", false));
    }

    @Override
    public void onUpdate() {
        if (!mc.thePlayer.isUsingItem()) return;
        if (mc.thePlayer.ticksExisted % 3 == 0 && !Raven.badPacketsHandler.C07) {
            mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 1, null, 0, 0, 0));
        }
    }

    @Override
    public float getSlowdown() {
        return oldHypixel.isToggled() ? .95f : 1;
    }
}