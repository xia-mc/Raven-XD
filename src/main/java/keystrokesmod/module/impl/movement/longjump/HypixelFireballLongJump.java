package keystrokesmod.module.impl.movement.longjump;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.impl.client.Notifications;
import keystrokesmod.module.impl.movement.LongJump;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.module.setting.utils.ModeOnly;
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

public class HypixelFireballLongJump extends SubMode<LongJump> {
    private final SliderSetting speed;
    private final ButtonSetting autoDisable;
    private final ModeSetting longer;
    private final ButtonSetting fakeGround;
    private final SliderSetting longerTick;

    private int lastSlot = -1;
    private int ticks = -1;
    private int offGroundTicks = 0;
    private boolean setSpeed;
    private boolean sentPlace;
    private int initTicks;
    private boolean thrown;

    public HypixelFireballLongJump(String name, @NotNull LongJump parent) {
        super(name, parent);
        this.registerSetting(speed = new SliderSetting("Speed", 1.5, 1, 2, 0.01));
        this.registerSetting(autoDisable = new ButtonSetting("Auto disable", true));
        this.registerSetting(longer = new ModeSetting("Longer", new String[]{"Disable", "Stop motion", "Air jump"}, 1));
        this.registerSetting(fakeGround = new ButtonSetting("Fake ground", false, new ModeOnly(longer, 1, 2)));
        this.registerSetting(longerTick = new SliderSetting("Longer tick", 20, 10, 30, 1, new ModeOnly(longer, 1, 2)));
    }

    @SubscribeEvent
    public void onSendPacket(@NotNull SendPacketEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof C08PacketPlayerBlockPlacement
                && ((C08PacketPlayerBlockPlacement) event.getPacket()).getStack() != null
                && ((C08PacketPlayerBlockPlacement) event.getPacket()).getStack().getItem() instanceof ItemFireball) {
            thrown = true;
            if (mc.thePlayer.onGround && !Utils.jumpDown()) {
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

        if (mc.thePlayer.onGround)
            offGroundTicks = 0;
        else
            offGroundTicks++;

        switch (initTicks) {
            case 0:
                int fireballSlot = getFireball();
                if (fireballSlot != -1 && fireballSlot != SlotHandler.getCurrentSlot()) {
                    lastSlot = SlotHandler.getCurrentSlot();
                    SlotHandler.setCurrentSlot(fireballSlot);
                }
            case 1:
                event.setYaw(mc.thePlayer.rotationYaw - 180);
                event.setPitch(89);
                break;
            case 2:
                if (!sentPlace) {
                    PacketUtils.sendPacket(new C08PacketPlayerBlockPlacement(SlotHandler.getHeldItem()));
                    sentPlace = true;
                }
                break;
            case 3:
                if (lastSlot != -1) {
                    SlotHandler.setCurrentSlot(lastSlot);
                    lastSlot = -1;
                }
                break;
        }

        if (longer.getInput() != 0) {
            if (offGroundTicks == (int) longerTick.getInput()) {
                if (fakeGround.isToggled())
                    event.setOnGround(true);
                if (longer.getInput() == 1)
                    mc.thePlayer.motionY = 0;
                else if (longer.getInput() == 2)
                    mc.thePlayer.jump();
                if (autoDisable.isToggled())
                    parent.disable();
            }
        } else if (ticks > 1) {
            if (autoDisable.isToggled())
                parent.disable();
        }

        if (setSpeed) {
            this.setSpeed();
            ticks++;
        }
        if (initTicks <= 3) {
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
            SlotHandler.setCurrentSlot(lastSlot);
        }
        ticks = lastSlot = -1;
        setSpeed = sentPlace = false;
        initTicks = 0;
    }

    public void onEnable() {
        if (getFireball() == -1) {
            Notifications.sendNotification(Notifications.NotificationTypes.INFO, "Could not find Fireball");
            parent.disable();
            return;
        }
        initTicks = 0;
        offGroundTicks = 0;
    }

    private void setSpeed() {
        MoveUtil.strafe((float) speed.getInput());
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
