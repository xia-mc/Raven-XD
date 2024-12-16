package keystrokesmod.module.impl.movement.step;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import keystrokesmod.event.PreTickEvent;
import keystrokesmod.event.StepEvent;
import keystrokesmod.module.impl.movement.Step;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class HypixelStep extends SubMode<Step> {
    public static final DoubleList MOTION = DoubleList.of(.42, .75, 1);

    private final SliderSetting delay;
    private final SliderSetting timer;

    private long lastStep = -1;
    private boolean stepped = false;

    public HypixelStep(String name, @NotNull Step parent) {
        super(name, parent);
        this.registerSetting(delay = new SliderSetting("Delay", 1000, 0, 5000, 250, "ms"));
        this.registerSetting(timer = new SliderSetting("Timer", 0.25, 0.25, 1, 0.01));
    }

    @Override
    public void onDisable() throws Throwable {
        mc.thePlayer.stepHeight = 0.6f;
        Utils.resetTimer();
    }

    @SubscribeEvent
    public void onStep(@NotNull StepEvent event) {
        if (event.getHeight() == 1 && mc.thePlayer.onGround && !Utils.inLiquid()) {
            Block block = BlockUtils.getBlock(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            if (block instanceof BlockStairs || block instanceof BlockSlab) return;

            Utils.getTimer().timerSpeed = (float) timer.getInput();
            stepped = true;
            for (double motion : MOTION) {
                MoveUtil.strafe(MoveUtil.getBaseMoveSpeed());
                PacketUtils.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(
                        mc.thePlayer.posX,
                        mc.thePlayer.posY + motion,
                        mc.thePlayer.posZ,
                        false
                ));
            }
            mc.thePlayer.stepHeight = 0.6f;
            lastStep = System.currentTimeMillis();
        }
    }

    @SubscribeEvent
    public void onPreTick(PreTickEvent event) {
        if (stepped) {
            Utils.resetTimer();
            stepped = false;
        }
        if (System.currentTimeMillis() - lastStep > delay.getInput())
            mc.thePlayer.stepHeight = 1;
    }
}
