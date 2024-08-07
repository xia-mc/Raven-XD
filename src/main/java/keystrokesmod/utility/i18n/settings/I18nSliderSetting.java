package keystrokesmod.utility.i18n.settings;

import lombok.Getter;

@Getter
public class I18nSliderSetting extends I18nSetting {
    private final String settingName;
    private final String settingInfo;

    public I18nSliderSetting(String n, String toolTip, String settingName, String settingInfo) {
        super(n, toolTip);
        this.settingName = settingName;
        this.settingInfo = settingInfo;
    }
}
