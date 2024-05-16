package keystrokesmod.clickgui.components.impl;

import keystrokesmod.Raven;
import keystrokesmod.clickgui.components.Component;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.utility.profile.ProfileModule;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ButtonComponent extends Component {
    private final int c = (new Color(20, 255, 0)).getRGB();
    private final Module mod;
    private final ButtonSetting buttonSetting;
    private final ModuleComponent p;
    private int o;
    private int x;
    private int y;

    public ButtonComponent(Module mod, ButtonSetting op, ModuleComponent b, int o) {
        this.mod = mod;
        this.buttonSetting = op;
        this.p = b;
        this.x = b.categoryComponent.getX() + b.categoryComponent.gw();
        this.y = b.categoryComponent.getY() + b.o;
        this.o = o;
    }

    public static void e() {
        GL11.glDisable(2929);
        GL11.glDisable(3553);
        GL11.glBlendFunc(770, 771);
        GL11.glDepthMask(true);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        GL11.glHint(3155, 4354);
    }

    public static void d() {
        GL11.glEnable(3553);
        GL11.glEnable(2929);
        GL11.glDisable(2848);
        GL11.glHint(3154, 4352);
        GL11.glHint(3155, 4352);
    }

    public static void d(float x, float y, float x1, float y1, int c) {
        e();
        b(c);
        d(x, y, x1, y1);
        d();
    }

    public static void d(float x, float y, float x1, float y1) {
        GL11.glBegin(7);
        GL11.glVertex2f(x, y1);
        GL11.glVertex2f(x1, y1);
        GL11.glVertex2f(x1, y);
        GL11.glVertex2f(x, y);
        GL11.glEnd();
    }

    public static void b(int h) {
        float a1pha = (float) (h >> 24 & 255) / 350.0F;
        GL11.glColor4f(0.0F, 0.0F, 0.0F, a1pha);
    }

    public void render() {
        GL11.glPushMatrix();
        GL11.glScaled(0.5D, 0.5D, 0.5D);
        Minecraft.getMinecraft().fontRendererObj.drawString((this.buttonSetting.isMethodButton ? "[=]  " : (this.buttonSetting.isToggled() ? "[+]  " : "[-]  ")) + this.buttonSetting.getName(), (float) ((this.p.categoryComponent.getX() + 4) * 2), (float) ((this.p.categoryComponent.getY() + this.o + 4) * 2), this.buttonSetting.isToggled() ? this.c : -1, false);
        GL11.glPopMatrix();
    }

    public void so(int n) {
        this.o = n;
    }

    public void drawScreen(int x, int y) {
        this.y = this.p.categoryComponent.getY() + this.o;
        this.x = this.p.categoryComponent.getX();
    }

    public void onClick(int x, int y, int b) {
        if (this.i(x, y) && b == 0 && this.p.po) {
            if (this.buttonSetting.isMethodButton) {
                this.buttonSetting.runMethod();
                return;
            }
            this.buttonSetting.toggle();
            this.mod.guiButtonToggled(this.buttonSetting);
            if (Raven.currentProfile != null) {
                ((ProfileModule) Raven.currentProfile.getModule()).saved = false;
            }
        }
    }

    public boolean i(int x, int y) {
        return x > this.x && x < this.x + this.p.categoryComponent.gw() && y > this.y && y < this.y + 11;
    }
}
