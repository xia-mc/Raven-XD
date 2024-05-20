package keystrokesmod.module;

import keystrokesmod.Raven;
import keystrokesmod.module.setting.Setting;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.script.Script;
import keystrokesmod.utility.Utils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.Iterator;

public class Module {
    protected ArrayList<Setting> settings;
    private final String moduleName;
    private final Module.category moduleCategory;
    private boolean enabled;
    private int keycode;
    protected static Minecraft mc;
    private boolean isToggled = false;
    public boolean canBeEnabled = true;
    public boolean ignoreOnSave = false;
    public boolean hidden = false;
    public Script script = null;

    public Module(String moduleName, Module.category moduleCategory, int keycode) {
        this.moduleName = moduleName;
        this.moduleCategory = moduleCategory;
        this.keycode = keycode;
        this.enabled = false;
        mc = Minecraft.getMinecraft();
        this.settings = new ArrayList();
    }

    public static Module getModule(Class<? extends Module> a) {
        Iterator var1 = ModuleManager.modules.iterator();

        Module module;
        do {
            if (!var1.hasNext()) {
                return null;
            }

            module = (Module) var1.next();
        } while (module.getClass() != a);

        return module;
    }

    public Module(String name, Module.category moduleCategory) {
        this.moduleName = name;
        this.moduleCategory = moduleCategory;
        this.keycode = 0;
        this.enabled = false;
        mc = Minecraft.getMinecraft();
        this.settings = new ArrayList();
    }

    public Module(Script script) {
        super();
        this.enabled = false;
        this.moduleName = script.name;
        this.script = script;
        this.keycode = 0;
        this.moduleCategory = category.scripts;
        this.settings = new ArrayList<>();
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

    public boolean canBeEnabled() {
        if (this.script != null && script.error) {
            return false;
        }
        return this.canBeEnabled;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public void enable() {
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
            FMLCommonHandler.instance().bus().register(this);
            this.onEnable();
        }
    }

    public void disable() {
        if (!this.isEnabled()) {
            return;
        }
        this.setEnabled(false);
        ModuleManager.organizedModules.remove(this);
        if (this.script != null) {
            Raven.scriptManager.onDisable(script);
        }
        else {
            FMLCommonHandler.instance().bus().unregister(this);
            this.onDisable();
        }
    }

    public String getInfo() {
        return "";
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getName() {
        return this.moduleName;
    }

    public ArrayList<Setting> getSettings() {
        return this.settings;
    }

    public void registerSetting(Setting Setting) {
        this.settings.add(Setting);
    }

    public Module.category moduleCategory() {
        return this.moduleCategory;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void toggle() {
        if (this.isEnabled()) {
            this.disable();
        } else {
            this.enable();
        }

    }

    public void onUpdate() {
    }

    public void guiUpdate() {
    }

    public void guiButtonToggled(ButtonSetting b) {
    }

    public int getKeycode() {
        return this.keycode;
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
        scripts
    }
}
