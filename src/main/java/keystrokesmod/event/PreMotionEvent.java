package keystrokesmod.event;

import keystrokesmod.script.classes.PlayerState;
import lombok.Getter;
import lombok.Setter;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.jetbrains.annotations.NotNull;

@Cancelable
public class PreMotionEvent extends Event {
    private static boolean setRenderYaw;
    @Setter
    @Getter
    public double posY;
    @Setter
    @Getter
    private double posX;
    @Setter
    @Getter
    private double posZ;
    @Getter
    private float yaw;
    @Setter
    @Getter
    private float pitch;
    @Setter
    @Getter
    private boolean onGround;
    private boolean isSprinting;
    private boolean isSneaking;

    public PreMotionEvent(double posX, double posY, double posZ, float yaw, float pitch, boolean onGround, boolean isSprinting, boolean isSneaking) {
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
        this.isSprinting = isSprinting;
        this.isSneaking = isSneaking;
    }

    public static boolean setRenderYaw() {
        return setRenderYaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
        setRenderYaw = true;
    }

    public void setRenderYaw(boolean setRenderYaw) {
        PreMotionEvent.setRenderYaw = setRenderYaw;
    }

    public boolean isSprinting() {
        return isSprinting;
    }

    public void setSprinting(boolean sprinting) {
        this.isSprinting = sprinting;
    }

    public boolean isSneaking() {
        return isSneaking;
    }

    public void setSneaking(boolean sneaking) {
        this.isSneaking = sneaking;
    }

    public boolean isEquals(@NotNull PlayerState e) {
        return e.x == this.posX && e.y == this.posY && e.z == this.posZ && e.yaw == this.yaw && e.pitch == this.pitch && e.onGround == this.onGround && e.isSprinting == this.isSprinting && e.isSneaking == this.isSneaking;
    }
}
