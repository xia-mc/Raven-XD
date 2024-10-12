package keystrokesmod.mixins.impl.entity;

import net.minecraft.entity.EntityLivingBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityLivingBase.class)
public interface EntityLivingBaseAccessor {

    @Accessor("newPosX")
    double getNewPosX();

    @Accessor("newPosY")
    double getNewPosY();

    @Accessor("newPosZ")
    double getNewPosZ();

    @Accessor("newRotationYaw")
    double getNewRotationYaw();

    @Accessor("newRotationPitch")
    double getNewRotationPitch();

    @Accessor("newPosRotationIncrements")
    int getNewPosRotationIncrements();
}
