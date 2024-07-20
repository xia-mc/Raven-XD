package keystrokesmod.module.impl.combat;

import keystrokesmod.event.RightClickEvent;
import keystrokesmod.module.impl.combat.autoclicker.DragClickAutoClicker;
import keystrokesmod.module.impl.combat.autoclicker.IAutoClicker;
import keystrokesmod.module.impl.combat.autoclicker.NormalAutoClicker;
import keystrokesmod.module.impl.combat.autoclicker.RecordAutoClicker;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.ModeValue;
import keystrokesmod.utility.CoolDown;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class RightClicker extends IAutoClicker {
    private final ModeValue mode;
    private final ModeSetting clickSound;

    private final CoolDown coolDown = new CoolDown(100);

    public RightClicker() {
        super("RightClicker", category.combat);
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new NormalAutoClicker("Normal", this, false, false))
                .add(new DragClickAutoClicker("Drag Click", this, false, false))
                .add(new RecordAutoClicker("Record", this, false, false))
                .setDefaultValue("Normal")
        );
        this.registerSetting(clickSound = new ModeSetting("Click sound", new String[]{"None", "Standard", "Double", "Alan"}, 0));
    }

    @Override
    public void onEnable() {
        mode.enable();
    }

    @Override
    public void onDisable() {
        mode.disable();
    }

    @SubscribeEvent
    public void onClick(RightClickEvent event) {
        coolDown.start();

        if (clickSound.getInput() != 0) {
            mc.thePlayer.playSound(
                    "keystrokesmod:click." + clickSound.getOptions()[(int) clickSound.getInput()].toLowerCase()
                    , 1, 1
            );
        }
    }

    @Override
    public boolean click() {
        Utils.sendClick(1, true);
        return true;
    }
}
