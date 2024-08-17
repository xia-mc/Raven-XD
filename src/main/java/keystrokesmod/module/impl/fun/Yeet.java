package keystrokesmod.module.impl.fun;

import keystrokesmod.module.Module;
import net.minecraft.crash.CrashReport;


public class Yeet extends Module {
    public Yeet() {
        super("Yeet", category.fun, "Yeet!");
    }

    @Override
    public  void onEnable(){
        this.disable();
        mc.crashed(new CrashReport("Yeet!", new YeetException()));
    }

    public static class YeetException extends RuntimeException {
        public YeetException() {
            super("Yeet!");
        }
    }
}