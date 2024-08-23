package keystrokesmod.module.impl.combat.morekb;

import keystrokesmod.module.Module;

public abstract class IMoreKB extends Module {
    private boolean canSprint = true;

    public IMoreKB(String name, category moduleCategory) {
        super(name, moduleCategory);
    }

    public void stopSprint() {
        canSprint = false;
    }

    public void reSprint() {
        canSprint = true;
    }

    protected boolean noSprint() {
        return !canSprint;
    }
}
