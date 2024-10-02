package keystrokesmod.module.impl.movement.longjump.hypixelfireball;

import keystrokesmod.event.*;
import keystrokesmod.module.impl.movement.longjump.HypixelFireballLongJump;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class SameYHypixelFireballLongJump extends SubMode<HypixelFireballLongJump> {
    private int ticks = 0;
    private boolean start;
    private boolean done;
    public static boolean stopModules;
    private boolean waitForDamage = false;
    private int aimedTicks = Integer.MAX_VALUE;
    private boolean setSpeed;

    public SameYHypixelFireballLongJump(String name, @NotNull HypixelFireballLongJump parent) {
        super(name, parent);
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S12PacketEntityVelocity) {
            if (((S12PacketEntityVelocity) event.getPacket()).getEntityID() == mc.thePlayer.getEntityId()) {
                if (waitForDamage) {
                    setSpeed = true;
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPreMotion(PreMotionEvent event) {
        if (!waitForDamage) {
            event.setYaw(mc.thePlayer.rotationYaw - 180);
            event.setPitch(89);
            if (aimedTicks == Integer.MAX_VALUE){
                aimedTicks = mc.thePlayer.ticksExisted;
            }
        }

        if (!waitForDamage && mc.thePlayer.ticksExisted - aimedTicks >= 2) {
            int shouldSlot = parent.getFireball();
            if (shouldSlot != mc.thePlayer.inventory.currentItem) {
                mc.thePlayer.inventory.currentItem = shouldSlot;
            } else {
                mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem());
                waitForDamage = true;
            }
        }

        if (mc.thePlayer.hurtTime >= 3) {
            start = true;
        }

        if (start) {
            ticks++;
        }

        if (setSpeed) {
            MoveUtil.strafe(parent.speed.getInput());
            setSpeed = false;
        }

        if (ticks > 0 && ticks < 30) {
            mc.thePlayer.motionY = 0.01;
        } else if (ticks >= 30) {
            done = true;
            start = false;
        }

        if (mc.thePlayer.hurtTime == 0 && done) {
            disable();
            if (parent.autoDisable.isToggled())
                parent.parent.disable();
        }
    }

    @SubscribeEvent
    public void onMoveInput(MoveInputEvent event) {
        if (((!start && !done) || ticks <= 0))
            event.setCanceled(true);
    }

    public void onDisable() {
        start = false;
        done = false;
        waitForDamage = false;
        aimedTicks = Integer.MAX_VALUE;
        ticks = 0;
        stopModules = false;
        setSpeed = false;
    }

    public void onEnable() {
        waitForDamage = true;
    }
}
