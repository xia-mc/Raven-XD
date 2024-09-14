package keystrokesmod.module.impl.combat;

import keystrokesmod.event.MoveInputEvent;
import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.SprintEvent;
import keystrokesmod.mixins.impl.client.KeyBindingAccessor;
import keystrokesmod.module.impl.combat.morekb.IMoreKB;
import keystrokesmod.module.impl.combat.morekb.SimpleSprintReset;
import keystrokesmod.module.setting.impl.ModeValue;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public class MoreKB extends IMoreKB {
    private final ModeValue mode;

    public MoreKB() {
        super("MoreKB", category.combat);
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new SimpleSprintReset("Legit", this))
                .add(new SimpleSprintReset("LegitSneak", this))
                .add(new SimpleSprintReset("LegitFast", this))
                .add(new SimpleSprintReset("Fast", this))
                .add(new SimpleSprintReset("Packet", this))
                .add(new SimpleSprintReset("LegitBlock", this))
                .add(new SimpleSprintReset("LegitInv", this))
                .add(new SimpleSprintReset("STap", this))
                .setDefaultValue("LegitFast")
        );
    }

    @Override
    public void onEnable() throws Exception {
        mode.enable();
    }

    @Override
    public void onDisable() throws Exception {
        mode.disable();
    }

    @SubscribeEvent
    public void onMoveInput(MoveInputEvent event) {
        if (noSprint() && MoveUtil.isMoving()) {
            switch ((int) mode.getInput()) {
                case 1:
                    event.setSneak(true);
                    break;
                case 3:
                    event.setForward(0.7999f);
                    break;
            }
        }
    }

    @SubscribeEvent
    public void onSprint(SprintEvent event) {
        if (noSprint() && MoveUtil.isMoving() && (int) mode.getInput() == 2) {
            event.setSprint(false);
        }
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (noSprint() && MoveUtil.isMoving()) {
            if ((int) mode.getInput() == 4) {
                event.setSprinting(false);
            }
        }
    }

    @Override
    public void stopSprint() {
        super.stopSprint();
        switch ((int) mode.getInput()) {
            case 7:
                ((KeyBindingAccessor) mc.gameSettings.keyBindBack).setPressed(true);
            case 0:
                ((KeyBindingAccessor) mc.gameSettings.keyBindForward).setPressed(false);
                break;
            case 5:
                Utils.sendClick(1, true);
                break;
            case 6:
                ((KeyBindingAccessor) mc.gameSettings.keyBindInventory).setPressed(true);
                KeyBinding.onTick(mc.gameSettings.keyBindInventory.getKeyCode());
                ((KeyBindingAccessor) mc.gameSettings.keyBindInventory).setPressed(false);
                KeyBinding.onTick(mc.gameSettings.keyBindInventory.getKeyCode());
                break;
        }
    }

    @Override
    public void reSprint() {
        super.reSprint();
        switch ((int) mode.getInput()) {
            case 7:
                ((KeyBindingAccessor) mc.gameSettings.keyBindBack).setPressed(Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()));
            case 0:
                ((KeyBindingAccessor) mc.gameSettings.keyBindForward).setPressed(Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()));
                break;
            case 5:
                Utils.sendClick(1, false);
                break;
            case 6:
                if (mc.currentScreen instanceof GuiInventory)
                    mc.thePlayer.closeScreen();
                break;
        }
    }

    @Override
    public String getInfo() {
        return mode.getSelected().getPrettyName();
    }
}
