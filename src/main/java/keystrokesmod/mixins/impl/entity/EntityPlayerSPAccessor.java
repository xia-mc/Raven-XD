package keystrokesmod.mixins.impl.entity;


import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityPlayerSP.class)
public interface EntityPlayerSPAccessor {

    @Accessor("serverSprintState")
    boolean isServerSprint();

    @Accessor("serverSprintState")
    void setServerSprint(boolean serverSprint);
}
