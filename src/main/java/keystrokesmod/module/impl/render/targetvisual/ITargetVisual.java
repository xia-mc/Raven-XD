package keystrokesmod.module.impl.render.targetvisual;

import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;

public interface ITargetVisual {

    void render(@NotNull EntityLivingBase target);
}
