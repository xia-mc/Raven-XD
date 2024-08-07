package keystrokesmod.utility.i18n.settings;

import lombok.Getter;

@Getter
public class I18nDescriptionSetting extends I18nSetting {
    private final String desc;

    public I18nDescriptionSetting(String n, String toolTip, String desc) {
        super(n, toolTip);
        this.desc = desc;
    }
}
