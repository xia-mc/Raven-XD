package keystrokesmod.module.impl.combat.ragebot.nospread;

import keystrokesmod.module.impl.combat.RageBot;
import keystrokesmod.module.impl.combat.ragebot.IRageBotFeature;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.utility.PacketUtils;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import org.jetbrains.annotations.NotNull;

public class SwitchNoSpread extends IRageBotFeature {
    public SwitchNoSpread(String name, @NotNull RageBot parent) {
        super(name, parent);
    }

    @Override
    public void onFire() {
        PacketUtils.sendPacket(new C09PacketHeldItemChange(SlotHandler.getCurrentSlot() % 8 + 1));
        PacketUtils.sendPacket(new C09PacketHeldItemChange(SlotHandler.getCurrentSlot()));
    }
}
