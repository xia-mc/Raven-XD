package keystrokesmod.mixins.impl.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

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

    @Accessor("activePotionsMap")
    Map<Integer, PotionEffect> getActivePotionsMap();

    @Accessor("activePotionsMap")
    void setActivePotionsMap(Map<Integer, PotionEffect> map);

    @Accessor("dead")
    boolean isDead();

    @Accessor("jumpTicks")
    void setJumpTicks(int ticks);
}
