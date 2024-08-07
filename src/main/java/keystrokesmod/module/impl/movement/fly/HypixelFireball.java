package keystrokesmod.module.impl.movement.fly;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.impl.client.Notifications;
import keystrokesmod.module.impl.movement.Fly;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemFireball;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class HypixelFireball extends SubMode<Fly> {
    private int lastSlot = -1;
    private int ticks = -1;
    private boolean setSpeed;
    private boolean sentPlace;
    private int initTicks;
    private boolean thrown;

    public HypixelFireball(String name, @NotNull Fly parent) {
        super(name, parent);
    }

    @SubscribeEvent
    public void onSendPacket(@NotNull SendPacketEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof C08PacketPlayerBlockPlacement
                && ((C08PacketPlayerBlockPlacement) event.getPacket()).getStack() != null
                && ((C08PacketPlayerBlockPlacement) event.getPacket()).getStack().getItem() instanceof ItemFireball) {
            thrown = true;
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump();
            }
        }
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent event) {
        if (!Utils.nullCheck()) {
            return;
        }

        Packet<?> packet = event.getPacket();
        if (packet instanceof S12PacketEntityVelocity) {
            if (((S12PacketEntityVelocity) event.getPacket()).getEntityID() != mc.thePlayer.getEntityId()) {
                return;
            }
            if (thrown) {
                ticks = 0;
                setSpeed = true;
                thrown = false;
            }
        }
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (!Utils.nullCheck()) {
            return;
        }
        if (initTicks == 0) {
            event.setYaw(mc.thePlayer.rotationYaw - 180);
            event.setPitch(89);
            int fireballSlot = getFireball();
            if (fireballSlot != -1 && fireballSlot != mc.thePlayer.inventory.currentItem) {
                lastSlot = mc.thePlayer.inventory.currentItem;
                mc.thePlayer.inventory.currentItem = fireballSlot;
            }
        }
        if (initTicks == 1) {
            if (!sentPlace) {
                PacketUtils.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                sentPlace = true;
            }
        } else if (initTicks == 2) {
            if (lastSlot != -1) {
                mc.thePlayer.inventory.currentItem = lastSlot;
                lastSlot = -1;
            }
        }
        if (ticks > 1) {
            this.toggle();
            return;
        }
        if (setSpeed) {
            this.setSpeed();
            ticks++;
        }
        if (initTicks < 3) {
            initTicks++;
        }

        if (setSpeed) {
            if (ticks > 1) {
                setSpeed = false;
                ticks = 0;
                return;
            }
            ticks++;
            setSpeed();
        }
    }

    public void onDisable() {
        if (lastSlot != -1) {
            mc.thePlayer.inventory.currentItem = lastSlot;
        }
        ticks = lastSlot = -1;
        setSpeed = sentPlace = false;
        initTicks = 0;
    }

    public void onEnable() {
        if (getFireball() == -1) {
            Notifications.sendNotification(Notifications.NotificationTypes.INFO, "Could not find Fireball");
            this.toggle();
            return;
        }
        initTicks = 0;
    }

    private void setSpeed() {
        MoveUtil.strafe(1.5f);
    }

    private int getFireball() {
        int a = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack getStackInSlot = mc.thePlayer.inventory.getStackInSlot(i);
            if (getStackInSlot != null && getStackInSlot.getItem() == Items.fire_charge) {
                a = i;
                break;
            }
        }
        return a;
    }
}
