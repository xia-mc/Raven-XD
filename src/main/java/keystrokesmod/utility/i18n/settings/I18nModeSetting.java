package keystrokesmod.utility.i18n.settings;

import lombok.Getter;

/**
 * Some modules (like RecordClick) will change the options dynamic,
 * so this I18n implement doesn't work for every modeSetting.
 * <p>
 * TODO This is an unresolved problem.
 */
@Getter
public class I18nModeSetting extends I18nSetting {
    private final String settingName;
    private final String[] options;

    public I18nModeSetting(String toolTip, String settingName, String[] options) {
        super(toolTip);
        this.settingName = settingName;
        this.options = options;
    }
}
