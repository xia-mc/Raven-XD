package keystrokesmod.module.impl.combat.autoclicker;

import keystrokesmod.module.impl.other.RecordClick;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.Utils;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.jetbrains.annotations.NotNull;

public class RecordAutoClicker extends SubMode<IAutoClicker> {
    private final boolean left;

    public RecordAutoClicker(String name, @NotNull IAutoClicker parent, boolean left) {
        super(name, parent);
        this.left = left;
    }

    @Override
    public void onUpdate() {
        if (left ? !mc.gameSettings.keyBindAttack.isKeyDown() : !mc.gameSettings.keyBindUseItem.isKeyDown())
            return;
        if (System.currentTimeMillis() < RecordClick.getNextClickTime())
            return;

        if (mc.currentScreen == null) {
            Utils.sendClick(left ? 0 : 1, true);
            RecordClick.click();
        } else if (mc.currentScreen instanceof GuiContainer && parent.isInventoryFill()) {
            Utils.inventoryClick(mc.currentScreen);
            RecordClick.click();
        }
    }
}
