package keystrokesmod.module.impl.movement.fly;

import keystrokesmod.event.MoveEvent;
import keystrokesmod.event.PreVelocityEvent;
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
        event.setPitch(-85);
        event.setYaw(yaw + 180);
    }

    @Override
    public void onUpdate() {
        SlotHandler.setCurrentSlot(ContainerUtils.getSlot(ItemBow.class));
    }

    @SubscribeEvent
    public void onPreVelocity(@NotNull PreVelocityEvent event) {
        event.setCanceled(true);
        yaw = mc.thePlayer.rotationYaw;  // because we have set the rotation yaw on RotationEvent.
        mc.thePlayer.motionY = Math.abs(event.getMotionY() / 8000);
        MoveUtil.strafe(Math.hypot(event.getMotionX() / 8000.0, event.getMotionZ() / 8000.0));
    }

    @SubscribeEvent
    public void onMove(@NotNull MoveEvent event) {
        if (mc.thePlayer.hurtTime == 0) {
            event.setCanceled(true);
            mc.thePlayer.motionY = 0;
        }
    }

    @Override
    public void onEnable() {
        yaw = RotationHandler.getRotationYaw();
    }
}
