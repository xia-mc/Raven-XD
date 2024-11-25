package keystrokesmod.module.impl.movement.fly;

import keystrokesmod.event.MoveEvent;
import keystrokesmod.event.PreVelocityEvent;
import keystrokesmod.event.RotationEvent;
import keystrokesmod.module.impl.movement.Fly;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.ContainerUtils;
import keystrokesmod.utility.MoveUtil;
import net.minecraft.item.ItemBow;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class MatrixBowFly extends SubMode<Fly> {
    private int ticksSinceVelocity = 999;
    private float yaw;

    public MatrixBowFly(String name, @NotNull Fly parent) {
        super(name, parent);
    }

    @SubscribeEvent
    public void onRotation(@NotNull RotationEvent event) {
        event.setPitch(-89);
        event.setYaw(yaw);
    }

    @Override
    public void onEnable() throws Exception {
        yaw = mc.thePlayer.rotationYaw;
    }

    @Override
    public void onUpdate() {
        SlotHandler.setCurrentSlot(ContainerUtils.getSlot(ItemBow.class));
        ticksSinceVelocity++;
    }

    @SubscribeEvent
    public void onPreVelocity(@NotNull PreVelocityEvent event) {
        ticksSinceVelocity = 0;
        mc.thePlayer.motionY = Math.abs(event.getMotionY() / 8000.0);
        MoveUtil.strafe(Math.hypot(event.getMotionX() / 8000.0, event.getMotionZ() / 8000.0));
        yaw = mc.thePlayer.rotationYaw;
        event.setCanceled(true);
    }

    @SubscribeEvent
    public void onMove(@NotNull MoveEvent event) {
        if (ticksSinceVelocity >= 6) {
            event.setCanceled(true);
        }
    }
}
