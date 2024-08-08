package keystrokesmod.utility.i18n.settings;

import lombok.Getter;

@Getter
public class I18nDescriptionSetting extends I18nSetting {
    private final String desc;

    public I18nDescriptionSetting(String toolTip, String desc) {
        super(toolTip);
        this.desc = desc;
    }
}
