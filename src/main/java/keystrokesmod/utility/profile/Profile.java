package keystrokesmod.utility.profile;

import keystrokesmod.module.Module;

public class Profile {
    private final Module module;
    private int bind = 0;
    private final String profileName;

    public Profile(String profileName, int bind) {
        this.profileName = profileName;
        this.bind = bind;
        this.module = new ProfileModule(this, profileName, bind);
        this.module.ignoreOnSave = true;
    }

    public Module getModule() {
        return module;
    }

    public int getBind() {
        return bind;
    }

    public String getName() {
        return profileName;
    }
}
