package keystrokesmod.module.impl.movement.noslow.customnoslow;

import keystrokesmod.event.*;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.movement.noslow.CustomNoSlow;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0CPacketInput;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class SimpleCustomNoSlow extends SubMode<CustomNoSlow> {
    private final ButtonSetting slowDown;
    private final SliderSetting slowDownForward;
    private final SliderSetting slowDownStrafe;
    private final ButtonSetting switch1;
    private final ButtonSetting slot;
    private final ButtonSetting onGround;
    private final ButtonSetting toggle;
    private final ButtonSetting input;
    private final ButtonSetting inputForwardLegit;
    private final SliderSetting inputForward;
    private final ButtonSetting inputStrafeLegit;
    private final SliderSetting inputStrafe;
    private final ButtonSetting inputJumpLegit;
    private final ButtonSetting inputJump;
    private final ButtonSetting inputSneakLegit;
    private final ButtonSetting inputSneak;
    private final ButtonSetting bug;
    private final ButtonSetting timer;
    private final SliderSetting timerValue;
    private final ButtonSetting blink;

    public SimpleCustomNoSlow(String name, @NotNull CustomNoSlow parent) {
        super(name, parent);
        this.registerSetting(slowDown = new ButtonSetting("Slowdown", false));
        this.registerSetting(slowDownForward = new SliderSetting("SlowdownForward", 1, 0.2, 1, 0.01));
        this.registerSetting(slowDownStrafe = new SliderSetting("SlowdownStrafe", 1, 0.2, 1, 0.01));
        this.registerSetting(switch1 = new ButtonSetting("Switch", false));
        this.registerSetting(slot = new ButtonSetting("Slot", false));
        this.registerSetting(onGround = new ButtonSetting("OnGround", false));
        this.registerSetting(toggle = new ButtonSetting("Toggle", false));
        this.registerSetting(input = new ButtonSetting("Input", false));
        this.registerSetting(inputForwardLegit = new ButtonSetting("InputForwardLegit", false));
        this.registerSetting(inputForward = new SliderSetting("InputForward", 1, -1, 1, 0.01));
        this.registerSetting(inputStrafeLegit = new ButtonSetting("InputStrafeLegit", false));
        this.registerSetting(inputStrafe = new SliderSetting("InputStrafe", 0, -1, 1, 0.01));
        this.registerSetting(inputJumpLegit = new ButtonSetting("InputJumpLegit", false));
        this.registerSetting(inputJump = new ButtonSetting("InputJump", false));
        this.registerSetting(inputSneakLegit = new ButtonSetting("InputSneakLegit", false));
        this.registerSetting(inputSneak = new ButtonSetting("InputSneak", false));
        this.registerSetting(bug = new ButtonSetting("Bug", false));
        this.registerSetting(timer = new ButtonSetting("Timer", false));
        this.registerSetting(timerValue = new SliderSetting("Timer", 0.2, 0.1, 2, 0.01));
        this.registerSetting(blink = new ButtonSetting("Blink", false));
    }

    public void onPreUpdate(PreUpdateEvent event) {
        if (switch1.isToggled() && parent.switchMode.getInput() == 0) {
            PacketUtils.sendPacket(new C09PacketHeldItemChange(SlotHandler.getCurrentSlot() % 8 + 1));
            PacketUtils.sendPacket(new C09PacketHeldItemChange(SlotHandler.getCurrentSlot()));
        }
        if (slot.isToggled() && parent.switchMode.getInput() == 0) {
            PacketUtils.sendPacket(new C09PacketHeldItemChange(SlotHandler.getCurrentSlot()));
        }
    }

    public void onPostUpdate(PostUpdateEvent event) {
        if (switch1.isToggled() && parent.switchMode.getInput() == 1) {
            PacketUtils.sendPacket(new C09PacketHeldItemChange(SlotHandler.getCurrentSlot() % 8 + 1));
            PacketUtils.sendPacket(new C09PacketHeldItemChange(SlotHandler.getCurrentSlot()));
        }
        if (slot.isToggled() && parent.switchMode.getInput() == 1) {
            PacketUtils.sendPacket(new C09PacketHeldItemChange(SlotHandler.getCurrentSlot()));
        }
    }

    public void onPreMotion(PreMotionEvent event) {
        if (switch1.isToggled() && parent.switchMode.getInput() == 2) {
            PacketUtils.sendPacket(new C09PacketHeldItemChange(SlotHandler.getCurrentSlot() % 8 + 1));
            PacketUtils.sendPacket(new C09PacketHeldItemChange(SlotHandler.getCurrentSlot()));
        }
        if (slot.isToggled() && parent.switchMode.getInput() == 2) {
            PacketUtils.sendPacket(new C09PacketHeldItemChange(SlotHandler.getCurrentSlot()));
        }
        if (onGround.isToggled()) {
            event.setOnGround(true);
        }
        if (toggle.isToggled()) {
            PacketUtils.sendPacket(new C08PacketPlayerBlockPlacement(SlotHandler.getHeldItem()));
        }
        if (input.isToggled()) {
            PacketUtils.sendPacket(new C0CPacketInput(
                    inputForwardLegit.isToggled() ? mc.thePlayer.movementInput.moveForward : (float) inputForward.getInput(),
                    inputStrafeLegit.isToggled() ? mc.thePlayer.movementInput.moveStrafe : (float) inputStrafe.getInput(),
                    inputJumpLegit.isToggled() ? mc.thePlayer.movementInput.jump : inputJump.isToggled(),
                    inputSneakLegit.isToggled() ? mc.thePlayer.movementInput.sneak : inputSneak.isToggled()
            ));
        }
        if (timer.isToggled()) {
            if (mc.thePlayer.isUsingItem()) {
                Utils.getTimer().timerSpeed = (float) timerValue.getInput();
            } else {
                Utils.resetTimer();
            }
        }
        if (blink.isToggled()) {
            if (mc.thePlayer.isUsingItem()) {
                ModuleManager.blink.enable();
            } else {
                ModuleManager.blink.disable();
            }
        }
    }

    public void onPostMotion(PostMotionEvent event) {
        if (switch1.isToggled() && parent.switchMode.getInput() == 3) {
            PacketUtils.sendPacket(new C09PacketHeldItemChange(SlotHandler.getCurrentSlot() % 8 + 1));
            PacketUtils.sendPacket(new C09PacketHeldItemChange(SlotHandler.getCurrentSlot()));
        }
        if (slot.isToggled() && parent.switchMode.getInput() == 3) {
            PacketUtils.sendPacket(new C09PacketHeldItemChange(SlotHandler.getCurrentSlot()));
        }
    }

    @Override
    public void onDisable() {
        Utils.resetTimer();
        ModuleManager.blink.disable();
    }

    public void onSendPacket(@NotNull SendPacketEvent event) {
        if (event.getPacket() instanceof C08PacketPlayerBlockPlacement && !event.isCanceled() && bug.isToggled()) {
            C08PacketPlayerBlockPlacement packet = (C08PacketPlayerBlockPlacement) event.getPacket();
            if (!mc.thePlayer.isUsingItem()) {
                event.setCanceled(true);
                PacketUtils.sendPacketNoEvent(packet);
                PacketUtils.sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            }
        }
    }

    public float getForwardSlowed() {
        return slowDown.isToggled() ? (float) slowDownForward.getInput() : 0.2f;
    }

    public float getStrafeSlowed() {
        return slowDown.isToggled() ? (float) slowDownStrafe.getInput() : 0.2f;
    }
}
