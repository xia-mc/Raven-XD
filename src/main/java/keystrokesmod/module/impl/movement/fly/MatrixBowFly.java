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

    public MatrixBowFly(String name, @NotNull Fly parent) {
        super(name, parent);
    }

    @SubscribeEvent
    public void onRotation(@NotNull RotationEvent event) {
        event.setPitch(-89);
    }

    @Override
    public void onUpdate() {
        SlotHandler.setCurrentSlot(ContainerUtils.getSlot(ItemBow.class));
    }

    @SubscribeEvent
    public void onPreVelocity(@NotNull PreVelocityEvent event) {
        event.setCanceled(true);
        mc.thePlayer.motionY = Math.abs(event.getMotionY() / 8000.0);
        MoveUtil.strafe(Math.hypot(event.getMotionX() / 8000.0, event.getMotionZ() / 8000.0));
    }

    @SubscribeEvent
    public void onMove(@NotNull MoveEvent event) {
        if (mc.thePlayer.hurtTime <= 3) {
            event.setCanceled(true);
            mc.thePlayer.motionY = 0;
        }
    }
}
