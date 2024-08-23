package keystrokesmod.module.impl.combat;

import keystrokesmod.event.MoveInputEvent;
import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.SprintEvent;
import keystrokesmod.mixins.impl.client.KeyBindingAccessor;
import keystrokesmod.module.impl.combat.morekb.IMoreKB;
import keystrokesmod.module.impl.combat.morekb.impl.SimpleSprintReset;
import keystrokesmod.module.setting.impl.ModeValue;
import keystrokesmod.utility.MoveUtil;
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
                .setDefaultValue("LegitFast")
        );
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
        if ((int) mode.getInput() == 0) {
            ((KeyBindingAccessor) mc.gameSettings.keyBindForward).setPressed(false);
        }
    }

    @Override
    public void reSprint() {
        super.reSprint();
        if ((int) mode.getInput() == 0) {
            ((KeyBindingAccessor) mc.gameSettings.keyBindForward).setPressed(Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()));
        }
    }

    @Override
    public String getInfo() {
        return mode.getSelected().getPrettyName();
    }
}
