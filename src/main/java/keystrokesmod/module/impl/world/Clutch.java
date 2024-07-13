package keystrokesmod.module.impl.world;

import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.impl.other.anticheats.utils.phys.Vec2;
import keystrokesmod.module.impl.other.anticheats.utils.world.PlayerRotation;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.*;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;
import java.util.Optional;

public class Clutch extends Module {
    private final ModeSetting mode = new ModeSetting("Rotation mode", new String[]{"None", "Block", "Strict"}, 2);
    private final SliderSetting aimSpeed = new SliderSetting("Aim speed", 20, 10, 30, 0.5, new ModeOnly(mode, 1, 2));
    private final ButtonSetting lookView = new ButtonSetting("Look view", false, new ModeOnly(mode, 1, 2));
    private final SliderSetting placeDelay = new SliderSetting("Place delay", 50, 0, 500, 1, "ms");
    private final ButtonSetting overVoid = new ButtonSetting("Over void", true);
    private final ButtonSetting fallDistance = new ButtonSetting("Fall distance", false);
    private final SliderSetting minFallDistance = new SliderSetting("Min fall distance", 6, 0, 10, 1, fallDistance::isToggled);
    private final ButtonSetting autoSwitch = new ButtonSetting("Auto switch", false);
    private final ButtonSetting silentSwing = new ButtonSetting("Silent swing", false);
    private Vec2 rot = null;
    private long lastPlace = -1;

    public Clutch() {
        super("Clutch", category.experimental);
        this.registerSetting(mode, aimSpeed, lookView, placeDelay, overVoid, fallDistance, minFallDistance, autoSwitch, silentSwing);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPreUpdate(PreUpdateEvent event) {
        if (!shouldClutch()) {
            rot = null;
            return;
        }
        if (autoSwitch.isToggled()) {
            ItemStack item = SlotHandler.getHeldItem();
            if ((item == null || !(item.getItem() instanceof ItemBlock) || !ContainerUtils.canBePlaced((ItemBlock) item.getItem()))) {
                int slot = Scaffold.getSlot();
                SlotHandler.setCurrentSlot(slot);
            }
        }
        if (SlotHandler.getHeldItem() == null || !(SlotHandler.getHeldItem().getItem() instanceof ItemBlock)) {
            rot = null;
            return;
        }

        if (rot == null) {
            rot = new Vec2(RotationHandler.getRotationYaw(), RotationHandler.getRotationPitch());
        }

        final Vec3 eyePos = Utils.getEyePos();
        final BlockPos position = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        final Vec3 groundPos = new Vec3(position.getX() + 0.5, position.getY() - 1, position.getZ() + 0.5);
        final List<BlockPos> blocks = BlockUtils.getAllInBox(position.add(-5, -5, -5), position.add(5, 0, 5));

        double minDistance = Double.MAX_VALUE;
        Triple<BlockPos, EnumFacing, Vec3> bestPlace = null;
        for (BlockPos block : blocks) {
            if (!BlockUtils.replaceable(block) || BlockUtils.isSamePos(block, position) || BlockUtils.isSamePos(block, position.up())) continue;

            final Optional<Triple<BlockPos, EnumFacing, Vec3>> optional = RotationUtils.getPlaceSide(block);
            if (!optional.isPresent()) continue;
            final Triple<BlockPos, EnumFacing, Vec3> place = optional.get();

            final double placeDistance = place.getRight().distanceTo(eyePos);
            if (placeDistance > 4.5) continue;

            final float yaw = PlayerRotation.getYaw(place.getRight());
            final float pitch = PlayerRotation.getPitch(place.getRight());
            if ((int) mode.getInput() == 2) {
                MovingObjectPosition hitResult = RotationUtils.rayCast(4.5, yaw, pitch);
                if (hitResult == null || !BlockUtils.isSamePos(hitResult.getBlockPos(), place.getLeft()))
                    return;
            }

            final double blockDistance = place.getRight().distanceTo(groundPos);

            if (blockDistance < minDistance) {
                minDistance = blockDistance;
                bestPlace = place;
            }
        }

        if (bestPlace != null) {
            final BlockPos pos = bestPlace.getLeft();
            final EnumFacing facing = bestPlace.getMiddle();
            final Vec3 hitPos = bestPlace.getRight();

            if (mode.getInput() != 0) {  // need rotation
                final float yaw = PlayerRotation.getYaw(hitPos);
                final float pitch = PlayerRotation.getPitch(hitPos);

                rot.x = AimSimulator.rotMove(yaw, rot.x, (float) aimSpeed.getInput());
                rot.y = AimSimulator.rotMove(pitch, rot.y, (float) aimSpeed.getInput());
                if (lookView.isToggled()) {
                    mc.thePlayer.rotationYaw = rot.x;
                    mc.thePlayer.rotationPitch = rot.y;
                } else {
                    RotationHandler.setRotationYaw(rot.x);
                    RotationHandler.setRotationPitch(rot.y);
                }

                switch ((int) mode.getInput()) {
                    case 1:
                        if (rot.x != yaw || rot.y != pitch) return;
                        break;
                    case 2:
                        MovingObjectPosition hitResult = RotationUtils.rayCast(4.5, yaw, pitch);
                        if (hitResult == null || !BlockUtils.isSamePos(hitResult.getBlockPos(), pos))
                            return;
                        break;
                }
            }

            long time = System.currentTimeMillis();
            if (time - lastPlace < placeDelay.getInput()) return;

            if (mc.playerController.onPlayerRightClick(
                    mc.thePlayer, mc.theWorld,
                    mc.thePlayer.getHeldItem(),
                    pos, facing,
                    hitPos.toVec3()
            )) {
                if (silentSwing.isToggled()) {
                    mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                } else {
                    mc.thePlayer.swingItem();
                    mc.getItemRenderer().resetEquippedProgress();
                }
                lastPlace = time;
            }
        }
    }

    private boolean shouldClutch() {
        if (overVoid.isToggled() && Utils.overVoid()) return true;
        return fallDistance.isToggled() && mc.thePlayer.fallDistance >= minFallDistance.getInput();
    }
}
