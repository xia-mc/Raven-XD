package keystrokesmod.module.impl.movement.step;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.StepEvent;
import keystrokesmod.module.impl.movement.Step;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class MatrixStep extends SubMode<Step> {
    private final ButtonSetting twoBlock;
    private final ButtonSetting instant;

    private int ticks;
    private boolean doJump;
    private int timedTicksLeft = -1;

    public MatrixStep(String name, @NotNull Step parent) {
        super(name, parent);
        this.registerSetting(twoBlock = new ButtonSetting("Two block", true));
        this.registerSetting(instant = new ButtonSetting("Instant", false, twoBlock::isToggled));
    }

    @Override
    public void onDisable() throws Exception {
        mc.thePlayer.stepHeight = 0.6F;
        timedTicksLeft = -1;
        Utils.resetTimer();
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        mc.thePlayer.stepHeight = twoBlock.isToggled() ? 2 : 1;
        if (doJump) {
            if ((ticks > 0 && mc.thePlayer.onGround) || ticks > 5) {
                ticks = 0;
                doJump = false;
                return;
            }
            if (ticks % 3 == 0) {
                event.setOnGround(true);
                mc.thePlayer.jump();
            }
            ticks++;
        }
    }

    @Override
    public void onUpdate() throws Exception {
        if (timedTicksLeft >= 0)
            timedTicksLeft--;
        if (timedTicksLeft == 0) {
            Utils.resetTimer();
        }
    }

    @SubscribeEvent
    public void onStep(@NotNull StepEvent event) {
        if (!MoveUtil.isMoving()) return;

        if (event.getHeight() > 1.0F) {
            Utils.getTimer().timerSpeed = 1.0F / 7F;
            if (instant.isToggled()) {
                PacketUtils.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.41999998688698, mc.thePlayer.posZ, false));
                PacketUtils.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.7531999805212, mc.thePlayer.posZ, false));
                PacketUtils.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.00133597911215, mc.thePlayer.posZ, true));
                PacketUtils.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.42133596599913, mc.thePlayer.posZ, false));
                PacketUtils.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1.75453595963335, mc.thePlayer.posZ, false));
                PacketUtils.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 2.0026719582243, mc.thePlayer.posZ, false));
            } else {
                doJump = true;
                ticks = 0;
                mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            }
        } else if (event.getHeight() > 0.6F) {
            Utils.getTimer().timerSpeed = 0.33333f;
            PacketUtils.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.42F, mc.thePlayer.posZ, false));
            PacketUtils.sendPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY + 0.42F, mc.thePlayer.posZ, true));
        }
        timedTicksLeft = 2;
    }
}
