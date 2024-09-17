package keystrokesmod.module.impl.world.tower;

import keystrokesmod.Raven;
import keystrokesmod.event.MoveEvent;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.world.Tower;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Reflection;
import keystrokesmod.utility.RotationUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.block.BlockAir;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class HypixelTower extends SubMode<Tower> {
    public static final HashSet<EnumFacing> LIMIT_FACING = new HashSet<>(Collections.singleton(EnumFacing.SOUTH));
    public static final double STEP_MOVE = 0.3;
    private boolean towering;
    private int towerTicks;
    private boolean blockPlaceRequest = false;
    private int lastOnGroundY;
    private BlockPos deltaPlace = BlockPos.ORIGIN;
    private final ButtonSetting onlyWhileMoving;

    public HypixelTower(String name, @NotNull Tower parent) {
        super(name, parent);
        this.registerSetting(onlyWhileMoving = new ButtonSetting("Only while moving", true));
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) throws IllegalAccessException {
        if (mc.thePlayer.isPotionActive(Potion.jump)) return;
        final boolean airUnder = negativeExpand(0.239);

        if (!MoveUtil.isMoving() && parent.canTower()) {
            if (onlyWhileMoving.isToggled()) return;

            final double targetZPos = Math.floor(mc.thePlayer.posZ) + 0.99999999999998;
            if (mc.thePlayer.posZ != targetZPos) {
                if (mc.thePlayer.posY - lastOnGroundY < 1) return;
                MoveUtil.stop();
                if (targetZPos > mc.thePlayer.posZ) {
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY, Math.min(mc.thePlayer.posZ + STEP_MOVE, targetZPos));
                } else {
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY, Math.max(mc.thePlayer.posZ - STEP_MOVE, targetZPos));
                }
                return;
            }

            blockPlaceRequest = true;
        }

        if (MoveUtil.speed() > 0.1 || (!MoveUtil.isMoving() && !onlyWhileMoving.isToggled())) {
            double towerSpeed = isGoingDiagonally(0.1) ? 0.22 : 0.29888888;
            if (!mc.thePlayer.onGround) {
                if (this.towering) {
                    if (this.towerTicks == 2) {
                        event.setY(Math.floor(mc.thePlayer.posY + 1.0) - mc.thePlayer.posY);
                    } else if (this.towerTicks == 3) {
                        if (parent.canTower() && !airUnder) {
                            event.setY(mc.thePlayer.motionY = 0.4198499917984009);
                            if (MoveUtil.isMoving())
                                MoveUtil.strafe((float) towerSpeed - this.randomAmount());
                            this.towerTicks = 0;
                        } else {
                            this.towering = false;
                        }
                    }
                }
            } else {
                this.towering = parent.canTower() && !airUnder;
                if (this.towering) {
                    this.towerTicks = 0;
                    Reflection.jumpTicks.set(mc.thePlayer, 0);
                    if (event.getY() > 0.0) {
                        event.setY(mc.thePlayer.motionY = 0.4198479950428009);
                        if (MoveUtil.isMoving())
                            MoveUtil.strafe((float) towerSpeed - this.randomAmount());
                    }
                }
            }

            ++this.towerTicks;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer.onGround) {
            lastOnGroundY = (int) mc.thePlayer.posY;
            deltaPlace = new BlockPos(0, 1, 1);
        } else {
        }

        if (blockPlaceRequest && !Utils.isMoving() && !onlyWhileMoving.isToggled()) {
            MovingObjectPosition lastScaffoldPlace = ModuleManager.scaffold.placeBlock;
            if (lastScaffoldPlace == null)
                return;
            Optional<Triple<BlockPos, EnumFacing, Vec3>> optionalPlaceSide = RotationUtils.getPlaceSide(
                    lastScaffoldPlace.getBlockPos().add(deltaPlace),
                    LIMIT_FACING
            );
            if (!optionalPlaceSide.isPresent())
                return;

            Triple<BlockPos, EnumFacing, Vec3> placeSide = optionalPlaceSide.get();

            Raven.getExecutor().schedule(() -> ModuleManager.scaffold.place(
                    new MovingObjectPosition(placeSide.getRight().toVec3(), placeSide.getMiddle(), placeSide.getLeft()),
                    false
            ), 50, TimeUnit.MILLISECONDS);
//            ModuleManager.scaffold.tower$noBlockPlace = true;
            blockPlaceRequest = false;
        }
    }

    public static boolean isGoingDiagonally(double amount) {
        return Math.abs(mc.thePlayer.motionX) > amount && Math.abs(mc.thePlayer.motionZ) > amount;
    }

    public static boolean negativeExpand(double negativeExpandValue) {
        return mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX + negativeExpandValue, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ + negativeExpandValue)).getBlock() instanceof BlockAir && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX - negativeExpandValue, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ - negativeExpandValue)).getBlock() instanceof BlockAir && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX - negativeExpandValue, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)).getBlock() instanceof BlockAir && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX + negativeExpandValue, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)).getBlock() instanceof BlockAir && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ + negativeExpandValue)).getBlock() instanceof BlockAir && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ - negativeExpandValue)).getBlock() instanceof BlockAir;
    }

    private double randomAmount() {
        return 8.0E-4 + Math.random() * 0.008;
    }
}
