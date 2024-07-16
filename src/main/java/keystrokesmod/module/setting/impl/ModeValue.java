package keystrokesmod.module.setting.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.Setting;
import keystrokesmod.module.setting.interfaces.InputSetting;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ModeValue extends Setting implements InputSetting {
    private String settingName;
    private final Module parent;
    private ArrayList<SubModeValue> subModeValues = new ArrayList<>();
    private String defaultValue;
    private int selected;
    public ModeValue(String settingName, Module parent) {
        super(settingName, () -> true);
        this.settingName = settingName;
        this.parent = parent;

    }
    public ModeValue add(final SubModeValue subModeValue) {
        if(subModeValue == null)
            return this;
        subModeValues.add(subModeValue);
        return this;
    }
    public ArrayList<SubModeValue> getSubModeValues() {
        return subModeValues;
    }
    public ModeValue setDefaultValue(String name) {
        this.defaultValue = name;
        return this;
    }
    @Override
    public void loadProfile(@NotNull JsonObject profile) {
        if (profile.has(getName()) && profile.get(getName()).isJsonPrimitive()) {
            JsonPrimitive jsonPrimitive = profile.getAsJsonPrimitive(getName());
            if (jsonPrimitive.isNumber()) {
                int newValue = jsonPrimitive.getAsInt();
                setValue(newValue);
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
        this.selected = (int) value;
        if(this.parent.isEnabled()) {
            this.subModeValues.get(selected).enable();
        }
    }
    public void setValueRaw(int n) {
        this.subModeValues.get(selected).disable();
        this.selected = n;
        this.setValue(n);
    }
    public int getMax() {
        return subModeValues.size() - 1;
    }
    public int getMin() {
        return 0;
    }
    public void nextValue() {
        if (getInput() >= getMax()) {
            setValueRaw(getMin());
        } else {
            setValueRaw((int) (getInput() + 1));
        }
    }


    public void prevValue() {
        if (getInput() <= getMin()) {
            setValueRaw(getMax());
        } else {
            setValueRaw((int) (getInput() - 1));

        }
    }

    public void enable(SubModeValue subModeValue) {
        subModeValues.get(this.selected).disable();
        subModeValue.enable();
        this.selected = subModeValues.indexOf(subModeValue);
    }
}