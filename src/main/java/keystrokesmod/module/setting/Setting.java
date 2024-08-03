package keystrokesmod.module.setting;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class Setting {
    public String n;
    public Supplier<Boolean> visibleCheck;
    public boolean viewOnly;
    public @Nullable String toolTip = null;

    public Setting(String n, @NotNull Supplier<Boolean> visibleCheck, @Nullable String toolTip) {
        this.n = n;
        this.visibleCheck = visibleCheck;
        this.viewOnly = false;
        this.toolTip = toolTip;
    }

    public String getName() {
        return this.n;
    }

    public boolean isVisible() {
        final Boolean b = visibleCheck.get();
        return b == null || b;
    }

    public abstract void loadProfile(JsonObject data);
}
