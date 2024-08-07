package keystrokesmod.utility.i18n.settings;

import lombok.Getter;

@Getter
public class I18nButtonSetting extends I18nSetting {
    private final String name;

    public I18nButtonSetting(String n, String toolTip, String name) {
        super(n, toolTip);
        this.name = name;
    }
}
