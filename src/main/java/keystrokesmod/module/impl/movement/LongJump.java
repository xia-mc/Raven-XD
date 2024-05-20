package keystrokesmod.module.impl.movement;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class LongJump extends Module {
    private SliderSetting mode;
    private SliderSetting horizontalSpeed;
    private SliderSetting verticalSpeed;
    private SliderSetting boostTicks;
    private ButtonSetting invertYaw;
    private int lastSlot = -1;
    private int ticks = -1;
    private boolean setSpeed;
    public static boolean stopModules;
    private boolean placed;
    private int waitTicks;
    private String[] modes = new String[]{"Fireball", "Fireball Auto"};
    public LongJump() {
        super("Long Jump", category.movement);
        this.registerSetting(mode = new SliderSetting("Mode", modes, 0));
        this.registerSetting(horizontalSpeed = new SliderSetting("Horizontal speed", 0, 0.0, 8.0, 0.1));
        this.registerSetting(verticalSpeed = new SliderSetting("Vertical speed", 0, 0.0, 8.0, 0.1));
        this.registerSetting(boostTicks = new SliderSetting("Boost ticks", 2, 1, 20, 1));
        this.registerSetting(invertYaw = new ButtonSetting("Invert yaw", true));
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent e) {
        if (e.getPacket() instanceof S12PacketEntityVelocity && Utils.nullCheck()) {
            if (((S12PacketEntityVelocity) e.getPacket()).getEntityID() == mc.thePlayer.getEntityId() && placed && ticks < 0) {
                ticks = 0;
            }
        }
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent e) {
        if (mode.getInput() == 0 && e.getPacket() instanceof C08PacketPlayerBlockPlacement && holdingFireball()) {
            ticks = 0;
            setSpeed = true;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPreMotion(PreMotionEvent e) {
        if (!Utils.nullCheck()) {
            return;
        }
        if (mode.getInput() == 1) {
            if (!placed) {
                if (mc.thePlayer.onGround) {
                    waitTicks++;
                }
                int fireballSlot = getFireball();
                if (fireballSlot != -1 && fireballSlot != mc.thePlayer.inventory.currentItem) {
                    lastSlot = mc.thePlayer.inventory.currentItem;
                    mc.thePlayer.inventory.currentItem = fireballSlot;
                }
            }
            if (!placed && mc.thePlayer.onGround && waitTicks <= 3) {
                if (invertYaw.isToggled()) {
                    e.setYaw(mc.thePlayer.rotationYaw - 180);
                    e.setPitch(60);
                } else {
                    e.setPitch(90);
                }
            }
            if (waitTicks == 2 && !placed) {
                mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                placed = true;
            }
            if (ticks < 0) {
                return;
            }
            if (ticks >= boostTicks.getInput()) {
                this.disable();
                return;
            }
            setSpeed();
            ticks++;
        }
        else {
            if (setSpeed) {
                if (ticks > boostTicks.getInput()) {
                    setSpeed = false;
                    ticks = 0;
                    return;
                }
                ticks++;
                setSpeed();
            }
        }
    }

    private boolean holdingFireball() {
        return mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() == Items.fire_charge;
    }

    public void onDisable() {
        if (lastSlot != -1 && mode.getInput() == 1) {
            mc.thePlayer.inventory.currentItem = lastSlot;
        }
        ticks = lastSlot = -1;
        setSpeed = stopModules = placed = false;
        waitTicks = 0;
    }

    public void onEnable() {
        if (getFireball() == -1 && mode.getInput() == 1) {
            Utils.sendMessage("§cNo fireball found.");
            this.disable();
            return;
        }
        if (horizontalSpeed.getInput() == 0 && verticalSpeed.getInput() == 0) {
            Utils.sendMessage("&cLong jump values are set to 0.");
            this.disable();
            return;
        }
        if (mode.getInput() == 1) {
            stopModules = true;
        }
    }

    private void setSpeed() {
        if (verticalSpeed.getInput() != 0.0) {
            mc.thePlayer.motionY = verticalSpeed.getInput() / 2.0 - Math.random() / 20.0;
        }
        if (horizontalSpeed.getInput() != 0.0) {
            Utils.setSpeed(horizontalSpeed.getInput());
        }
    }

    private int getFireball() {
        int n = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack getStackInSlot = mc.thePlayer.inventory.getStackInSlot(i);
            if (getStackInSlot != null && getStackInSlot.getItem() == Items.fire_charge) {
                n = i;
                break;
            }
        }
        return n;
    }
}
