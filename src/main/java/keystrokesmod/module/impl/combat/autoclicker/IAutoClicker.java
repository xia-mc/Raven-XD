package keystrokesmod.module.impl.combat.autoclicker;

import keystrokesmod.module.Module;

public abstract class IAutoClicker extends Module {

    public IAutoClicker(String name, category moduleCategory) {
        super(name, moduleCategory);
    }

    public abstract boolean isInventoryFill();
}
