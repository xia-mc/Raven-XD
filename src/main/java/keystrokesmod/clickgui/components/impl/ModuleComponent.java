package keystrokesmod.clickgui.components.impl;

import keystrokesmod.Raven;
import keystrokesmod.clickgui.components.Component;
import keystrokesmod.clickgui.components.IComponent;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.client.Gui;
import keystrokesmod.module.setting.Setting;
import keystrokesmod.utility.render.RenderUtils;
import keystrokesmod.utility.profile.Manager;
import keystrokesmod.utility.profile.ProfileModule;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;

public class ModuleComponent implements IComponent {
    private final int c2 = (new Color(154, 2, 255)).getRGB();
    private final int hoverColor = (new Color(0, 0, 0, 110)).getRGB();
    private final int unsavedColor = new Color(114, 188, 250).getRGB();
    private final int invalidColor = new Color(255, 80, 80).getRGB();
    private final int enabledColor = new Color(24, 154, 255).getRGB();
    private final int disabledColor = new Color(192, 192, 192).getRGB();
    public Module mod;
    public CategoryComponent categoryComponent;
    public int o;
    public ArrayList<Component> settings;
    public boolean po;
    private boolean hovering;

    public ModuleComponent(Module mod, CategoryComponent p, int o) {
        this.mod = mod;
        this.categoryComponent = p;
        this.o = o;
        this.settings = new ArrayList<>();
        this.po = false;
        updateSetting();
    }

    public void updateSetting() {
        int y = o + 12;
        if (mod != null && !mod.getSettings().isEmpty()) {
            this.settings.clear();
            for (Setting v : mod.getSettings()) {
                this.settings.add(Component.fromSetting(v, this, y));
                y += 12;
            }
        }
        this.settings.add(new BindComponent(this, y));
    }

    public void so(int n) {
        this.o = n;
        int y = this.o + 16;

        for (Component co : this.settings) {
            Setting setting = co.getSetting();
            if (setting == null || setting.isVisible()) {
                co.so(y);
                if (co instanceof SliderComponent) {
                    y += 16;
                } else {
                    y += 12;
                }
            }
        }
    }

    public static void e() {
        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glDepthMask(true);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
    }

    public static void f() {
        GL11.glEnable(3553);
        GL11.glDisable(3042);
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
        GL11.glEdgeFlag(true);
    }

    public static void g(int h) {
        float a = 0.0F;
        float r = 0.0F;
        float g = 0.0F;
        float b = 0.0F;
        GL11.glColor4f(r, g, b, a);
    }

    public static void v(float x, float y, float x1, float y1, int t, int b) {
        e();
        GL11.glShadeModel(7425);
        GL11.glBegin(7);
        g(t);
        GL11.glVertex2f(x, y1);
        GL11.glVertex2f(x1, y1);
        g(b);
        GL11.glVertex2f(x1, y);
        GL11.glVertex2f(x, y);
        GL11.glEnd();
        GL11.glShadeModel(7424);
        f();
    }

    public void render() {
        if (hovering) {
            RenderUtils.drawRoundedRectangle(this.categoryComponent.getX(), this.categoryComponent.getY() + o, this.categoryComponent.getX() + this.categoryComponent.gw(), this.categoryComponent.getY() + 16 + this.o, 8, hoverColor);
        }
        v((float) this.categoryComponent.getX(), (float) (this.categoryComponent.getY() + this.o), (float) (this.categoryComponent.getX() + this.categoryComponent.gw()), (float) (this.categoryComponent.getY() + 15 + this.o), this.mod.isEnabled() ? this.c2 : -12829381, this.mod.isEnabled() ? this.c2 : -12302777);
        GL11.glPushMatrix();
        int button_rgb = this.mod.isEnabled() ? enabledColor : disabledColor;
        if (this.mod.script != null && this.mod.script.error) {
            button_rgb = invalidColor;
        }
        if (this.mod.moduleCategory() == Module.category.profiles && !(this.mod instanceof Manager) && !((ProfileModule) this.mod).saved && Raven.currentProfile.getModule() == this.mod) {
            button_rgb = unsavedColor;
        }
        getFont().drawStringWithShadow(this.mod.getPrettyName(), (float) (this.categoryComponent.getX() + (double) this.categoryComponent.gw() / 2 - getFont().width(this.mod.getPrettyName()) / 2), (float) (this.categoryComponent.getY() + this.o + 4), button_rgb);
        GL11.glPopMatrix();
        if (this.po && !this.settings.isEmpty()) {
            for (Component c : this.settings) {
                Setting setting = c.getSetting();
                if (setting == null || setting.isVisible()) {
                    c.render();
                }
            }
        }
    }

    @Override
    public @NotNull ModuleComponent getParent() {
        return this;
    }

    public int gh() {
        if (!this.po) {
            return 16;
        } else {
            int h = 16;

            for (Component c : this.settings) {
                Setting setting = c.getSetting();
                if (setting == null || setting.isVisible()) {
                    if (c instanceof SliderComponent) {
                        h += 16;
                    } else {
                        h += 12;
                    }
                }
            }

            return h;
        }
    }

    public void onDrawScreen(int x, int y) {
        if (!this.settings.isEmpty()) {
            for (Component c : this.settings) {
                c.drawScreen(x, y);
            }
        }
        hovering = isHover(x, y);

        if (hovering && categoryComponent.isCategoryOpened() && Gui.toolTip.isToggled() && mod.getPrettyToolTip() != null) {
            Raven.clickGui.run(() -> RenderUtils.drawToolTip(mod.getPrettyToolTip(), x, y));
        }
    }

    public String getName() {
        return mod.getName();
    }

    public void onClick(int x, int y, int b) {
        if (this.isHover(x, y) && b == 0 && this.mod.canBeEnabled()) {
            this.mod.toggle();
            if (this.mod.moduleCategory() != Module.category.profiles) {
                if (Raven.currentProfile != null) {
                    ((ProfileModule) Raven.currentProfile.getModule()).saved = false;
                }
            }
        }

        if (this.isHover(x, y) && b == 1) {
            this.po = !this.po;
            this.categoryComponent.render();
        }

        for (Component c : this.settings) {
            c.onClick(x, y, b);
        }
    }

    public void mouseReleased(int x, int y, int m) {
        for (Component c : this.settings) {
            c.mouseReleased(x, y, m);
        }

    }

    public void keyTyped(char t, int k) {
        for (Component c : this.settings) {
            c.keyTyped(t, k);
        }
    }

    public void onGuiClosed() {
        for (Component c : this.settings) {
            c.onGuiClosed();
        }
    }

    public boolean isHover(int x, int y) {
        return x > this.categoryComponent.getX() && x < this.categoryComponent.getX() + this.categoryComponent.gw() && y > this.categoryComponent.getY() + this.o && y < this.categoryComponent.getY() + 16 + this.o;
    }
}
