package keystrokesmod.module.impl.movement;

import keystrokesmod.module.Module;
import keystrokesmod.utility.Utils;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class Sprint extends Module {
    public Sprint() {
        super("Sprint", Module.category.movement, 0);
    }

    @SubscribeEvent
    public void KeepSprint(PlayerTickEvent e) {
        if (Utils.nullCheck() && mc.inGameHasFocus) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
        }
    }
}
