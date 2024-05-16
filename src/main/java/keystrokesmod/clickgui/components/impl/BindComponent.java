package keystrokesmod.clickgui.components.impl;

import keystrokesmod.Raven;
import keystrokesmod.clickgui.components.Component;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.client.Gui;
import keystrokesmod.utility.Theme;
import keystrokesmod.utility.profile.ProfileModule;
import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class BindComponent extends Component {
    private boolean isBinding;
    private final ModuleComponent moduleComponent;
    private int bind;
    private int x;
    private int y;

    public BindComponent(ModuleComponent moduleComponent, int bind) {
        this.moduleComponent = moduleComponent;
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
        this.drawString(!this.moduleComponent.mod.canBeEnabled() && this.moduleComponent.mod.script == null ? "Module cannot be bound." : this.isBinding ? "Press a key..." : "Current bind: '§e" + (this.moduleComponent.mod.getKeycode() >= 1000 ? "M" + (this.moduleComponent.mod.getKeycode() - 1000) : Keyboard.getKeyName(this.moduleComponent.mod.getKeycode())) + "§r'");
        GL11.glPopMatrix();
    }

    public void drawScreen(int x, int y) {
        this.y = this.moduleComponent.categoryComponent.getY() + this.bind;
        this.x = this.moduleComponent.categoryComponent.getX();
    }

    public void onClick(int x, int y, int b) {
        if (this.i(x, y) && this.moduleComponent.po && this.moduleComponent.mod.canBeEnabled()) {
            if (b == 0) {
                this.isBinding = !this.isBinding;
            }
            else if (b == 1 && this.moduleComponent.mod.moduleCategory() != Module.category.profiles) {
                this.moduleComponent.mod.setVisibility(!this.moduleComponent.mod.isVisible());
                if (Raven.currentProfile != null) {
                    ((ProfileModule) Raven.currentProfile.getModule()).saved = false;
                }
            }
            else if (b > 1) {
                if (this.isBinding) {
                    this.moduleComponent.mod.setBind(b + 1000);
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
            if (keybind == 11) {
                if (this.moduleComponent.mod instanceof Gui) {
                    this.moduleComponent.mod.setBind(54);
                } else {
                    this.moduleComponent.mod.setBind(0);
                }
                if (Raven.currentProfile != null) {
                    ((ProfileModule) Raven.currentProfile.getModule()).saved = false;
                }
            } else {
                if (Raven.currentProfile != null) {
                    ((ProfileModule) Raven.currentProfile.getModule()).saved = false;
                }
                this.moduleComponent.mod.setBind(keybind);
            }

            this.isBinding = false;
        }
    }

    public boolean i(int x, int y) {
        return x > this.x && x < this.x + this.moduleComponent.categoryComponent.gw() && y > this.y - 1 && y < this.y + 12;
    }

    public int gh() {
        return 16;
    }

    private void drawString(String s) {
        Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(s, (float) ((this.moduleComponent.categoryComponent.getX() + 4) * 2), (float) ((this.moduleComponent.categoryComponent.getY() + this.bind + 3) * 2), this.moduleComponent.mod.visible ? Theme.getGradient(10, 0) : Theme.getGradient(11, 0));
    }

    public void onGuiClosed() {
        this.isBinding = false;
    }
}
