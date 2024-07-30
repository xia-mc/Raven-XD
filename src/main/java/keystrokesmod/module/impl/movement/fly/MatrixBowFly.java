package keystrokesmod.module.impl.movement.fly;

import keystrokesmod.event.MoveEvent;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.event.RotationEvent;
import keystrokesmod.module.impl.movement.Fly;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.ContainerUtils;
import keystrokesmod.utility.MoveUtil;
import net.minecraft.item.ItemBow;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class MatrixBowFly extends SubMode<Fly> {
    private float yaw;

    public MatrixBowFly(String name, @NotNull Fly parent) {
        super(name, parent);
    }

    @SubscribeEvent
    public void onRotation(@NotNull RotationEvent event) {
        event.setPitch(-90);
        event.setYaw(yaw);
    }

    @Override
    public void onUpdate() {
        SlotHandler.setCurrentSlot(ContainerUtils.getSlot(ItemBow.class));
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S12PacketEntityVelocity) {
            final S12PacketEntityVelocity packet = (S12PacketEntityVelocity) event.getPacket();
            if (packet.getEntityID() == mc.thePlayer.getEntityId()) {
                event.setCanceled(true);
                yaw = mc.thePlayer.rotationYaw;  // because we have set the rotation yaw on RotationEvent.
                MoveUtil.strafe(Math.hypot(packet.getMotionX(), packet.getMotionZ()));
                mc.thePlayer.motionY = Math.abs(packet.getMotionY());
            }
        }
    }

    @SubscribeEvent
    public void onMove(@NotNull MoveEvent event) {
        if (mc.thePlayer.hurtTime < 3) {
            event.setCanceled(true);
        }
    }

    @Override
    public void onEnable() {
        yaw = RotationHandler.getRotationYaw();
    }
}
