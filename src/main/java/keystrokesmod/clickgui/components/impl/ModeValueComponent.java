package keystrokesmod.clickgui.components.impl;

import keystrokesmod.Raven;
import keystrokesmod.clickgui.components.Component;
import keystrokesmod.module.setting.Setting;
import keystrokesmod.module.setting.impl.ModeValue;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class ModeValueComponent extends Component {
    public static final double SCALE_FACTOR = 0.5D;
    private final ModeValue modeValue;

    public ModeValueComponent(ModeValue modeValue, ModuleComponent moduleComponent, int o) {
        super(moduleComponent);
        this.modeValue = modeValue;
        this.x = moduleComponent.categoryComponent.getX() + moduleComponent.categoryComponent.gw();
        this.y = moduleComponent.categoryComponent.getY() + moduleComponent.o;
        this.o = o;
    }

    @Override
    public Setting getSetting() {
        return modeValue;
    }

    public void render() {
        GL11.glPushMatrix();
        GL11.glScaled(SCALE_FACTOR, SCALE_FACTOR, SCALE_FACTOR);

        String value = this.modeValue.getSubModeValues().get((int) this.modeValue.getInput()).getRawPrettyName();
        Raven.mc.fontRendererObj.drawString(
                this.modeValue.getName() + ": " + value,
                (float) ((int) ((float) (this.parent.categoryComponent.getX() + 4) * 2.0F)),
                (float) ((int) ((float) (this.parent.categoryComponent.getY() + this.o + 3) * 2.0F)),
                color, true
        );

        GL11.glPopMatrix();
    }

    public void so(int n) {
        this.o = n;
    }

    public void onDrawScreen(int x, int y) {
        this.y = this.parent.categoryComponent.getY() + this.o;
        this.x = this.parent.categoryComponent.getX();
    }

    public void onClick(int x, int y, int b) {
        if (this.getSetting() != null && !this.getSetting().isVisible()) return;

        if (isHover(x, y) && this.parent.po) {
            changeValue(b, Keyboard.isKeyDown(Raven.mc.gameSettings.keyBindSneak.getKeyCode()));
            parent.categoryComponent.render();
        }
    }

    private void changeValue(int b, boolean reserve) {
        boolean next;
        switch (b) {
            case 0:
                next = true;
                break;
            case 1:
                next = false;
                break;
            default:
                return;
        }

        if (reserve)
            next = !next;

        if (next) {
            this.modeValue.nextValue();
        } else {
            this.modeValue.prevValue();
        }
    }
}
