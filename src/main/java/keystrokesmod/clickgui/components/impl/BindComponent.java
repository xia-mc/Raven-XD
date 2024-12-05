package keystrokesmod.clickgui.components.impl;

import keystrokesmod.Raven;
import keystrokesmod.clickgui.components.Component;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.client.Gui;
import keystrokesmod.utility.Theme;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.profile.ProfileModule;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class BindComponent extends Component {
    public boolean isBinding;
    private int bind;
    private int x;
    private int y;

    public BindComponent(ModuleComponent moduleComponent, int bind) {
        super(moduleComponent);
        this.x = moduleComponent.categoryComponent.getX() + moduleComponent.categoryComponent.gw();
        this.y = moduleComponent.categoryComponent.getY() + moduleComponent.o;
        this.bind = bind;
    }

    public void so(int n) {
        this.bind = n;
    }

    public void render() {
        GL11.glPushMatrix();
        GL11.glScaled(0.5D, 0.5D, 0.5D);
        this.drawString(!this.parent.mod.canBeEnabled() && this.parent.mod.script == null ? "Module cannot be bound." : this.isBinding ? "Press a key..." : "Current bind: '§e" + Utils.getKeyName(this.parent.mod.getKeycode()) + "§r'");
        GL11.glPopMatrix();
    }

    public void onDrawScreen(int x, int y) {
        this.y = this.parent.categoryComponent.getY() + this.bind;
        this.x = this.parent.categoryComponent.getX();
    }

    public void onClick(int x, int y, int b) {
        if (this.getSetting() != null && !this.getSetting().isVisible()) return;

        if (this.i(x, y) && this.parent.po && this.parent.mod.canBeEnabled()) {
            if (b == 0) {
                this.isBinding = !this.isBinding;
            }
            else if (b == 1 && this.parent.mod.moduleCategory() != Module.category.profiles) {
                this.parent.mod.setHidden(!this.parent.mod.isHidden());
                if (Raven.currentProfile != null) {
                    ((ProfileModule) Raven.currentProfile.getModule()).saved = false;
                }
            }
            else if (b > 1) {
                if (this.isBinding) {
                    this.parent.mod.setBind(b + 1000);
                    if (Raven.currentProfile != null) {
                        ((ProfileModule) Raven.currentProfile.getModule()).saved = false;
                    }
                    this.isBinding = false;
                }
            }
        }
    }

    public void keyTyped(char t, int keybind) {
        if (this.isBinding) {
            if (keybind == Keyboard.KEY_0 || keybind == Keyboard.KEY_ESCAPE) {
                if (this.parent.mod instanceof Gui) {
                    this.parent.mod.setBind(54);
                } else {
                    this.parent.mod.setBind(0);
                }
                if (Raven.currentProfile != null) {
                    ((ProfileModule) Raven.currentProfile.getModule()).saved = false;
                }
            } else {
                if (Raven.currentProfile != null) {
                    ((ProfileModule) Raven.currentProfile.getModule()).saved = false;
                }
                this.parent.mod.setBind(keybind);
            }

            this.isBinding = false;
        }
    }

    public boolean i(int x, int y) {
        return x > this.x && x < this.x + this.parent.categoryComponent.gw() && y > this.y - 1 && y < this.y + 12;
    }

    public int gh() {
        return 16;
    }

    private void drawString(String s) {
        getFont().drawStringWithShadow(s, (float) ((this.parent.categoryComponent.getX() + 4) * 2), (float) ((this.parent.categoryComponent.getY() + this.bind + 3) * 2), Gui.getColor());
    }

    public void onGuiClosed() {
        this.isBinding = false;
    }
}
