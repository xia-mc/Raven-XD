package keystrokesmod.module.impl.combat.ragebot;

import keystrokesmod.module.impl.combat.RageBot;
import keystrokesmod.module.setting.impl.SubMode;
import org.jetbrains.annotations.NotNull;

public class IRageBotFeature extends SubMode<RageBot> {
    public IRageBotFeature(String name, @NotNull RageBot parent) {
        super(name, parent);
    }

    public void onFire() {}
}
