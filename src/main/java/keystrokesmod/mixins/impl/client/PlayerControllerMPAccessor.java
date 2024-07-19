package keystrokesmod.mixins.impl.client;

import net.minecraft.client.multiplayer.PlayerControllerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerControllerMP.class)
public interface PlayerControllerMPAccessor {

    @Accessor("isHittingBlock")
    boolean isHittingBlock();

    @Accessor("curBlockDamageMP")
    void setCurBlockDamageMP(float curBlockDamageMP);
}
