package keystrokesmod.utility.profile;

import keystrokesmod.Raven;
import keystrokesmod.clickgui.ClickGui;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.client.Settings;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.utility.Utils;

public class ProfileModule extends Module {
    private final ButtonSetting saveProfile;
    private final ButtonSetting removeProfile;
    private final Profile profile;
    public boolean saved = true;

    public ProfileModule(Profile profile, String name, int bind) {
        super(name, category.profiles, bind);
        this.profile = profile;
        this.registerSetting(saveProfile = new ButtonSetting("Save profile", () -> {
            Utils.sendMessage("&7Saved profile: &b" + getName());
            Raven.profileManager.saveProfile(this.profile);
            saved = true;
        }));
        this.registerSetting(removeProfile = new ButtonSetting("Remove profile", () -> {
            Utils.sendMessage("&7Removed profile: &b" + getName());
            Raven.profileManager.deleteProfile(getName());
        }));
    }

    @Override
    public void toggle() {
        if (mc.currentScreen instanceof ClickGui || mc.currentScreen == null) {
            if (this.profile == Raven.currentProfile) {
                return;
            }
            Raven.profileManager.loadProfile(this.getName());

            Raven.currentProfile = profile;

            if (Settings.sendMessage.isToggled()) {
                Utils.sendMessage("&7Enabled profile: &b" + this.getName());
            }
            saved = true;
        }
    }

    @Override
    public boolean isEnabled() {
        if (Raven.currentProfile == null) {
            return false;
        }
        return Raven.currentProfile.getModule() == this;
    }
}
