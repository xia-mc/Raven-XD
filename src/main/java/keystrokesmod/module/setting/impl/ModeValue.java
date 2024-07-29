package keystrokesmod.module.setting.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.Setting;
import keystrokesmod.module.setting.interfaces.InputSetting;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class ModeValue extends Setting implements InputSetting {
    private final String settingName;
    private final Module parent;
    private final List<SubMode<?>> subModes = new ArrayList<>();
    private int selected = 0;
    public ModeValue(String settingName, Module parent) {
        super(settingName, () -> true);
        this.settingName = settingName;
        this.parent = parent;

    }
    public ModeValue add(final SubMode<?> subMode) {
        if (subMode == null)
            return this;
        subModes.add(subMode);
        subMode.register();
        // register settings from SubModes
        for (Setting setting : subMode.getSettings()) {
            final Supplier<Boolean> fromVisibleCheck = setting.visibleCheck;
            setting.visibleCheck = () -> subModes.get((int) this.getInput()) == subMode && fromVisibleCheck.get();
            parent.registerSetting(setting);
        }
        return this;
    }
    public List<SubMode<?>> getSubModeValues() {
        return subModes;
    }
    public ModeValue setDefaultValue(String name) {
        Optional<SubMode<?>> subMode = subModes.stream().filter(mode -> Objects.equals(mode.getName(), name)).findFirst();
        if (!subMode.isPresent()) return this;

        setValueRaw(subModes.indexOf(subMode.get()));
        return this;
    }
    @Override
    public void loadProfile(@NotNull JsonObject profile) {
        if (profile.has(getName()) && profile.get(getName()).isJsonPrimitive()) {
            JsonPrimitive jsonPrimitive = profile.getAsJsonPrimitive(getName());
            if (jsonPrimitive.isNumber()) {
                int newValue = jsonPrimitive.getAsInt();
                setValueRaw(newValue);
            }
        }
    }

    public String getSettingName() {
        return settingName;
    }

    public Module getParent() {
        return parent;
    }

    @Override
    public double getInput() {
        return this.selected;
    }

    @Override
    public void setValue(double value) {
        if (value > getMax() || value < getMin()) {
            this.selected = (int) getMin();
        } else {
            this.selected = (int) value;
        }
        if (this.parent.isEnabled() || !parent.canBeEnabled) {
            this.subModes.get(selected).enable();
        }
    }
    public void setValueRaw(int n) {
        disable();
        this.setValue(n);
    }
    public double getMax() {
        return subModes.size() - 1;
    }
    public double getMin() {
        return 0;
    }
    public void nextValue() {
        if (getInput() >= getMax()) {
            setValueRaw((int) getMin());
        } else {
            setValueRaw((int) (getInput() + 1));
        }
    }


    public void prevValue() {
        if (getInput() <= getMin()) {
            setValueRaw((int) getMax());
        } else {
            setValueRaw((int) (getInput() - 1));

        }
    }

    public void enable() {
        setValueRaw((int) getInput());
    }

    public void disable() {
        this.subModes.get(selected).disable();
    }
}