package keystrokesmod.module.impl.render.targetvisual.targetesp;

import keystrokesmod.module.impl.render.TargetESP;
import keystrokesmod.module.impl.render.targetvisual.ITargetVisual;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.Theme;
import keystrokesmod.utility.render.RenderUtils;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;

public class RavenTargetESP extends SubMode<TargetESP> implements ITargetVisual {
    private final ModeSetting theme;

    public RavenTargetESP(String name, @NotNull TargetESP parent) {
        super(name, parent);
        this.registerSetting(theme = new ModeSetting("Theme", Theme.themes, 0));
    }

    @Override
    public void render(@NotNull EntityLivingBase target) {
        RenderUtils.renderEntity(target, 2, 0.0, 0.0, Theme.getGradient((int) theme.getInput(), 0), target.hurtTime != 0);
    }
}
