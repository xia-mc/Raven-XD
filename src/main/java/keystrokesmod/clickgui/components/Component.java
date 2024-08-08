package keystrokesmod.clickgui.components;

import keystrokesmod.Raven;
import keystrokesmod.clickgui.components.impl.*;
import keystrokesmod.module.impl.client.Gui;
import keystrokesmod.module.setting.Setting;
import keystrokesmod.module.setting.impl.*;
import keystrokesmod.utility.render.RenderUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.NoSuchElementException;

public abstract class Component implements IComponent {
    public static final int DEFAULT_COLOR = new Color(255, 255, 255).getRGB();
    public static final int HOVER_COLOR = new Color(162, 162, 162).getRGB();
    private static final int TOGGLE_DEFAULT_COLOR = (new Color(20, 255, 0)).getRGB();
    private static final int TOGGLE_HOVER_COLOR = (new Color(20, 162, 0)).getRGB();

    protected ModuleComponent parent;
    protected int color = DEFAULT_COLOR;
    protected int toggleColor = TOGGLE_DEFAULT_COLOR;
    protected int o;
    protected int x;
    protected int y;

    public Component(ModuleComponent parent) {
        this.parent = parent;
    }

    public final void drawScreen(int x, int y) {
        boolean hover = isHover(x, y);
        color = hover ? HOVER_COLOR : DEFAULT_COLOR;
        toggleColor = hover ? TOGGLE_HOVER_COLOR : TOGGLE_DEFAULT_COLOR;
        onDrawScreen(x, y);

        if (getSetting() != null && hover && getSetting().isVisible() && getParent().po && Gui.toolTip.isToggled() && getSetting().getPrettyToolTip() != null) {
            Raven.clickGui.run(() -> RenderUtils.drawToolTip(getSetting().getPrettyToolTip(), x, y));
        }
    }

    @Override
    public @NotNull ModuleComponent getParent() {
        return parent;
    }

    public boolean isHover(int x, int y) {
        return x > this.x && x < this.x + getParent().categoryComponent.gw() && y > this.y && y < this.y + 8;
    }

    @Contract("_, _, _ -> new")
    public static @NotNull Component fromSetting(@NotNull Setting setting, ModuleComponent component, final int y) {
        if (setting instanceof SliderSetting) {
            return new SliderComponent((SliderSetting) setting, component, y);
        }
        if (setting instanceof ButtonSetting) {
            return new ButtonComponent(component.mod, (ButtonSetting) setting, component, y);
        }
        if (setting instanceof DescriptionSetting) {
            return new DescriptionComponent((DescriptionSetting) setting, component, y);
        }
        if (setting instanceof ModeSetting) {
            return new ModeComponent((ModeSetting) setting, component, y);
        }
        if (setting instanceof ModeValue) {
            return new ModeValueComponent((ModeValue) setting, component, y);
        }
        throw new NoSuchElementException("no match component for setting '%s', this shouldn't be happen. please content author.");
    }
}
