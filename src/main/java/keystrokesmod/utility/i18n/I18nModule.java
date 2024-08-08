package keystrokesmod.utility.i18n;

import keystrokesmod.module.setting.Setting;
import keystrokesmod.utility.i18n.settings.I18nSetting;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@Getter
@AllArgsConstructor
public class I18nModule {
    private final String name;
    private final @Nullable String toolTip;
    private final Map<Setting, I18nSetting> settings;
}
