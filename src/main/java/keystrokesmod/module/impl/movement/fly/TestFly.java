package keystrokesmod.module.impl.movement.fly;

import keystrokesmod.module.impl.movement.Fly;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.network.play.client.C03PacketPlayer;
import org.jetbrains.annotations.NotNull;

public class TestFly extends SubMode<Fly> {
    private int enableTicks = 0;

    private final SliderSetting extraSpeed;
    private final SliderSetting timer;
    private final ButtonSetting resetMotion;
    private final SliderSetting resetEveryTicks;
    private final ButtonSetting groundSpoof;

    public TestFly(String name, @NotNull Fly parent) {
        super(name, parent);
        this.registerSetting(extraSpeed = new SliderSetting("Extra Speed", 0.0, 0.0, 0.3, 0.01));
        this.registerSetting(timer = new SliderSetting("Timer", 0.42, 0.2, 1, 0.01));
        this.registerSetting(resetMotion = new ButtonSetting("Reset motion", false));
        this.registerSetting(resetEveryTicks = new SliderSetting("Reset every ticks", 5, 2, 10, 1, resetMotion::isToggled));
        this.registerSetting(groundSpoof = new ButtonSetting("Ground spoof", false, resetMotion::isToggled));
    }

    @Override
    public void onEnable() throws Throwable {
        enableTicks = 0;
    }

    @Override
    public void onUpdate() throws Throwable {
        if (mc.thePlayer.onGround) {
            enableTicks = 0;
            Utils.resetTimer();
            return;
        } else {
            enableTicks++;
        }

        Utils.getTimer().timerSpeed = (float) timer.getInput();
        MoveUtil.moveFlying(extraSpeed.getInput());
        if (resetMotion.isToggled()) {
            if (enableTicks % (int) resetEveryTicks.getInput() == 0) {
                mc.thePlayer.motionY *= 0.1;
                if (groundSpoof.isToggled())
                    PacketUtils.sendPacket(new C03PacketPlayer(true));
            }
        }
    }

    @Override
    public void onDisable() throws Throwable {
        Utils.resetTimer();
    }
}
