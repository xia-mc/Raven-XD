package keystrokesmod.module.impl.movement.noweb;

import keystrokesmod.event.BlockWebEvent;
import keystrokesmod.event.PrePlayerInputEvent;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.event.RotationEvent;
import keystrokesmod.module.impl.movement.NoWeb;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class IntaveNoWeb extends SubMode<NoWeb> {
    private BlockPos lastWeb = null;
    private boolean webbing = false;

    public IntaveNoWeb(String name, @NotNull NoWeb parent) {
        super(name, parent);
    }

    @SubscribeEvent
    public void onWeb(@NotNull BlockWebEvent event) {
        lastWeb = event.getBlockPos();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPreUpdate(PreUpdateEvent event) {
        if (lastWeb == null || !Utils.nullCheck()) {
            if (webbing)
                Utils.resetTimer();
            webbing = false;
        }

        AxisAlignedBB box = BlockUtils.getCollisionBoundingBox(lastWeb);
        if (box != null && box.intersectsWith(mc.thePlayer.getEntityBoundingBox())) {
            if (mc.thePlayer.onGround)
                mc.thePlayer.jump();
            Utils.getTimer().timerSpeed = 1.004f;
            webbing = true;
        } else {
            if (webbing) {
                Utils.resetTimer();
                webbing = false;
            }
            lastWeb = null;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onRotation(RotationEvent event) {
        if (!mc.thePlayer.onGround && webbing) {
            event.setYaw(mc.thePlayer.rotationYaw - 45);
            event.setMoveFix(RotationHandler.MoveFix.SILENT);
        }
    }

    @SubscribeEvent
    public void onPrePlayerInput(PrePlayerInputEvent event) {
        if (webbing) {
            event.setStrafe(0);
            event.setForward(event.getForward() != 0 ? 1 : 0);
        }
    }

    @Override
    public void onDisable() {
        if (webbing)
            Utils.resetTimer();
        webbing = false;
        lastWeb = null;
    }
}
