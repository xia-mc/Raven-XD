package keystrokesmod.module;

import keystrokesmod.Raven;
import keystrokesmod.module.impl.client.Gui;
import keystrokesmod.module.impl.client.Notifications;
import keystrokesmod.module.impl.client.Settings;
import keystrokesmod.module.setting.Setting;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeValue;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.script.Script;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.i18n.I18nModule;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.Iterator;

public class Module {
    @Getter
    @Setter
    private @Nullable I18nModule i18nObject = null;

    @Getter
    protected final ArrayList<Setting> settings;
    private final String moduleName;
    private String prettyName;
    private String prettyInfo = "";
    private final Module.category moduleCategory;
    @Getter
    @Setter
    private boolean enabled;
    @Getter
    private int keycode;
    private final @Nullable String toolTip;
    protected static Minecraft mc;
    private boolean isToggled = false;
    public boolean canBeEnabled = true;
    public boolean ignoreOnSave = false;
    @Setter
    @Getter
    public boolean hidden = false;
    public Script script = null;

    public Module(String moduleName, Module.category moduleCategory, int keycode) {
        this(moduleName, moduleCategory, keycode, null);
    }

    public Module(String moduleName, Module.category moduleCategory, int keycode, @Nullable String toolTip) {
        this.moduleName = moduleName;
        this.prettyName = moduleName;
        this.moduleCategory = moduleCategory;
        this.keycode = keycode;
        this.toolTip = toolTip;
        this.enabled = false;
        mc = Minecraft.getMinecraft();
        this.settings = new ArrayList<>();
        if (!(this instanceof SubMode))
            Raven.moduleCounter++;
    }

    public static @Nullable Module getModule(Class<? extends Module> a) {
        Iterator<Module> var1 = ModuleManager.modules.iterator();

        Module module;
        do {
            if (!var1.hasNext()) {
                return null;
            }

            module = var1.next();
        } while (module.getClass() != a);

        return module;
    }

    public Module(String name, Module.category moduleCategory) {
        this(name, moduleCategory, null);
    }

    public Module(String name, Module.category moduleCategory, String toolTip) {
        this(name, moduleCategory, 0, toolTip);
    }

    public Module(@NotNull Script script) {
        this(script.name, category.scripts);
        this.script = script;
    }

    public void keybind() {
        if (this.keycode != 0) {
            try {
                if (!this.isToggled && (this.keycode >= 1000 ? Mouse.isButtonDown(this.keycode - 1000) : Keyboard.isKeyDown(this.keycode))) {
                    this.toggle();
                    this.isToggled = true;
                } else if ((this.keycode >= 1000 ? !Mouse.isButtonDown(this.keycode - 1000) : !Keyboard.isKeyDown(this.keycode))) {
                    this.isToggled = false;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                Utils.sendMessage("&cFailed to check keybinding. Setting to none");
                this.keycode = 0;
            }
        }
    }

    public final boolean canBeEnabled() {
        if (this.script != null && script.error) {
            return false;
        }
        return this.canBeEnabled;
    }

    public final void enable() {
        if (!this.canBeEnabled() || this.isEnabled()) {
            return;
        }
        this.setEnabled(true);
        ModuleManager.organizedModules.add(this);
        if (ModuleManager.hud.isEnabled()) {
            ModuleManager.sort();
        }

        if (this.script != null) {
            Raven.scriptManager.onEnable(script);
        }
        else {
            try {
                FMLCommonHandler.instance().bus().register(this);
                this.onEnable();
            } catch (Throwable ignored) {
            }
        }
    }

    public final void disable() {
        if (!this.isEnabled()) {
            return;
        }
        this.setEnabled(false);
        ModuleManager.organizedModules.remove(this);
        if (this.script != null) {
            Raven.scriptManager.onDisable(script);
        }
        else {
            try {
                FMLCommonHandler.instance().bus().unregister(this);
                this.onDisable();
            } catch (Throwable ignored) {
            }
        }
    }

    public String getInfo() {
        return "";
    }

    public String getPrettyInfo() {
        return ModuleManager.customName.isEnabled() && ModuleManager.customName.info.isToggled()
                ? getRawPrettyInfo()
                : getInfo();
    }

    public String getName() {
        return this.moduleName;
    }

    public String getPrettyName() {
        return ModuleManager.customName.isEnabled()
                ? getRawPrettyName()
                : i18nObject != null ? i18nObject.getName() : getName();
    }

    public @Nullable String getToolTip() {
        return toolTip;
    }

    public @Nullable String getPrettyToolTip() {
        return i18nObject != null ? i18nObject.getToolTip() : getToolTip();
    }

    public String getRawPrettyName() {
        return prettyName;
    }

    public String getRawPrettyInfo() {
        return prettyInfo.isEmpty() ? getInfo() : prettyInfo;
    }

    public final void setPrettyName(String name) {
        this.prettyName = name;
        ModuleManager.sort();
    }

    public final void setPrettyInfo(String name) {
        this.prettyInfo = name;
        ModuleManager.sort();
    }

    public void registerSetting(Setting setting) {
        synchronized (settings) {
            if (settings.contains(setting))
                throw new RuntimeException("Setting '" + setting.getName() + "' is already registered in module '" + this.getName() + "'!");

            setting.setParent(this);
            if (setting instanceof ModeValue) {
                this.settings.add(0, setting);
            } else {
                this.settings.add(setting);
            }
        }
    }

    public void registerSetting(Setting @NotNull ... setting) {
        for (Setting set : setting) {
            registerSetting(set);
        }
    }

    public void unregisterSetting(Setting setting) {
        synchronized (settings) {
            this.settings.remove(setting);
        }
    }

    public final Module.category moduleCategory() {
        return this.moduleCategory;
    }

    public void onEnable() throws Throwable {
    }

    public void onDisable() throws Throwable {
    }

    public void toggle() {
        if (this.isEnabled()) {
            this.disable();
            if (Settings.toggleSound.getInput() != 0) mc.thePlayer.playSound(Settings.getToggleSound(false), 1, 1);
            if (Notifications.moduleToggled.isToggled() && !(this instanceof Gui))
                Notifications.sendNotification(Notifications.NotificationTypes.INFO, "§4Disabled " + this.getPrettyName());
        } else {
            this.enable();
            if (Settings.toggleSound.getInput() != 0) mc.thePlayer.playSound(Settings.getToggleSound(true), 1, 1);
            if (Notifications.moduleToggled.isToggled() && !(this instanceof Gui))
                Notifications.sendNotification(Notifications.NotificationTypes.INFO, "§2Enabled " + this.getPrettyName());
        }

    }

    public void onUpdate() throws Throwable {
    }

    public void guiUpdate() throws Throwable {
    }

    public void guiButtonToggled(ButtonSetting b) throws Exception {
    }

    public void setBind(int keybind) {
        this.keycode = keybind;
    }


    public enum category {
        combat,
        movement,
        player,
        world,
        render,
        minigames,
        fun,
        other,
        client,
        profiles,
        scripts,
        exploit,
        experimental
    }
}
