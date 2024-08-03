package keystrokesmod.module.setting.impl;

import com.google.gson.JsonObject;
import keystrokesmod.module.setting.Setting;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

@Setter
@Getter
public class DescriptionSetting extends Setting {
    private String desc;

    public DescriptionSetting(String t) {
        this(t, () -> true);
    }

    public DescriptionSetting(String t, @NotNull Supplier<Boolean> visibleCheck) {
        this(t, visibleCheck, null);
    }

    public DescriptionSetting(String t, @NotNull Supplier<Boolean> visibleCheck, String toolTip) {
        super(t, visibleCheck, toolTip);
        this.desc = t;
    }

    @Override
    public void loadProfile(JsonObject data) {
    }
}
