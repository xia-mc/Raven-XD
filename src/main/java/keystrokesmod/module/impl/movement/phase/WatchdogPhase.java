package keystrokesmod.module.impl.movement.phase;

import keystrokesmod.event.BlockAABBEvent;
import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.WorldChangeEvent;
import keystrokesmod.mixins.impl.client.PlayerControllerMPAccessor;
import keystrokesmod.module.impl.movement.Phase;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import net.minecraft.util.BlockPos;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import static keystrokesmod.module.ModuleManager.blink;

public class WatchdogPhase extends SubMode<Phase> {
    private final ButtonSetting autoBlink;
    private final ButtonSetting waitingBreakBlock;
    private final ButtonSetting autoDisable;
    private final SliderSetting autoDisableTicks;
    private final ButtonSetting exceptGround;

    private boolean phase;
    private int phaseTime;

    private boolean currentHittingBlock;
    private boolean lastHittingBlock;

    public WatchdogPhase(String name, Phase parent) {
        super(name, parent);
        this.registerSetting(autoBlink = new ButtonSetting("Blink", true));
        this.registerSetting(waitingBreakBlock = new ButtonSetting("waiting break block", false));
        this.registerSetting(autoDisable = new ButtonSetting("Auto disable", false));
        this.registerSetting(autoDisableTicks = new SliderSetting("Auto disable", 6, 1, 100, 1, "ticks"));
        this.registerSetting(exceptGround = new ButtonSetting("Except ground", false));
    }

    @Override
    public void onEnable() {
        phase = false;
        phaseTime = 0;
        currentHittingBlock = lastHittingBlock = false;
    }

    @Override
    public void onDisable() {
        if (autoBlink.isToggled())
            blink.disable();

        phaseTime = 0;
    }

    @Override
    public void onUpdate() {
        currentHittingBlock = ((PlayerControllerMPAccessor) mc.playerController).isHittingBlock();
        lastHittingBlock = currentHittingBlock;
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (this.phase) {
            this.phaseTime++;
        } else {
            this.phaseTime = 0;
        }

        if (autoDisable.isToggled() && phaseTime > autoDisableTicks.getInput()) {
            disable();
            return;
        }

        if (waitingBreakBlock.isToggled() && !(lastHittingBlock && !currentHittingBlock)) {
            return;
        }

        if (autoBlink.isToggled()) blink.enable();
        phase = true;
    }

    @SubscribeEvent
    public void onBlockAABB(BlockAABBEvent event) {
        if (this.phase) {
            if (exceptGround.isToggled() && event.getBlockPos().getY() < mc.thePlayer.posY)
                return;
            event.setBoundingBox(null);
        }
    }

    @SubscribeEvent
    public void onWorldChange(@NotNull WorldChangeEvent event) {
        this.phase = false;
        this.phaseTime = 0;
    }
}
