package keystrokesmod.module.setting.impl;

import keystrokesmod.Raven;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.Setting;
import org.jetbrains.annotations.NotNull;

public abstract class SubMode<T extends Module> extends Module {
    protected final String name;
    protected final T parent;

    public SubMode(String name, @NotNull T parent) {
        super(parent.getName() + "$" + name, parent.moduleCategory());
        Raven.getModuleManager().addModule(this);
        this.name = name;
        this.parent = parent;
    }

    @Override
    public String getPrettyName() {
        return getRawPrettyName();
    }

    @Override
    public String getRawPrettyName() {
        return name;
    }

    @Override
    public void registerSetting(Setting setting) {
        super.registerSetting(setting);
    }
}
