package keystrokesmod.module.impl.movement.longjump.hypixelfireball;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.impl.movement.longjump.HypixelFireballLongJump;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Reflection;
import keystrokesmod.utility.Utils;
import net.minecraft.item.ItemFireball;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class MotionHypixelFireballLongJump extends SubMode<HypixelFireballLongJump> {
    private final SliderSetting motion;
    private final SliderSetting time;
    private final ButtonSetting strafe;

    private int lastSlot = -1;
    private int ticks = -1;
    private boolean setSpeed;
    private boolean sentPlace;
    private int initTicks;
    private boolean thrown;

    public MotionHypixelFireballLongJump(String name, @NotNull HypixelFireballLongJump parent) {
        super(name, parent);
        this.registerSetting(motion = new SliderSetting("Motion", 0.3, 0.01, 0.4, 0.01));
        this.registerSetting(time = new SliderSetting("Time", 31, 10, 60, 1, "ticks"));
        this.registerSetting(strafe = new ButtonSetting("Strafe", true));
    }

    @SubscribeEvent
    public void onSendPacket(@NotNull SendPacketEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof C08PacketPlayerBlockPlacement
                && ((C08PacketPlayerBlockPlacement) event.getPacket()).getStack() != null
                && ((C08PacketPlayerBlockPlacement) event.getPacket()).getStack().getItem() instanceof ItemFireball) {
            thrown = true;
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

        switch (initTicks) {
            case 0:
                int fireballSlot = parent.getFireball();
                if (fireballSlot != -1 && fireballSlot != SlotHandler.getCurrentSlot()) {
                    lastSlot = SlotHandler.getCurrentSlot();
                    SlotHandler.setCurrentSlot(fireballSlot);
                }
                event.setYaw(mc.thePlayer.rotationYaw - 180);
                event.setPitch(89);
                break;
            case 1:
                event.setYaw(mc.thePlayer.rotationYaw - 180);
                event.setPitch(89);
                if (!sentPlace) {
                    PacketUtils.sendPacket(new C08PacketPlayerBlockPlacement(SlotHandler.getHeldItem()));
                    sentPlace = true;
                }
                break;
            case 2:
                if (lastSlot != -1) {
                    SlotHandler.setCurrentSlot(lastSlot);
                    lastSlot = -1;
                }
                break;
        }

        initTicks++;

        if (ticks > time.getInput()) {
            if (parent.autoDisable.isToggled())
                parent.parent.disable();
        } else if (ticks >= 0) {
            mc.thePlayer.motionY = motion.getInput();
            if (strafe.isToggled())
                MoveUtil.strafe();
        }

        if (setSpeed) {
            this.setSpeed();
            ticks++;
        }

        if (setSpeed) {
            if (ticks > 1) {
                setSpeed = false;
                return;
            }
            ticks++;
            setSpeed();
        }
    }

    public void onDisable() {
        ticks = lastSlot = -1;
        setSpeed = sentPlace = false;
        initTicks = 0;
    }

    public void onEnable() {
        initTicks = 0;
    }

    private void setSpeed() {
        MoveUtil.strafe((float) parent.speed.getInput());
    }

}
