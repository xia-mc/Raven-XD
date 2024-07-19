package keystrokesmod.module.impl.world;

import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.event.RotationEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.impl.other.anticheats.utils.phys.Vec2;
import keystrokesmod.module.impl.other.anticheats.utils.world.PlayerRotation;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.*;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BlockIn extends Module {
    private static final String[] rotationModes = new String[]{"None", "Block", "Strict"};
    private final ModeSetting rotationMode;
    private final SliderSetting aimSpeed;
    private final ButtonSetting lookView;
    private final SliderSetting placeDelay;
    private final ButtonSetting silentSwing;
    private final ButtonSetting autoSwitch;

    private Vec2 currentRot = null;
    private long lastPlace = 0;

    private int lastSlot = -1;

    public BlockIn() {
        super("Block-In", category.world);
        this.registerSetting(new DescriptionSetting("make you block in."));
        this.registerSetting(rotationMode = new ModeSetting("Rotation mode", rotationModes, 2));
        this.registerSetting(aimSpeed = new SliderSetting("Aim speed", 5, 0, 10, 0.05, new ModeOnly(rotationMode, 1, 2)));
        this.registerSetting(lookView = new ButtonSetting("Look view", false, new ModeOnly(rotationMode, 1, 2)));
        this.registerSetting(placeDelay = new SliderSetting("Place delay", 50, 0, 500, 1, "ms"));
        this.registerSetting(silentSwing = new ButtonSetting("Silent swing", false));
        this.registerSetting(autoSwitch = new ButtonSetting("Auto switch", true));
    }

    @Override
    public void onDisable() {
        currentRot = null;
        lastPlace = 0;
        if (autoSwitch.isToggled() && lastSlot != -1) {
            SlotHandler.setCurrentSlot(lastSlot);
        }
        lastSlot = -1;
    }

    @SubscribeEvent
    public void onPreMotion(RotationEvent event) {
        if (currentRot == null) return;
        if (rotationMode.getInput() == 0) {
            currentRot = null;
            return;
        }
        if (!lookView.isToggled()) {
            event.setYaw(currentRot.x);
            event.setPitch(currentRot.y);
        }
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        if (autoSwitch.isToggled() && lastSlot == -1) {
            int slot = Scaffold.getSlot();
            lastSlot = SlotHandler.getCurrentSlot();
            SlotHandler.setCurrentSlot(slot);
        }

        try {
            if (!(Objects.requireNonNull(SlotHandler.getHeldItem()).getItem() instanceof ItemBlock)) {
                Utils.sendMessage("No blocks found.");
                disable();
                return;
            }
        } catch (NullPointerException e) {
            Utils.sendMessage("No blocks found.");
            disable();
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPlace < placeDelay.getInput()) return;

        int placed = 0;
        for (BlockPos blockPos : getBlockInBlocks()) {
            if (currentTime - lastPlace < placeDelay.getInput()) return;
            if (!BlockUtils.replaceable(blockPos)) continue;

            Triple<BlockPos, EnumFacing, Vec3> placeSideBlock;
            try {
                placeSideBlock = RotationUtils.getPlaceSide(blockPos).orElseThrow(NoSuchElementException::new);
            } catch (NoSuchElementException e) {
                continue;
            }

            Vec3 hitPos = placeSideBlock.getRight();
            Vec2 rotation = new Vec2(PlayerRotation.getYaw(hitPos), PlayerRotation.getPitch(hitPos));

            if ((int) rotationMode.getInput() == 2) {
                MovingObjectPosition hitResult = RotationUtils.rayCast(4.5, rotation.x, rotation.y);
                if (hitResult != null && hitPos.distanceTo(hitResult.hitVec) > 0.05) continue;
            }

            if (currentRot == null) {
                currentRot = new Vec2(RotationHandler.getRotationYaw(), RotationHandler.getRotationPitch());
            }
            if (rotationMode.getInput() != 0 && !AimSimulator.equals(currentRot, rotation)) {
                currentRot = new Vec2(
                        AimSimulator.rotMove(rotation.x, currentRot.x, (float) aimSpeed.getInput() * 5),
                        AimSimulator.rotMove(rotation.y, currentRot.y, (float) aimSpeed.getInput() * 5)
                );

                if (lookView.isToggled()) {
                    mc.thePlayer.rotationYaw = currentRot.x;
                    mc.thePlayer.rotationPitch = currentRot.y;
                }
                return;
            }

            if (rotationMode.getInput() == 0 || AimSimulator.equals(currentRot, rotation)) {
                if (mc.playerController.onPlayerRightClick(
                        mc.thePlayer, mc.theWorld,
                        SlotHandler.getHeldItem(),
                        placeSideBlock.getLeft(), placeSideBlock.getMiddle(),
                        hitPos.toVec3()
                )) {
                    if (silentSwing.isToggled()) {
                        mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                    } else {
                        mc.thePlayer.swingItem();
                        mc.getItemRenderer().resetEquippedProgress();
                    }

                    lastPlace = currentTime;
                    placed++;
                }
            }
        }
        if (placed == 0) disable();
    }

    private @NotNull Set<BlockPos> getBlockInBlocks() {
        return BlockUtils.getSurroundBlocks(mc.thePlayer);
    }

}
