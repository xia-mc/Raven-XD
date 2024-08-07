package keystrokesmod.clickgui.components;

import keystrokesmod.clickgui.ClickGui;
import keystrokesmod.clickgui.components.impl.ModuleComponent;
import keystrokesmod.module.setting.Setting;
import keystrokesmod.utility.font.IFont;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public interface IComponent {
    @Nullable
    default Setting getSetting() {
        return null;
    }

    default void render() {
    }

    @NotNull
    ModuleComponent getParent();

    @NotNull
    default IFont getFont() {
        return ClickGui.getFont();
    }

    default void onDrawScreen(int x, int y) {
    }

    default void onClick(int x, int y, int b) {
    }

    default void mouseReleased(int x, int y, int m) {
    }

    default void keyTyped(char t, int k) {
    }

    default void so(int n) {
    }

    default int gh() {
        return 0;
    }

    default void onGuiClosed() {
    }

    default void drawScreen(int x, int y) {
        onDrawScreen(x, y);
    }
}
