package keystrokesmod.module.impl.movement.longjump;

import keystrokesmod.event.MoveInputEvent;
import keystrokesmod.event.PrePlayerInputEvent;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.event.RotationEvent;
import keystrokesmod.module.impl.client.Notifications;
import keystrokesmod.module.impl.movement.LongJump;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class HypixelBowLongJump extends SubMode<LongJump> {
    private final SliderSetting speed;
    private final ButtonSetting autoDisable;

    private State state = State.SELF_DAMAGE;
    private double cacheMotionX;
    private double cacheMotionY;
    private double cacheMotionZ;
    private final Queue<S32PacketConfirmTransaction> delayedPackets = new ConcurrentLinkedQueue<>();

    public HypixelBowLongJump(String name, @NotNull LongJump parent) {
        super(name, parent);
        this.registerSetting(speed = new SliderSetting("Speed", 1, 0.5, 1.5, 0.1));
        this.registerSetting(autoDisable = new ButtonSetting("Auto disable", true));
    }

    @Override
    public void onEnable() throws Throwable {
        MoveUtil.stop();
        state = State.SELF_DAMAGE;
    }

    @SubscribeEvent
    public void onMoveInput(MoveInputEvent event) {
        if (state != State.NONE) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S12PacketEntityVelocity && state == State.SELF_DAMAGE) {
            S12PacketEntityVelocity packet = (S12PacketEntityVelocity) event.getPacket();
            if (packet.getEntityID() != mc.thePlayer.getEntityId()) return;

            cacheMotionX = packet.getMotionX() / 8000.0;
            cacheMotionY = packet.getMotionY() / 8000.0;
            cacheMotionZ = packet.getMotionZ() / 8000.0;
            event.setCanceled(true);
            state = State.JUMP;
        } else if (event.getPacket() instanceof S32PacketConfirmTransaction && state == State.JUMP) {
            delayedPackets.add((S32PacketConfirmTransaction) event.getPacket());
        }
    }

    @SubscribeEvent
    public void onRotation(RotationEvent event) {
        if (state == State.SELF_DAMAGE)
            event.setPitch(-90);
    }

    @SubscribeEvent
    public void onPrePlayerInput(PrePlayerInputEvent event) {
        switch (state) {
            case SELF_DAMAGE:
                int slot = getBow();
                if (slot == -1) {
                    Notifications.sendNotification(Notifications.NotificationTypes.INFO, "Could not find Bow");
                    parent.disable();
                }
                SlotHandler.setCurrentSlot(slot);
                break;
            case JUMP:
                if (!Utils.jumpDown() && mc.thePlayer.onGround) {
                    MoveUtil.strafe(MoveUtil.getAllowedHorizontalDistance());
                    mc.thePlayer.jump();
                    state = State.APPLY;
                }
                break;
            case APPLY:
                if (parent.offGroundTicks >= 7) {
                    mc.thePlayer.motionX = cacheMotionX;
                    mc.thePlayer.motionY = cacheMotionY;
                    mc.thePlayer.motionZ = cacheMotionZ;
                    synchronized (delayedPackets) {
                        for (S32PacketConfirmTransaction p : delayedPackets) {
                            PacketUtils.receivePacketNoEvent(p);
                        }
                        delayedPackets.clear();
                    }
                    state = State.BOOST;
                }
                break;
            case BOOST:
                MoveUtil.strafe(speed.getInput());
                state = State.NONE;
                break;
            case NONE:
                if (autoDisable.isToggled())
                    parent.disable();
        }
    }

    private int getBow() {
        int a = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack getStackInSlot = mc.thePlayer.inventory.getStackInSlot(i);
            if (getStackInSlot != null && getStackInSlot.getItem() instanceof ItemBow) {
                a = i;
                break;
            }
        }
        return a;
    }

    enum State {
        SELF_DAMAGE,
        JUMP,
        APPLY,
        BOOST,
        NONE
    }
}
