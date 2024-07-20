package keystrokesmod.module.impl.combat.autoclicker;

import keystrokesmod.module.impl.other.RecordClick;
import keystrokesmod.module.setting.impl.SubMode;
import org.jetbrains.annotations.NotNull;

public class RecordAutoClicker extends SubMode<IAutoClicker> {
    private final boolean left;
    private final boolean always;

    public RecordAutoClicker(String name, @NotNull IAutoClicker parent, boolean left, boolean always) {
        super(name, parent);
        this.left = left;
        this.always = always;
    }

    @Override
    public void onUpdate() {
        if (!always && left ? !mc.gameSettings.keyBindAttack.isKeyDown() : !mc.gameSettings.keyBindUseItem.isKeyDown())
            return;
        if (System.currentTimeMillis() < RecordClick.getNextClickTime())
            return;

        if (parent.click())
            RecordClick.click();
    }
}
