package keystrokesmod.module.impl.movement.longjump;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.module.impl.movement.LongJump;
import keystrokesmod.module.setting.impl.SubMode;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class VulcanLongJump extends SubMode<LongJump> {
    private int ticks;

    public VulcanLongJump(String name, @NotNull LongJump parent) {
        super(name, parent);
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
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 9.9, mc.thePlayer.posZ);
        }

        if (ticks > 0 && !(ticks >3)) {
            mc.thePlayer.motionY = 0;
            mc.thePlayer.onGround = true;

        }

        if (ticks > 3 && ticks % 2 == 0 & !mc.thePlayer.onGround) {
            mc.thePlayer.motionY = -0.155;

        } else if (!(ticks % 2 == 0 && !mc.thePlayer.onGround)) {
            mc.thePlayer.motionY = -0.098;
        }
    }
}
