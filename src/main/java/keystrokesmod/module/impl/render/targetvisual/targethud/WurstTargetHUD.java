package keystrokesmod.module.impl.render.targetvisual.targethud;

import keystrokesmod.module.impl.render.TargetHUD;
import keystrokesmod.module.impl.render.targetvisual.ITargetVisual;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.font.IFont;
import keystrokesmod.utility.render.RenderUtils;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class WurstTargetHUD extends SubMode<TargetHUD> implements ITargetVisual {
    private final ModeSetting font;

    public WurstTargetHUD(String name, @NotNull TargetHUD parent) {
        super(name, parent);
        this.registerSetting(font = new ModeSetting("Font", new String[]{"Minecraft", "ProductSans", "Regular"}, 0));
    }

    private IFont getFont() {
        switch ((int) font.getInput()) {
            default:
            case 0:
                return FontManager.getMinecraft();
            case 1:
                return FontManager.productSansMedium;
            case 2:
                return FontManager.regular22;
        }
    }

    @Override
    public void render(@NotNull EntityLivingBase target) {
        String name = target.getDisplayName().getFormattedText();

        double x = TargetHUD.current$minX;
        double y = TargetHUD.current$minY;

        RenderUtils.drawRect(x, y,  x + 185, y + 34, new Color(255, 255, 255, 100).getRGB());
        getFont().drawString("Name: " + name, x + 4, y + 4, Color.BLACK.getRGB());

        double health = Math.min(Math.round(target.getHealth()), target.getMaxHealth());
        RenderUtils.drawRect(x + 4, y + 16, x + 4 + (185 - 8) * (health / target.getMaxHealth()), y + 16 + 10, Color.ORANGE.getRGB());
    }
}
