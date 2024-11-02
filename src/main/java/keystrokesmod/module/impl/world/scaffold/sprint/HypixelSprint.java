package keystrokesmod.module.impl.world.scaffold.sprint;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.impl.player.blink.NormalBlink;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.impl.world.scaffold.IScaffoldSprint;
import keystrokesmod.utility.MoveUtil;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class HypixelSprint extends IScaffoldSprint {
    private final NormalBlink blink = new NormalBlink("Blink", this) {
        @Override
        public void onSendPacket(@NotNull SendPacketEvent e) {
            if (e.getPacket() instanceof C0FPacketConfirmTransaction)
                return;
            super.onSendPacket(e);
        }
    };

    public HypixelSprint(String name, @NotNull Scaffold parent) {
        super(name, parent);
    }

    public void onDisable() {
        blink.disable();

        mc.thePlayer.motionX *= .8;
        mc.thePlayer.motionZ *= .8;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPreUpdate(PreMotionEvent event) {
        if (mc.thePlayer.onGround) {
            mc.thePlayer.motionX *= 1.114 - MoveUtil.getSpeedEffect() * .01 - Math.random() * 1E-4;
            mc.thePlayer.motionZ *= 1.114 - MoveUtil.getSpeedEffect() * .01 - Math.random() * 1E-4;
        }

        if (mc.thePlayer.onGround && mc.thePlayer.ticksExisted % 2 == 0) {
            blink.enable();
        } else {
            blink.disable();
        }
    }

    @Override
    public boolean isSprint() {
        return true;
    }
}
