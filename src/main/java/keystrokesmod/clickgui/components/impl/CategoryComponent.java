package keystrokesmod.clickgui.components.impl;

import keystrokesmod.Raven;
import keystrokesmod.clickgui.components.IComponent;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.client.Gui;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.Timer;
import keystrokesmod.utility.font.Font;
import keystrokesmod.utility.render.RenderUtils;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.profile.Manager;
import keystrokesmod.utility.profile.Profile;
import keystrokesmod.utility.render.Animation;
import keystrokesmod.utility.render.Easing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class CategoryComponent {
    public List<ModuleComponent> modules = new CopyOnWriteArrayList<>();
    public Module.category categoryName;
    private boolean categoryOpened;
    private Timer smoothTimer;
    private int width;
    private int y;
    private int x;
    private int buttonHeight;
    public boolean dragging;
    public int dragStartX;
    public int dragStartY;
    public boolean n4m = false;
    public String pvp;
    public boolean pin = false;
    public boolean hovering = false;
    private final Animation openCloseAnimation;
    public int scale;
    private float big;
    private final int translucentBackground = new Color(0, 0, 0, 110).getRGB();
    private final int background = new Color(0, 0, 0, 255).getRGB();
    private final int regularOutline = new Color(81, 99, 149).getRGB();
    private final int regularOutline2 = new Color(97, 67, 133).getRGB();
    private final int categoryNameColor = new Color(220, 220, 220).getRGB();
    private final int categoryCloseColor = new Color(250, 95, 85).getRGB();
    private final int categoryOpenColor = new Color(135, 238, 144).getRGB();

    public CategoryComponent(Module.category category) {
        this.categoryName = category;
        this.width = 92;
        this.x = 5;
        this.y = 5;
        this.buttonHeight = 13;
        this.smoothTimer = null;
        this.dragStartX = 0;
        this.categoryOpened = false;
        this.dragging = false;
        int tY = this.buttonHeight + 3;
        this.scale = new ScaledResolution(Minecraft.getMinecraft()).getScaleFactor();
        this.openCloseAnimation = new Animation(Easing.EASE_OUT_QUART, 600); // EASE_OUT_QUART

        for (Module mod : Raven.getModuleManager().inCategory(this.categoryName)) {
            if (mod instanceof SubMode) {
                continue;
            }

            ModuleComponent b = new ModuleComponent(mod, this, tY);
            this.modules.add(b);
            tY += 16;
        }
    }

    public List<ModuleComponent> getModules() {
        return this.modules;
    }

    public void reloadModules(boolean isProfile) {
        this.modules.clear();
        this.buttonHeight = 13;
        int tY = this.buttonHeight + 3;

        if ((this.categoryName == Module.category.profiles && isProfile) || (this.categoryName == Module.category.scripts && !isProfile)) {
            ModuleComponent manager = new ModuleComponent(isProfile ? new Manager() : new keystrokesmod.script.Manager(), this, tY);
            this.modules.add(manager);

            if ((Raven.profileManager == null && isProfile) || (Raven.scriptManager == null && !isProfile)) {
                return;
            }

            if (isProfile) {
                for (Profile profile : Raven.profileManager.profiles) {
                    if (Objects.equals(profile.getName(), "latest")) continue;
                    tY += 16;
                    ModuleComponent b = new ModuleComponent(profile.getModule(), this, tY);
                    this.modules.add(b);
                }
            }
            else {
                for (Module module : Raven.scriptManager.scripts.values()) {
                    if (module instanceof SubMode)
                        continue;

                    tY += 16;
                    ModuleComponent b = new ModuleComponent(module, this, tY);
                    this.modules.add(b);
                }
            }
        }
    }

    public void x(int n) {
        this.x = n;
    }

    public void y(int y) {
        this.y = y;
    }

    public void d(boolean d) {
        this.dragging = d;
    }

    public boolean p() {
        return this.pin;
    }

    public void cv(boolean on) {
        this.pin = on;
    }

    public boolean fv() {
        return this.categoryOpened;
    }

    public void fv(boolean open) {
        this.categoryOpened = open;
    }

    public void mouseClicked(boolean on) {
        this.categoryOpened = on;
        (this.smoothTimer = new Timer(600)).start();
        this.openCloseAnimation.reset();
        this.openCloseAnimation.setDestinationValue(on ? 1 : 0);
    }

    public void rf(Font renderer) {
        this.width = 92;
        int h = 0;
        if (!this.modules.isEmpty() && this.categoryOpened) {
            IComponent c;
            for (Iterator<ModuleComponent> var3 = this.modules.iterator(); var3.hasNext(); h += c.gh()) {
                c = var3.next();
            }
            big = h;
        }

        this.openCloseAnimation.run(this.categoryOpened ? 1 : 0);
        float animationProgress = (float) this.openCloseAnimation.getValue();
        float extra = this.y + this.buttonHeight + 4 + (h * animationProgress);

        if (!this.categoryOpened) {
            if (smoothTimer == null) {
                extra = this.y + this.buttonHeight + (h * animationProgress) + 4;
            } else {
                float smoothValue = smoothTimer.getValueFloat(0, big, 1);
                extra = (this.y + this.buttonHeight + 4 + big) - smoothValue;
            }
        }

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        RenderUtils.scissor(0, this.y - 2, this.x + this.width + 4, extra - this.y + 4);
        RenderUtils.drawRoundedGradientOutlinedRectangle(
                this.x - 2, this.y, this.x + this.width + 2, extra, 9,
                Gui.translucentBackground.isToggled() ? translucentBackground : background,
                ((categoryOpened || hovering) && Gui.rainBowOutlines.isToggled()) ? RenderUtils.setAlpha(Utils.getChroma(2, 0), 0.5) : regularOutline,
                ((categoryOpened || hovering) && Gui.rainBowOutlines.isToggled()) ? RenderUtils.setAlpha(Utils.getChroma(2, 700), 0.5) : regularOutline2
        );
        renderer.drawString(this.n4m ? this.pvp : this.categoryName.name(), (float) (this.x + 2), (float) (this.y + 4), categoryNameColor, false);

        if (!this.n4m) {
            GL11.glPushMatrix();
            renderer.drawString(this.categoryOpened ? "-" : "+", (float) (this.x + 80), (float) ((double) this.y + 4.5D), this.categoryOpened ? categoryCloseColor : categoryOpenColor, false);
            GL11.glPopMatrix();

            if (this.categoryOpened && !this.modules.isEmpty()) {
                for (ModuleComponent module : this.modules) {
                    module.render();
                }
            }
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GL11.glPopMatrix();
    }

    public void render() {
        int o = this.buttonHeight + 3;

        IComponent c;
        for (Iterator<ModuleComponent> var2 = this.modules.iterator(); var2.hasNext(); o += c.gh()) {
            c = var2.next();
            c.so(o);
        }

    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int gw() {
        return this.width;
    }

    public void up(int x, int y) {
        if (this.dragging) {
            this.x(x - this.dragStartX);
            this.y(y - this.dragStartY);
        }
        hovering = overCategory(x, y);
    }

    public boolean i(int x, int y) {
        return x >= this.x + 92 - 13 && x <= this.x + this.width && (float) y >= (float) this.y + 2.0F && y <= this.y + this.buttonHeight + 1;
    }

    public boolean d(int x, int y) {
        return x >= this.x + 77 && x <= this.x + this.width - 6 && (float) y >= (float) this.y + 2.0F && y <= this.y + this.buttonHeight + 1;
    }

    public boolean overCategory(int x, int y) {
        return x >= this.x - 2 && x <= this.x + this.width + 2 && (float) y >= (float) this.y + 2.0F && y <= this.y + this.buttonHeight + 1;
    }

    public boolean v(int x, int y) {
        return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.buttonHeight;
    }
}
