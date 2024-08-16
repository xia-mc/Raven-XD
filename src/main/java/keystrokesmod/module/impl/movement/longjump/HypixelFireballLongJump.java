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
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class HypixelFireballLongJump extends SubMode<LongJump> {
    private final SliderSetting speed;
    private final ModeSetting verticalMode;
    private final SliderSetting verticalSpeed;
    private final SliderSetting verticalMotion;
    private final ButtonSetting longer;
    private final ButtonSetting fakeGround;
    private final SliderSetting longerTick;
    private final ButtonSetting autoDisable;

    private int lastSlot = -1;
    private int ticks = -1;
    private int sameY$ticks = -1;
    private int offGroundTicks = 0;
    private boolean setSpeed;
    private boolean sentPlace;
    private int initTicks;
    private boolean thrown;

    public HypixelFireballLongJump(String name, @NotNull LongJump parent) {
        super(name, parent);
        this.registerSetting(speed = new SliderSetting("Speed", 1.5, 0.1, 2, 0.01));
        this.registerSetting(verticalMode = new ModeSetting("Vertical mode", new String[]{"None", "Normal", "SameY"}, 0));
        this.registerSetting(verticalSpeed = new SliderSetting("Vertical speed", 0.5, 0.1, 2, 0.01, new ModeOnly(verticalMode, 1)));
        this.registerSetting(verticalMotion = new SliderSetting("Vertical motion", 0.01, 0.01, 0.6, 0.01, new ModeOnly(verticalMode, 2)));
        this.registerSetting(longer = new ButtonSetting("Longer", true, new ModeOnly(verticalMode, 0, 1)));
        Supplier<Boolean> longerOnly = new ModeOnly(verticalMode, 0, 1).extend(longer::isToggled);
        this.registerSetting(fakeGround = new ButtonSetting("Fake ground", false, longerOnly));
        this.registerSetting(longerTick = new SliderSetting("Longer tick", 20, 10, 30, 1, longerOnly));
        this.registerSetting(autoDisable = new ButtonSetting("Auto disable", true));
    }

    @SubscribeEvent
    public void onSendPacket(@NotNull SendPacketEvent event) {
        Packet<?> packet = event.getPacket();
        if (packet instanceof C08PacketPlayerBlockPlacement
                && ((C08PacketPlayerBlockPlacement) event.getPacket()).getStack() != null
                && ((C08PacketPlayerBlockPlacement) event.getPacket()).getStack().getItem() instanceof ItemFireball) {
            thrown = true;
            if (mc.thePlayer.onGround && !Utils.jumpDown() && verticalMode.getInput() != 2) {
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
                sameY$ticks = 0;
                setSpeed = true;
                thrown = false;

                if (verticalMode.getInput() == 2)
                    event.setCanceled(true);
            }
        } else if (packet instanceof S27PacketExplosion) {
            if (verticalMode.getInput() == 2)
                event.setCanceled(true);
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

        if (initTicks <= 3) {
            initTicks++;
        }

        switch ((int) verticalMode.getInput()) {
            case 0:
            case 1:
                if (longer.isToggled()) {
                    if (offGroundTicks == (int) longerTick.getInput()) {
                        if (fakeGround.isToggled())
                            event.setOnGround(true);
                        mc.thePlayer.motionY = Math.max(mc.thePlayer.motionY, 0);
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

                if (setSpeed) {
                    if (ticks > 1) {
                        setSpeed = false;
                        ticks = 0;
                        return;
                    }
                    ticks++;
                    setSpeed();
                }
                break;
            case 2:
                if (sameY$ticks == -1) break;

                if (sameY$ticks == 0)
                    MoveUtil.strafe(speed.getInput());
                mc.thePlayer.motionY = verticalMotion.getInput();

                sameY$ticks++;
                if (sameY$ticks >= 31) {
                    sameY$ticks = -1;
                    if (autoDisable.isToggled())
                        parent.disable();
                }
                break;
        }
    }

    public void onDisable() {
        if (lastSlot != -1) {
            SlotHandler.setCurrentSlot(lastSlot);
        }
        ticks = sameY$ticks = lastSlot = -1;
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
        if (verticalMode.getInput() == 1)
            mc.thePlayer.motionY = verticalSpeed.getInput();
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
