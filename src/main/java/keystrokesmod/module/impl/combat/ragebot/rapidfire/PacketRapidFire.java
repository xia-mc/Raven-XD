package keystrokesmod.module.impl.combat.ragebot.rapidfire;

import keystrokesmod.module.impl.combat.RageBot;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import org.jetbrains.annotations.NotNull;

public class PacketRapidFire extends LegitRapidFire {
    private final SliderSetting amount;

    public PacketRapidFire(String name, @NotNull RageBot parent) {
        super(name, parent);
        this.registerSetting(amount = new SliderSetting("Amount", 5, 1, 10, 1));
    }

    @Override
    public void onFire() {
        for (int i = 0; i < (int) amount.getInput(); i++) {
            int bestArm = getBestArm();
            SlotHandler.setCurrentSlot(bestArm);
            Utils.sendClick(1, true);
        }
    }
}
