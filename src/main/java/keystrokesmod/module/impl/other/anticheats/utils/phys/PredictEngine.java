package keystrokesmod.module.impl.other.anticheats.utils.phys;

import keystrokesmod.mixins.impl.entity.EntityLivingBaseAccessor;
import keystrokesmod.module.impl.other.anticheats.TRPlayer;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.player.EntityPlayer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class PredictEngine extends EntityPlayer implements Cloneable {
    private final TRPlayer player;
    double posX;
    double posY;
    double posZ;
    double newPosX;
    double newPosY;
    double newPosZ;
    float rotationYaw;
    float rotationPitch;
    double newRotationYaw;
    double newRotationPitch;
    int newPosRotationIncrements;
    private double motionX;
    private double motionY;
    private double motionZ;

    @Contract(pure = true)
    public PredictEngine(@NotNull TRPlayer player) {
        super(player.fabricPlayer.worldObj, player.fabricPlayer.getGameProfile());
        this.player = player;
        sync();
    }

    private void sync() {
        this.posX = player.fabricPlayer.posX;
        this.posY = player.fabricPlayer.posY;
        this.posZ = player.fabricPlayer.posZ;
        this.newPosX = ((EntityLivingBaseAccessor) player.fabricPlayer).getNewPosX();
        this.newPosY = ((EntityLivingBaseAccessor) player.fabricPlayer).getNewPosY();
        this.newPosZ = ((EntityLivingBaseAccessor) player.fabricPlayer).getNewPosZ();
        this.rotationYaw = player.fabricPlayer.rotationYaw;
        this.rotationPitch = player.fabricPlayer.rotationPitch;
        this.newRotationYaw = ((EntityLivingBaseAccessor) player.fabricPlayer).getNewRotationYaw();
        this.newRotationPitch = ((EntityLivingBaseAccessor) player.fabricPlayer).getNewRotationPitch();
        this.newPosRotationIncrements = ((EntityLivingBaseAccessor) player.fabricPlayer).getNewPosRotationIncrements();
        this.motionX = player.lastPos.x - player.currentPos.x;
        this.motionY = player.lastPos.y - player.currentPos.y;
        this.motionZ = player.lastPos.z - player.currentPos.z;
    }

    @Override
    public PredictEngine clone() {
        try {
            return (PredictEngine) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public boolean isSpectator() {
        return false;
    }
}
