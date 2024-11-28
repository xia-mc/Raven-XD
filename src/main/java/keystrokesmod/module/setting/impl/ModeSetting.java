package keystrokesmod.module.setting.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import keystrokesmod.module.setting.Setting;
import keystrokesmod.module.setting.interfaces.InputSetting;
import keystrokesmod.utility.i18n.I18nModule;
import keystrokesmod.utility.i18n.settings.I18nModeSetting;
import keystrokesmod.utility.i18n.settings.I18nSetting;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Supplier;

public class ModeSetting extends Setting implements InputSetting {
    private final String settingName;
    @Getter
    private String[] options;
    private int value;
    private int max;

    public ModeSetting(String settingName, String[] options, int defaultValue) {
        this(settingName, options, defaultValue, () -> true);
    }

    public ModeSetting(String settingName, String[] options, int defaultValue, String toolTip) {
        this(settingName, options, defaultValue, () -> true, toolTip);
    }

    public ModeSetting(String settingName, String @NotNull [] options, int defaultValue, Supplier<Boolean> visibleCheck) {
        this(settingName, options, defaultValue, visibleCheck, null);
    }

    public ModeSetting(String settingName, String @NotNull [] options, int defaultValue, Supplier<Boolean> visibleCheck, String toolTip) {
        super(settingName, visibleCheck, toolTip);
        this.settingName = settingName;
        this.options = options;
        this.value = defaultValue;
        this.max = options.length - 1;
    }

    public static double correctValue(double v, double i, double a) {
        v = Math.max(i, v);
        v = Math.min(a, v);
        return v;
    }

    public void setOptions(String @NotNull [] options) {
        this.options = options;
        this.max = options.length - 1;
    }

    @Override
    public String getName() {
        return this.settingName;
    }

    public String getPrettyName() {
        if (parent != null) {
            I18nModule i18nObject = parent.getI18nObject();
            if (i18nObject != null) {
                Map<Setting, I18nSetting> settings = i18nObject.getSettings();
                if (settings.containsKey(this)) {
                    return ((I18nModeSetting) settings.get(this)).getSettingName();
                }
            }
        }
        return getName();
    }

    public String[] getPrettyOptions() {
        if (parent != null) {
            I18nModule i18nObject = parent.getI18nObject();
            if (i18nObject != null) {
                Map<Setting, I18nSetting> settings = i18nObject.getSettings();
                if (settings.containsKey(this)) {
                    return ((I18nModeSetting) settings.get(this)).getOptions();
                }
            }
        }
        return getOptions();
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
