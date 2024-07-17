package keystrokesmod.module.impl.world.tower;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.module.impl.world.Tower;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import static keystrokesmod.module.ModuleManager.tower;

public class VanillaTower extends SubMode<Tower> {
    private final SliderSetting speed;
    private final SliderSetting diagonalSpeed;
    private final SliderSetting slowedSpeed;
    private final SliderSetting slowedTicks;
    private int slowTicks;
    private boolean wasTowering;

    public VanillaTower(String name, @NotNull Tower parent) {
        super(name, parent);
        this.registerSetting(speed = new SliderSetting("Speed", 0.95, 0.5, 1, 0.01));
        this.registerSetting(diagonalSpeed = new SliderSetting("Diagonal speed", 5, 0, 10, 0.1));
        this.registerSetting(slowedSpeed = new SliderSetting("Slowed speed", 2, 0, 9, 0.1));
        this.registerSetting(slowedTicks = new SliderSetting("Slowed ticks", 1, 0, 20, 1));
    }

    @Override
    public void onDisable() {
        wasTowering = false;
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent e) throws IllegalAccessException {
        if (tower.canTower()) {
            wasTowering = true;
                Utils.setSpeed(Math.max((tower.diagonal() ? diagonalSpeed.getInput() : speed.getInput()) * 0.1 - 0.25, 0));
                mc.thePlayer.jump();
        } else {
            if (wasTowering && slowedTicks.getInput() > 0 && tower.modulesEnabled()) {
                if (slowTicks++ < slowedTicks.getInput()) {
                    Utils.setSpeed(Math.max(slowedSpeed.getInput() * 0.1 - 0.25, 0));
                }
                else {
                    slowTicks = 0;
                    wasTowering = false;
                }
            }
            else {
                if (wasTowering) {
                    wasTowering = false;
                }
                slowTicks = 0;
            }
        }
    }

}
