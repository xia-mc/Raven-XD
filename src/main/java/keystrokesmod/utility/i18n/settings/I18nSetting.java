package keystrokesmod.utility.i18n.settings;

import lombok.Getter;

@Getter
public class I18nSetting {
    private final String toolTip;

    public I18nSetting(String toolTip) {
        this.toolTip = toolTip;
    }
}
