package keystrokesmod.module.setting.impl;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.FMLCommonHandler;

public class SubModeValue {
    private final String name;
    private boolean enabled;
    private boolean selected;
    protected static Minecraft mc;

    public SubModeValue(String name) {
        this.name = name;
        this.enabled = false;
        mc = Minecraft.getMinecraft();
    }
    public void setSelected(boolean b) {
        this.selected = b;
    }

    public String getName() {
        return name;
    }

    public void onEnable() {}
    public void enable() {
        if (this.isEnabled()) {
            return;
        }
        FMLCommonHandler.instance().bus().register(this);
        this.enabled = true;
        this.onEnable();
    }
    public void disable() {
        if (!this.isEnabled()) {
            return;
        }
        FMLCommonHandler.instance().bus().unregister(this);
        this.enabled = false;
        this.onDisable();
    }

    private boolean isEnabled() {
        return enabled;
    }

    private void onDisable() {}
}
