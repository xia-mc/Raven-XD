package keystrokesmod.event;

import keystrokesmod.utility.Utils;
import lombok.*;
import net.minecraftforge.fml.common.eventhandler.Event;

import static keystrokesmod.Raven.mc;

@Getter
public class EyeHeightEvent extends Event {
    private double y;
    private boolean set;

    public EyeHeightEvent(double eyeHeight) {
        setEyeHeight(eyeHeight);
    }

    public double getEyeHeight() {
        return 1.62 - (mc.thePlayer.lastTickPosY +
                        (((mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * Utils.getTimer().renderPartialTicks)) - y);
    }

    public void setEyeHeight(double targetEyeHeight) {
        this.y = targetEyeHeight - 1.62 + mc.thePlayer.lastTickPosY +
                ((mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * (double) Utils.getTimer().renderPartialTicks);
    }

    public void setY(double y) {
        this.y = y;
        this.set = true;
    }
}
