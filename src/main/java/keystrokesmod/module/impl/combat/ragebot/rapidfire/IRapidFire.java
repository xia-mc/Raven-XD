package keystrokesmod.module.impl.combat.ragebot.rapidfire;

import keystrokesmod.module.impl.combat.RageBot;
import keystrokesmod.module.impl.combat.ragebot.IRageBotFeature;
import keystrokesmod.utility.RageBotUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Set;

public class IRapidFire extends IRageBotFeature {
    public IRapidFire(String name, @NotNull RageBot parent) {
        super(name, parent);
    }

    public int getBestArm() {
        return getBestArm(Collections.emptySet());
    }

    public int getBestArm(Set<Integer> ignoreSlots) {
        int arm;

        switch ((int) parent.weaponMode.getInput()) {
            default:
            case 0:
                arm = RageBotUtils.getArmHypixelBedWars(ignoreSlots);
                break;
            case 1:
                arm = RageBotUtils.getArmHypixelZombie(ignoreSlots);
                break;
            case 2:
                arm = RageBotUtils.getArmCubeCraft(ignoreSlots);
                break;
            case 3:
                arm = RageBotUtils.getArmCSGO(ignoreSlots);
                break;
        }

        return arm;
    }
}
