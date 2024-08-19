package keystrokesmod.module.impl.fun;

import keystrokesmod.Raven;
import keystrokesmod.module.Module;
import net.minecraft.crash.CrashReport;


public class Yeet extends Module {
    private int enableTicks = 0;

    public Yeet() {
        super("Yeet", category.fun, "Yeet!");
    }

    @Override
    public void onEnable(){
        enableTicks = 0;
        Raven.mc.thePlayer.playSound("keystrokesmod:yeet", 1, 1);
        mc.ingameGUI.displayTitle("还是PVP大佬", "", 10, 10, 10);
    }

    @Override
    public void onUpdate() {
        enableTicks++;

        if (enableTicks == 20) {
            this.disable();
            for (int i = 0; i < 10; i++) {
                mc.crashed(new CrashReport("Yeet!", new YeetException()));
            }
        }
    }

    public static class YeetException extends RuntimeException {
        public YeetException() {
            super("Yeet!");
        }
    }
}