package keystrokesmod.module.impl.movement.longjump;

import keystrokesmod.module.impl.movement.LongJump;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;

public class GrimBoatLongJump extends SubMode<LongJump> {
    private final SliderSetting horizontalSpeed;
    private final SliderSetting verticalSpeed;
    private final ButtonSetting timer;
    private final SliderSetting balanceTimer;
    private final ButtonSetting autoDisable;

    private boolean active = false;

    public GrimBoatLongJump(String name, @NotNull LongJump parent) {
        super(name, parent);
        this.registerSetting(horizontalSpeed = new SliderSetting("Horizontal speed", 1, 0.1, 2, 0.01));
        this.registerSetting(verticalSpeed = new SliderSetting("Vertical speed", 1, 0.1, 2, 0.01));
        this.registerSetting(timer = new ButtonSetting("Timer", false));
        this.registerSetting(balanceTimer = new SliderSetting("timer", 0.5, 0.1, 0.8, 0.01, timer::isToggled));
        this.registerSetting(autoDisable = new ButtonSetting("Auto disable", false));
    }

    @Override
    public void onEnable() {
        active = false;
    }

    @Override
    public void onDisable() {
        Utils.resetTimer();
    }

    @Override
    public void onUpdate() {
        if (isActive()) {
            if (active) {
                MoveUtil.strafe(horizontalSpeed.getInput());
                mc.thePlayer.motionY = verticalSpeed.getInput();
            }
            active = true;
        } else {
            if (active && autoDisable.isToggled())
                parent.disable();
            active = false;
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (!Utils.nullCheck() || !timer.isToggled()) return;

        if (active) {
            Utils.getTimer().timerSpeed = (float) balanceTimer.getInput();
        } else {
            Utils.resetTimer();
        }
    }

    private boolean isActive() {
        if (!Utils.nullCheck()) return false;

        final AxisAlignedBB grimBox = mc.thePlayer.getEntityBoundingBox().expand(1, 1, 1);
        return mc.theWorld.loadedEntityList.parallelStream()
                .filter(e -> e instanceof EntityBoat)
                .anyMatch(e -> e.getCollisionBoundingBox().intersectsWith(grimBox));
    }
}
