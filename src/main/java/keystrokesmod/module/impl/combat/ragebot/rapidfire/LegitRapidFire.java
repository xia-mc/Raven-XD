package keystrokesmod.module.impl.combat.ragebot.rapidfire;

import keystrokesmod.module.impl.combat.RageBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class LegitRapidFire extends IRapidFire {
    private final ButtonSetting legitSwitch;
    private final Set<Integer> firedSlots = new HashSet<>();

    public LegitRapidFire(String name, @NotNull RageBot parent) {
        super(name, parent);
        this.registerSetting(legitSwitch = new ButtonSetting("Legit switch", false, this::notSelf));
    }

    @Override
    public int getBestArm() {
        if ((notSelf() && !legitSwitch.isToggled()))
            return super.getBestArm();
        int arm = super.getBestArm(firedSlots);

        if (arm == -1 && !firedSlots.isEmpty()) {
            firedSlots.clear();
            return getBestArm(firedSlots);
        }
        firedSlots.add(arm);

        return arm;
    }

    @Override
    public void onEnable() throws Throwable {
        firedSlots.clear();
    }

    private boolean notSelf() {
        return this.getClass() != LegitRapidFire.class;
    }
}
