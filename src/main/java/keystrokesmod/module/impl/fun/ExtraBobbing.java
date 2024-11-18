package keystrokesmod.module.impl.fun;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.SliderSetting;
import net.minecraft.client.entity.EntityPlayerSP;

public class ExtraBobbing extends Module {
    public SliderSetting level;
    private boolean b;

    public ExtraBobbing() {
        super("Extra Bobbing", Module.category.fun, 0);
        this.registerSetting(level = new SliderSetting("Level", 1.0D, 0.0D, 8.0D, 0.1D));
    }

    public void onEnable() {
        this.b = mc.gameSettings.viewBobbing;
        if (!this.b) {
            mc.gameSettings.viewBobbing = true;
        }

    }

    public void onDisable() {
        mc.gameSettings.viewBobbing = this.b;
    }

    public void onUpdate() {
        if (!mc.gameSettings.viewBobbing) {
            mc.gameSettings.viewBobbing = true;
        }

        if (mc.thePlayer.movementInput.moveForward != 0.0F || mc.thePlayer.movementInput.moveStrafe != 0.0F) {
            EntityPlayerSP var10000 = mc.thePlayer;
            var10000.cameraYaw = (float) ((double) var10000.cameraYaw + level.getInput() / 2.0D);
        }
    }
}
