package keystrokesmod.module.impl.movement;

import keystrokesmod.Raven;
import keystrokesmod.event.PostMotionEvent;
import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.RotationEvent;
import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.utility.ContainerUtils;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.item.*;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0CPacketInput;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public class NoSlow extends Module {
    public static ModeSetting mode;
    public static SliderSetting slowed;
    public static ButtonSetting disableBow;
    public static ButtonSetting disableSword;
    public static ButtonSetting disablePotions;
    public static ButtonSetting swordOnly;
    public static ButtonSetting vanillaSword;
    private final String[] modes = new String[]{"Vanilla", "Pre", "Post", "Alpha", "Old Intave", "Intave", "Polar", "GrimAC", "HypixelTest A", "HypixelTest B", "Blink", "Beta"};
    private boolean postPlace;
    private static ModeOnly canChangeSpeed;

    private boolean lastUsingItem = false;

    private int offGroundTicks = 0;
    private boolean send = false;

    public NoSlow() {
        super("NoSlow", Module.category.movement, 0);
        this.registerSetting(mode = new ModeSetting("Mode", modes, 0));
        canChangeSpeed = new ModeOnly(mode, 5, 6, 7).reserve();
        this.registerSetting(slowed = new SliderSetting("Slow %", 5.0D, 0.0D, 80.0D, 1.0D, canChangeSpeed));
        this.registerSetting(disableSword = new ButtonSetting("Disable sword", false));
        this.registerSetting(disableBow = new ButtonSetting("Disable bow", false, canChangeSpeed));
        this.registerSetting(disablePotions = new ButtonSetting("Disable potions", false));
        this.registerSetting(swordOnly = new ButtonSetting("Sword only", false));
        this.registerSetting(vanillaSword = new ButtonSetting("Vanilla sword", false));
    }

    @Override
    public void onDisable() {
        lastUsingItem = false;
        offGroundTicks = 0;
        send = false;
    }

    public void onUpdate() {
        if (ModuleManager.bedAura.stopAutoblock) {
            return;
        }
        postPlace = false;
        if (vanillaSword.isToggled() && Utils.holdingSword()) {
            return;
        }
        boolean apply = getSlowed() != 0.2f;
        if (!apply || !mc.thePlayer.isUsingItem()) {
            return;
        }
        switch ((int) mode.getInput()) {
            case 1:
                if (mc.thePlayer.ticksExisted % 3 == 0 && !Raven.badPacketsHandler.C07) {
                    mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(SlotHandler.getHeldItem()));
                }
                break;
            case 2:
                postPlace = true;
                break;
            case 3:
                if (mc.thePlayer.ticksExisted % 3 == 0 && !Raven.badPacketsHandler.C07) {
                    mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), 1, null, 0, 0, 0));
                }
                break;
        }
    }

    @SubscribeEvent
    public void onPostMotion(PostMotionEvent e) {
        switch ((int) mode.getInput()) {
            case 8:
            case 3:
                if (postPlace) {
                    if (mc.thePlayer.ticksExisted % 3 == 0 && !Raven.badPacketsHandler.C07) {
                        mc.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(SlotHandler.getHeldItem()));
                    }
                    postPlace = false;
                }
                break;
        }

    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer.onGround) {
            offGroundTicks = 0;
        } else {
            offGroundTicks++;
        }

        if (!mc.thePlayer.isUsingItem()) {
            if (lastUsingItem && mode.getInput() == 10)
                ModuleManager.blink.disable();

            lastUsingItem = false;
            return;
        }

        final Item item = Objects.requireNonNull(SlotHandler.getHeldItem()).getItem();
        switch ((int) mode.getInput()) {
            case 4:
                PacketUtils.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                PacketUtils.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                break;
            case 5:
                if (!MoveUtil.isMoving()) return;
                if (ContainerUtils.isRest(item)) {
                    if (!lastUsingItem) {
                        PacketUtils.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.UP));
                    }
                } else {
                    if (item instanceof ItemSword) {
                        PacketUtils.sendPacket(new C08PacketPlayerBlockPlacement(SlotHandler.getHeldItem()));
                    }
                }
                break;
            case 6:
                if (ContainerUtils.isRest(item)) {
                    if (!lastUsingItem) {
                        PacketUtils.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.UP));
                    }
                    PacketUtils.sendPacket(new C0CPacketInput(0, 0.82f, false, false));
                } else {
                    if (item instanceof ItemSword) {
                        PacketUtils.sendPacket(new C08PacketPlayerBlockPlacement(SlotHandler.getHeldItem()));
                        PacketUtils.sendPacket(new C0CPacketInput(0, 0.82f, false, false));
                    }
                }
                break;
            case 7:
                PacketUtils.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
                PacketUtils.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 7 + 2));
                PacketUtils.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                break;
            case 8:
                if (mc.thePlayer.ticksExisted % 3 == 0 && !Raven.badPacketsHandler.C07) {
                    event.setPitch(90);
                    RotationHandler.setRotationPitch(90);
                }
                break;
            case 9:
                if (ContainerUtils.isRest(item)) {
                    if (mc.thePlayer.onGround) {
                        if (!lastUsingItem) {
                            mc.thePlayer.jump();
                        } else {
                            mc.thePlayer.motionY += 1E-14;
                        }
                    } else {
                        mc.thePlayer.motionY -= 0.0000001;
                    }
                }
                break;
            case 10:
                if (ContainerUtils.isRest(item)) {
                    if (!lastUsingItem) {
                        ModuleManager.blink.enable();
                    }
                }
                break;
            case 11:
                if (offGroundTicks == 2 && send) {
                    send = false;
                    PacketUtils.sendPacket(new C08PacketPlayerBlockPlacement(
                            new BlockPos(-1, -1, -1),
                            255,
                            SlotHandler.getHeldItem(),
                            0, 0, 0
                    ));
                } else if (mc.thePlayer.isUsingItem() && ContainerUtils.isRest(SlotHandler.getHeldItem().getItem())) {
                    event.setPitch(event.getPitch() - 1E-14f);
                }
                break;
        }

        lastUsingItem = true;
    }

    @SubscribeEvent
    public void onPacketSent(@NotNull SendPacketEvent event) {
        if (event.getPacket() instanceof C08PacketPlayerBlockPlacement && !mc.thePlayer.isUsingItem()) {
            C08PacketPlayerBlockPlacement blockPlacement = (C08PacketPlayerBlockPlacement) event.getPacket();
            if (SlotHandler.getHeldItem() != null && blockPlacement.getPlacedBlockDirection() == 255
                    && ContainerUtils.isRest(SlotHandler.getHeldItem().getItem()) && offGroundTicks < 2) {
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.setJumping(false);
                    mc.thePlayer.jump();
                }
                send = true;
            }
        }
    }

    public static float getSlowed() {
        if (!mc.thePlayer.isUsingItem()) return (100.0F - 0.0F) / 100.0F;
        if (SlotHandler.getHeldItem() == null || ModuleManager.noSlow == null || !ModuleManager.noSlow.isEnabled()) {
            return 0.2f;
        }
        if (swordOnly.isToggled() && !(SlotHandler.getHeldItem().getItem() instanceof ItemSword)) {
            return 0.2f;
        }
        if (SlotHandler.getHeldItem().getItem() instanceof ItemSword && disableSword.isToggled()) {
            return 0.2f;
        } if (SlotHandler.getHeldItem().getItem() instanceof ItemBow && (disableBow.isToggled() || Arrays.asList(5, 6).contains((int) mode.getInput()))) {
            return 0.2f;
        } else if (SlotHandler.getHeldItem().getItem() instanceof ItemPotion && !ItemPotion.isSplash(SlotHandler.getHeldItem().getItemDamage()) && disablePotions.isToggled()) {
            return 0.2f;
        }
        return !canChangeSpeed.get() ? 1.0f : (100.0F - (float) slowed.getInput()) / 100.0F;
    }

    @Override
    public String getInfo() {
        return modes[(int) mode.getInput()];
    }
}
