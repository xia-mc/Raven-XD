package keystrokesmod.module.impl.movement.longjump;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.module.impl.movement.LongJump;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SubMode;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class VulcanLongJump extends SubMode<LongJump> {
    private final ButtonSetting teleport;
    private final ButtonSetting autoDisable;

    private int ticks;

    public VulcanLongJump(String name, @NotNull LongJump parent) {
        super(name, parent);
        this.registerSetting(teleport = new ButtonSetting("Teleport", true));
        this.registerSetting(autoDisable = new ButtonSetting("Auto disable", true));
    }

    @Override
    public void onEnable() {
        ticks = 0;
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        ticks++;
        if (ticks == 1) {
            mc.thePlayer.motionY = 0;
            mc.thePlayer.onGround = true;
            if (teleport.isToggled())
                mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 9.9, mc.thePlayer.posZ);
        }

        if (ticks > 0 && !(ticks > 3)) {
            mc.thePlayer.motionY = 0;
            mc.thePlayer.onGround = true;

        }

        if (ticks > 3 && mc.thePlayer.onGround && autoDisable.isToggled()) {
            parent.disable();
        }

        if (ticks > 3 && ticks % 2 == 0 & !mc.thePlayer.onGround) {
            mc.thePlayer.motionY = -0.155;

        } else if (!(ticks % 2 == 0 && !mc.thePlayer.onGround)) {
            mc.thePlayer.motionY = -0.098;
        }
    }
}
