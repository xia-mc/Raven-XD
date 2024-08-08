package keystrokesmod.module.setting;

import com.google.gson.JsonObject;
import keystrokesmod.module.Module;
import keystrokesmod.utility.i18n.I18nModule;
import keystrokesmod.utility.i18n.settings.I18nSetting;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

public abstract class Setting {
    public String n;
    public Supplier<Boolean> visibleCheck;
    public boolean viewOnly;
    private final @Nullable String toolTip;
    @Setter
    protected @Nullable Module parent = null;

    public Setting(String n, @NotNull Supplier<Boolean> visibleCheck, @Nullable String toolTip) {
        this.n = n;
        this.visibleCheck = visibleCheck;
        this.viewOnly = false;
        this.toolTip = toolTip;
    }

    public Setting(String n, @NotNull Supplier<Boolean> visibleCheck) {
        this(n, visibleCheck, null);
    }

    public String getName() {
        return this.n;
    }

    public @Nullable String getToolTip() {
        return this.toolTip;
    }

    public @Nullable String getPrettyToolTip() {
        if (parent != null) {
            I18nModule i18nObject = parent.getI18nObject();
            if (i18nObject != null) {
                Map<Setting, I18nSetting> settings = i18nObject.getSettings();
                if (settings.containsKey(this)) {
                    return settings.get(this).getToolTip();
                }
            }
        }
        return getToolTip();
    }

    public boolean isVisible() {
        final Boolean b = visibleCheck.get();
        return b == null || b;
    }

    public abstract void loadProfile(JsonObject data);
}
