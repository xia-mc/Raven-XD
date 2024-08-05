package keystrokesmod.module.impl.movement.noweb;

import keystrokesmod.event.BlockWebEvent;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.module.impl.movement.NoWeb;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class IntaveNoWeb extends SubMode<NoWeb> {
    private final ButtonSetting noDown;
    private final ButtonSetting upAndDown;

    private BlockPos lastWeb = null;
    private boolean webbing = false;

    public IntaveNoWeb(String name, @NotNull NoWeb parent) {
        super(name, parent);
        this.registerSetting(noDown = new ButtonSetting("No down", false));
        this.registerSetting(upAndDown = new ButtonSetting("UpAndDown", false, noDown::isToggled));
    }

    @SubscribeEvent
    public void onWeb(@NotNull BlockWebEvent event) {
        lastWeb = event.getBlockPos();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPreUpdate(PreUpdateEvent event) {
        if (lastWeb == null || !Utils.nullCheck() || BlockUtils.getBlock(lastWeb) != Blocks.web) {
            if (webbing)
                Utils.resetTimer();
            webbing = false;
            lastWeb = null;
            return;
        }

        AxisAlignedBB box = new AxisAlignedBB(lastWeb, lastWeb.add(1, 1, 1));
        if (box.intersectsWith(mc.thePlayer.getEntityBoundingBox())) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.motionY = MoveUtil.jumpMotion();
                MoveUtil.moveFlying(0.3);
            } else if (noDown.isToggled()) {
                if (upAndDown.isToggled())
                    if (mc.gameSettings.keyBindSneak.isKeyDown())
                        mc.thePlayer.motionY = -0.2;
                    else if (mc.gameSettings.keyBindJump.isKeyDown())
                        mc.thePlayer.motionY = mc.thePlayer.ticksExisted % 2 == 0 ? 0.2 : -0.01;
                    else
                        mc.thePlayer.motionY = -0.01;
                else
                    mc.thePlayer.motionY = -0.01;
            }

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

    @Override
    public void onDisable() {
        if (webbing)
            Utils.resetTimer();
        webbing = false;
        lastWeb = null;
    }
}
