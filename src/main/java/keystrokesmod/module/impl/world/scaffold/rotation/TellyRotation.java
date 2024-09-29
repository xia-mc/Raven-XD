package keystrokesmod.module.impl.world.scaffold.rotation;

import keystrokesmod.event.JumpEvent;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.event.RotationEvent;
import keystrokesmod.event.ScaffoldPlaceEvent;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.impl.world.scaffold.IScaffoldRotation;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.aim.RotationData;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class TellyRotation extends IScaffoldRotation {
    private final SliderSetting straightTicks;
    private final SliderSetting diagonalTicks;
    private final SliderSetting jumpDownTicks;
    private final ButtonSetting extraSnap;

    private boolean noPlace = false;

    public TellyRotation(String name, @NotNull Scaffold parent) {
        super(name, parent);
        this.registerSetting(straightTicks = new SliderSetting("Straight ticks", 6, 1, 8, 1));
        this.registerSetting(diagonalTicks = new SliderSetting("Diagonal ticks", 4, 1, 8, 1));
        this.registerSetting(jumpDownTicks = new SliderSetting("Jump down ticks", 1, 1, 8, 1));
        this.registerSetting(extraSnap = new ButtonSetting("Extra snap", false));
    }

    @Override
    public void onEnable() throws Throwable {
        noPlace = false;
    }

    @Override
    public @NotNull RotationData onRotation(float placeYaw, float placePitch, boolean forceStrict, @NotNull RotationEvent event) {
        if (noPlace || (extraSnap.isToggled() && !parent.place)) {
            return new RotationData(event.getYaw(), event.getPitch());
        } else {
            return new RotationData(placeYaw, placePitch);
        }
    }

    @SubscribeEvent
    public void onScaffoldPlace(ScaffoldPlaceEvent event) {
        if (noPlace)
            event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public boolean onPreUpdate(PreUpdateEvent event) {
        if (parent.offGroundTicks == 0) {
            if (parent.onGroundTicks == 0)
                noPlace = true;
            else if (MoveUtil.isMoving() && !Utils.jumpDown())
                mc.thePlayer.jump();
        } else if (BlockUtils.insideBlock(mc.thePlayer.getEntityBoundingBox().offset(mc.thePlayer.motionX, mc.thePlayer.motionY + 0.1, mc.thePlayer.motionZ))) {
            noPlace = true;
        } else {
            if (Utils.jumpDown()) {
                if (parent.offGroundTicks >= (int) jumpDownTicks.getInput()) {
                    noPlace = false;
                }
            } else {
                if (Scaffold.isDiagonal()) {
                    if (parent.offGroundTicks == (int) diagonalTicks.getInput()) {
                        noPlace = false;
                    }
                } else {
                    if (parent.offGroundTicks == (int) straightTicks.getInput()) {
                        noPlace = false;
                    }
                }
            }
        }

        return true;
    }

    @SubscribeEvent
    public void onJump(JumpEvent event) {
        if (parent.offGroundTicks == 0 && parent.onGroundTicks == 0)
            event.setCanceled(true);
    }
}
