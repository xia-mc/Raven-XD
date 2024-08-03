package keystrokesmod.module.setting.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import keystrokesmod.module.setting.Setting;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ButtonSetting extends Setting {
    @Getter
    private final String name;
    private boolean isEnabled;
    public boolean isMethodButton;
    private Runnable method;
    private Consumer<ButtonSetting> onToggle;

    public ButtonSetting(String name, boolean isEnabled) {
        this(name, isEnabled, () -> true);
    }

    public ButtonSetting(String name, boolean isEnabled, @Nullable String toolTip) {
        this(name, isEnabled, () -> true, toolTip);
    }

    public ButtonSetting(String name, boolean isEnabled, @NotNull Consumer<ButtonSetting> onToggle) {
        this(name, isEnabled, () -> true, onToggle);
    }

    public ButtonSetting(String name, boolean isEnabled, @NotNull Consumer<ButtonSetting> onToggle, @Nullable String toolTip) {
        this(name, isEnabled, () -> true, onToggle, toolTip);
    }

    public ButtonSetting(String name, boolean isEnabled, @NotNull Supplier<Boolean> visibleCheck) {
        this(name, isEnabled, visibleCheck, setting -> {});
    }

    public ButtonSetting(String name, boolean isEnabled, @NotNull Supplier<Boolean> visibleCheck, @Nullable String toolTip) {
        this(name, isEnabled, visibleCheck, setting -> {}, toolTip);
    }

    public ButtonSetting(String name, boolean isEnabled, @NotNull Supplier<Boolean> visibleCheck, @NotNull Consumer<ButtonSetting> onToggle) {
        this(name, isEnabled, visibleCheck, onToggle, null);
    }

    public ButtonSetting(String name, boolean isEnabled, @NotNull Supplier<Boolean> visibleCheck, @NotNull Consumer<ButtonSetting> onToggle, @Nullable String toolTip) {
        super(name, visibleCheck, toolTip);
        this.name = name;
        this.isEnabled = isEnabled;
        this.isMethodButton = false;
        this.onToggle = onToggle;
    }

    public ButtonSetting(String name, Runnable method) {
        this(name, method, () -> true, null);
    }

    public ButtonSetting(String name, Runnable method, @Nullable String toolTip) {
        this(name, method, () -> true, toolTip);
    }

    public ButtonSetting(String name, Runnable method, @NotNull Supplier<Boolean> visibleCheck) {
        this(name, method, visibleCheck, null);
    }

    public ButtonSetting(String name, Runnable method, @NotNull Supplier<Boolean> visibleCheck, @Nullable String toolTip) {
        super(name, visibleCheck, toolTip);
        this.name = name;
        this.isEnabled = false;
        this.isMethodButton = true;
        this.method = method;
    }

    public void runMethod() {
        if (method != null) {
            method.run();
        }
    }

    public boolean isToggled() {
        return this.isEnabled;
    }

    public void toggle() {
        this.isEnabled = !this.isEnabled;
        onToggle.accept(this);
    }

    public void enable() {
        this.isEnabled = true;
    }

    public void disable() {
        this.isEnabled = false;
    }

    public void setEnabled(boolean b) {
        this.isEnabled = b;
    }

    @Override
    public void loadProfile(JsonObject data) {
        if (data.has(getName()) && data.get(getName()).isJsonPrimitive() && !this.isMethodButton) {
            JsonPrimitive jsonPrimitive = data.getAsJsonPrimitive(getName());
            if (jsonPrimitive.isBoolean()) {
                boolean booleanValue = jsonPrimitive.getAsBoolean();
                setEnabled(booleanValue);
            }
        }
    }
}
