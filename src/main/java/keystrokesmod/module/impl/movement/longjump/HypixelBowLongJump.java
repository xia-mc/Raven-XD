package keystrokesmod.module.impl.movement.longjump;

import keystrokesmod.Raven;
import keystrokesmod.event.*;
import keystrokesmod.module.impl.client.Notifications;
import keystrokesmod.module.impl.movement.LongJump;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class HypixelBowLongJump extends SubMode<LongJump> {
    private final ButtonSetting autoDisable;

    private State state = State.SELF_DAMAGE;
    private int tick = 0;

    public HypixelBowLongJump(String name, @NotNull LongJump parent) {
        super(name, parent);
        this.registerSetting(autoDisable = new ButtonSetting("Auto disable", true));
    }

    @Override
    public void onEnable() throws Throwable {
        MoveUtil.stop();
        state = State.SELF_DAMAGE;
        tick = 0;
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S12PacketEntityVelocity && (state == State.SELF_DAMAGE_POST)) {
            S12PacketEntityVelocity packet = (S12PacketEntityVelocity) event.getPacket();
            if (packet.getEntityID() != mc.thePlayer.getEntityId()) return;

            state = State.JUMP;
        }
    }

    @SubscribeEvent
    public void onRotation(RotationEvent event) {
        if (state == State.SELF_DAMAGE || state == State.SELF_DAMAGE_POST)
            event.setPitch(-90);
    }

    @SubscribeEvent
    public void onPrePlayerInput(PrePlayerInputEvent event) {
        int slot = getBow();
        if (slot == -1) {
            Notifications.sendNotification(Notifications.NotificationTypes.INFO, "Could not find Bow");
            parent.disable();
        }
        switch (state) {
            case SELF_DAMAGE:
                if (SlotHandler.getCurrentSlot() == slot) {
                    PacketUtils.sendPacketNoEvent(new C08PacketPlayerBlockPlacement(SlotHandler.getHeldItem()));
                    Raven.getExecutor().schedule(() -> PacketUtils.sendPacketNoEvent(new C07PacketPlayerDigging(
                            C07PacketPlayerDigging.Action.RELEASE_USE_ITEM,
                            BlockPos.ORIGIN, EnumFacing.UP
                    )), 200, TimeUnit.MILLISECONDS);
                    state = State.SELF_DAMAGE_POST;
                }
                SlotHandler.setCurrentSlot(slot);
                event.setCanceled(true);
                MoveUtil.stop();
                break;
            case SELF_DAMAGE_POST:
                SlotHandler.setCurrentSlot(slot);
                event.setCanceled(true);
                MoveUtil.stop();
                break;
            case JUMP:
                tick++;
                if (!Utils.jumpDown() && mc.thePlayer.onGround) {
                    MoveUtil.strafe(MoveUtil.getAllowedHorizontalDistance() *
                            (mc.thePlayer.isPotionActive(Potion.moveSpeed) ? 0.75 : 0.7) - Math.random() / 10000f);
                    mc.thePlayer.jump();
                }

                if (tick == 8) {
                    state = State.APPLY;
                }
                break;
            case APPLY:
                tick++;
                MoveUtil.strafe((mc.thePlayer.isPotionActive(Potion.moveSpeed) ? 0.8 : 0.7) - Math.random() / 10000f);
                state = State.BOOST;
                break;
            case BOOST:
                tick++;
                if (tick > 50) {
                    state = State.NONE;
                    break;
                }

                mc.thePlayer.motionY += 0.028;

                if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                    mc.thePlayer.motionX *= 1.038;
                    mc.thePlayer.motionZ *= 1.038;
                } else {
                    if (tick == 12 || tick == 13) {
                        mc.thePlayer.motionX *= 1.1;
                        mc.thePlayer.motionZ *= 1.1;
                    }
                    mc.thePlayer.motionX *= 1.019;
                    mc.thePlayer.motionZ *= 1.019;
                }
                break;
            case NONE:
                if (autoDisable.isToggled())
                    parent.disable();
        }

        if (tick < 19) {
            MoveUtil.strafe();
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
        SELF_DAMAGE_POST,
        JUMP,
        APPLY,
        BOOST,
        NONE
    }
}
