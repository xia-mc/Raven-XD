package keystrokesmod.module.impl.movement;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public class InvMove extends Module {

    public static ButtonSetting Sprint;
    //  public static ButtonSetting NoMoveClick;
    public static ButtonSetting Spoof;

    public InvMove() {
        super("InvMove", Module.category.movement);
        this.registerSetting(Sprint = new ButtonSetting("Sprint", true));
        //    this.registerSetting(NoMoveClick = new ButtonSetting("No Move Click", false));
      //  this.registerSetting(Spoof = new ButtonSetting("Spoof", false));
    }

    public void onUpdate() {
        if (mc.currentScreen != null) {
            if (mc.currentScreen instanceof GuiChat) {
                return;
            }

            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()));
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode()));
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode()));
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode()));
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()));
            // KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
            EntityPlayerSP playerEntity;

            if (Sprint.isToggled()) {
                mc.thePlayer.setSprinting(true);
            } else {
                mc.thePlayer.setSprinting(false);
            }

            if (Keyboard.isKeyDown(208) && mc.thePlayer.rotationPitch < 90.0F) {
                playerEntity = mc.thePlayer;
                playerEntity.rotationPitch += 6.0F;
            }

            if (Keyboard.isKeyDown(200) && mc.thePlayer.rotationPitch > -90.0F) {
                playerEntity = mc.thePlayer;
                playerEntity.rotationPitch -= 6.0F;
            }

            if (Keyboard.isKeyDown(205)) {
                playerEntity = mc.thePlayer;
                playerEntity.rotationYaw += 6.0F;
            }

            if (Keyboard.isKeyDown(203)) {
                playerEntity = mc.thePlayer;
                playerEntity.rotationYaw -= 6.0F;
            }
        }
    }
}