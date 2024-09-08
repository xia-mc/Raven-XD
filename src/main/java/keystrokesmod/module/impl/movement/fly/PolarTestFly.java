package keystrokesmod.module.impl.movement.fly;

import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.impl.client.Notifications;
import keystrokesmod.module.impl.movement.Fly;
import keystrokesmod.module.impl.movement.motionmodifier.SimpleMotionModifier;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeValue;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PolarTestFly extends SubMode<Fly> {
    private final ModeValue editMotion;
    private final ButtonSetting clearMotion;

    private final Queue<Packet<INetHandlerPlayClient>> delayedPackets = new ConcurrentLinkedQueue<>();
    private boolean delayed = false;

    public PolarTestFly(String name, @NotNull Fly parent) {
        super(name, parent);
        this.registerSetting(editMotion = new ModeValue("Edit motion", this, () -> false)
                .add(new SimpleMotionModifier("MotionModifier", this))
        );
        this.registerSetting(clearMotion = new ButtonSetting("Clear motion", true));
    }

    @Override
    public void onDisable() throws Throwable {
        editMotion.disable();
        if (Utils.nullCheck()) {
            delayedPackets.forEach(PacketUtils::receivePacketNoEvent);
        }

        delayedPackets.clear();
        delayed = false;
        Utils.resetTimer();
        if (clearMotion.isToggled())
            mc.thePlayer.motionX = mc.thePlayer.motionY = mc.thePlayer.motionZ = 0;
    }

    @Override
    public void onEnable() throws Throwable {
        Utils.getTimer().timerSpeed = 0.1f;
        editMotion.enable();
        delayed = true;
        ((SimpleMotionModifier) editMotion.getSelected()).update();
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (delayed) {
            if (event.getPacket() instanceof S32PacketConfirmTransaction) {
                delayedPackets.add((Packet<INetHandlerPlayClient>) event.getPacket());
                event.setCanceled(true);
                Utils.sendMessage("Delayed " + delayedPackets.size());
            }
        }
    }

    @Override
    public void onUpdate() throws Throwable {
        ((SimpleMotionModifier) editMotion.getSelected()).update();
    }
}
