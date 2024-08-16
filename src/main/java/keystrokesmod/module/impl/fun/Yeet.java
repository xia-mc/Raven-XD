package keystrokesmod.module.impl.fun;

import keystrokesmod.module.Module;


public class Yeet extends Module {
    public Yeet() {
        super("Yeet", category.fun);

    }
    @Override
    public  void onEnable(){
        this.disable();
        System.exit(0);
    }
}