package keystrokesmod.module.setting.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import keystrokesmod.module.setting.Setting;
import keystrokesmod.module.setting.interfaces.InputSetting;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ModeSetting extends Setting implements InputSetting {
    private final String settingName;
    private String[] options;
    private int value;
    private int max;

    public ModeSetting(String settingName, String[] options, int defaultValue) {
        this(settingName, options, defaultValue, () -> true);
    }

    public ModeSetting(String settingName, String @NotNull [] options, int defaultValue, Supplier<Boolean> visibleCheck) {
        super(settingName, visibleCheck);
        this.settingName = settingName;
        this.options = options;
        this.value = defaultValue;
        this.max = options.length - 1;
    }

    public String[] getOptions() {
        return options;
    }

    public void setOptions(String @NotNull [] options) {
        this.options = options;
        this.max = options.length - 1;
    }

    @Override
    public String getName() {
        return this.settingName;
    }

    @Override
    public double getInput() {
        return value;
    }

    public double getMin() {
        return 0;
    }

    public double getMax() {
        return this.max;
    }

    @Override
    public void setValue(double n) {
        setValue((int) n);
    }

    public void setValue(int n) {
        n = (int) correctValue(n, 0, this.max);
        this.value = n;
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

    public void setValueRaw(int n) {
        this.value = n;
    }

    public static double correctValue(double v, double i, double a) {
        v = Math.max(i, v);
        v = Math.min(a, v);
        return v;
    }

    @Override
    public void loadProfile(@NotNull JsonObject data) {
        if (data.has(getName()) && data.get(getName()).isJsonPrimitive()) {
            JsonPrimitive jsonPrimitive = data.getAsJsonPrimitive(getName());
            if (jsonPrimitive.isNumber()) {
                int newValue = jsonPrimitive.getAsInt();
                setValue(newValue);
            }
        }
    }
}
