package keystrokesmod.module.impl.movement;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.SprintEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.movement.Move;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

public class Sprint extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", new String[]{"Legit", "Omni"}, 0);
    private final ModeSetting omniMode = new ModeSetting("Bypass mode", new String[]{"None", "Legit"}, 1, new ModeOnly(mode, 1));
    private final ButtonSetting disableWhileScaffold;
  
    public static boolean omni = false;
    public static boolean stopSprint = false;

    public Sprint() {
        super("Sprint", Module.category.movement, 0);
        this.registerSetting(mode, omniMode);
        // This setting is only applicable in legit mode as omni mode handles sprinting differently
        this.registerSetting(disableWhileScaffold = new ButtonSetting("Disable While Scaffold", false, new ModeOnly(mode, 0)));
    }

    public static boolean omni() {
        final SprintEvent event = new SprintEvent(
                MoveUtil.canSprint(true),
                omni || (ModuleManager.sprint != null && ModuleManager.sprint.isEnabled() && ModuleManager.sprint.mode.getInput() == 1)
        );

        return event.isSprint() && event.isOmni();
    }

    public static boolean stopSprint() {
        final SprintEvent event = new SprintEvent(!stopSprint, false);

        return !event.isSprint();
    }

    @SubscribeEvent
    public void p(PlayerTickEvent e) {
        if (Utils.nullCheck() && mc.inGameHasFocus) {

            if (ModuleManager.scaffold.isEnabled() && disableWhileScaffold.isToggled()) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
            } else {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
            }

        }
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (mode.getInput() != 1) return;

        switch ((int) omniMode.getInput()) {
            case 0:
                break;
            case 1:
                event.setYaw(event.getYaw() + Move.fromMovement(mc.thePlayer.moveForward, mc.thePlayer.moveStrafing).getDeltaYaw());
                break;
        }

        if (MoveUtil.isMoving())
            mc.thePlayer.setSprinting(true);
    }
}
