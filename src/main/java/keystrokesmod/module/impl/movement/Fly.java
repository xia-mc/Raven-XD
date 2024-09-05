package keystrokesmod.module.impl.movement;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.movement.fly.*;
import keystrokesmod.module.setting.impl.*;
import keystrokesmod.utility.render.RenderUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class Fly extends Module {
    public final ModeValue mode;
    private final ButtonSetting showBPS;
    private final ButtonSetting stopAtEnd;

    public Fly() {
        super("Fly", category.movement);
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new Vanilla1Fly("Vanilla1", this))
                .add(new Vanilla2Fly("Vanilla2", this))
                .add(new AirWalkFly("AirWalk", this))
                .add(new AirPlaceFly("AirPlace", this))
                .add(new VulcanFly("Vulcan", this))
                .add(new MatrixBowFly("MatrixBow", this))
                .add(new MatrixTNTFly("MatrixTNT", this))
                .add(new FakeFly("Fake", this))
                .add(new TestFly("Test", this))
                .add(new PolarTestFly("PolarTest", this))
        );
        this.registerSetting(showBPS = new ButtonSetting("Show BPS", false));
        this.registerSetting(stopAtEnd = new ButtonSetting("Stop at end", false));
    }

    @Override
    public String getInfo() {
        return mode.getSubModeValues().get((int) mode.getInput()).getPrettyName();
    }

    public void onEnable() {
        mode.enable();
    }

    public void onDisable() {
        mode.disable();

        if (stopAtEnd.isToggled()) {
            mc.thePlayer.motionZ = 0;
            mc.thePlayer.motionY = 0;
            mc.thePlayer.motionX = 0;
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent e) {
        RenderUtils.renderBPS(showBPS.isToggled(), e);
    }
}
