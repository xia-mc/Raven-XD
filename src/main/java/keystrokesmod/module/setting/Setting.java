package keystrokesmod.module.setting;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public abstract class Setting {
    public String n;
    public Supplier<Boolean> visibleCheck;
    public boolean viewOnly;

    public Setting(String n, @NotNull Supplier<Boolean> visibleCheck) {
        this.n = n;
        this.visibleCheck = visibleCheck;
        this.viewOnly = false;
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
