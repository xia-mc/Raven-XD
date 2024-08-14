package keystrokesmod.module.impl.player;

import akka.japi.Pair;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.event.RotationEvent;
import keystrokesmod.module.impl.combat.autoclicker.IAutoClicker;
import keystrokesmod.module.impl.combat.autoclicker.NormalAutoClicker;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.impl.other.anticheats.utils.phys.Vec2;
import keystrokesmod.module.impl.other.anticheats.utils.world.PlayerRotation;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeValue;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.aim.AimSimulator;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.RotationUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChestAura extends IAutoClicker {
    private final ModeValue clickMode;
    private final ButtonSetting oneClick;
    private final SliderSetting aimSpeed;
    private final SliderSetting aimRange;
    private final SliderSetting delay;
    private final ButtonSetting rayCast;
    private final ButtonSetting onlyCloseChest;
    private final ButtonSetting onlyNotBelowChest;
    private final ButtonSetting targetNearbyCheck;

    private boolean click = false;
    private Pair<BlockPos, MovingObjectPosition> target = null;
    private float yaw;
    private float pitch;
    private Float lastYaw = null, lastPitch = null;
    private final Set<BlockPos> clicked = new HashSet<>();
    private final Set<BlockPos> opened = new HashSet<>();
    private long lastDone = 0;

    public ChestAura() {
        super("ChestAura", category.player);
        this.registerSetting(clickMode = new ModeValue("Click mode", this)
                .add(new NormalAutoClicker("Normal", this, false, true))
        );
        this.registerSetting(oneClick = new ButtonSetting("One click", true));
        this.registerSetting(aimSpeed = new SliderSetting("Aim speed", 10, 5, 20, 1));
        this.registerSetting(aimRange = new SliderSetting("Aim range", 4.5, 1, 6, 0.1));
        this.registerSetting(delay = new SliderSetting("Delay", 100, 0, 1000, 50, "ms"));
        this.registerSetting(rayCast = new ButtonSetting("Ray cast", false));
        this.registerSetting(onlyCloseChest = new ButtonSetting("Only close chest", true));
        this.registerSetting(onlyNotBelowChest = new ButtonSetting("Only not below chest", true));
        this.registerSetting(targetNearbyCheck = new ButtonSetting("Target nearby check", true));
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        if (targetNearbyCheck.isToggled() && Utils.isTargetNearby()) {
            target = null;
            return;
        }

        long time = System.currentTimeMillis();
        if (mc.currentScreen instanceof GuiChest && target != null) {
            opened.add(target.first());
            lastDone = time;
        }

        if (time - lastDone < delay.getInput()) {
            target = null;
            return;
        }

        target = getNearestChest();

        if (target != null) {
            yaw = PlayerRotation.getYaw(new Vec3(target.second().hitVec));
            pitch = PlayerRotation.getPitch(new Vec3(target.second().hitVec));

            if (!mc.thePlayer.isSneaking() && click && mc.currentScreen == null) {
                MovingObjectPosition hitResult;
                if (rayCast.isToggled()) {
                    hitResult = RotationUtils.rayCast(4.5, RotationHandler.getRotationYaw(), RotationHandler.getRotationPitch());
                } else {
                    if (AimSimulator.equals(
                            new Vec2(yaw, pitch),
                            new Vec2(RotationHandler.getRotationYaw(), RotationHandler.getRotationPitch())
                    ))
                        hitResult = target.second();
                    else
                        hitResult = null;
                }
                if (hitResult != null && hitResult.getBlockPos().equals(target.first())) {
                    if (oneClick.isToggled() && clicked.contains(hitResult.getBlockPos())) return;

                    if (mc.playerController.onPlayerRightClick(
                            mc.thePlayer, mc.theWorld,
                            SlotHandler.getHeldItem(),
                            hitResult.getBlockPos(), hitResult.sideHit, hitResult.hitVec
                    )) {
                        mc.thePlayer.swingItem();
                        clicked.add(hitResult.getBlockPos());
                    }
                    click = false;
                }
            }
        }
    }

    @SubscribeEvent
    public void onRotation(RotationEvent event) {
        if (target == null) {
            lastYaw = null;
            lastPitch = null;
        } else {
            if (lastYaw == null || lastPitch == null) {
                lastYaw = event.getYaw();
                lastPitch = event.getPitch();
            }

            event.setYaw(lastYaw = AimSimulator.rotMove(yaw, lastYaw, (float) aimSpeed.getInput()));
            event.setPitch(lastPitch = AimSimulator.rotMove(pitch, lastPitch, (float) aimSpeed.getInput()));
        }
    }

    private @Nullable Pair<BlockPos, MovingObjectPosition> getNearestChest() {
        final Vec3 eyePos = Utils.getEyePos();
        final List<BlockPos> blockPosList = BlockUtils.getAllInSphere(eyePos, aimRange.getInput());

        BlockPos result = null;
        Vec3 hitPos = null;
        AxisAlignedBB box = null;
        double distance = aimRange.getInput();
        for (BlockPos pos : blockPosList) {
            if (opened.contains(pos)) continue;
            IBlockState state = BlockUtils.getBlockState(pos);
            TileEntity tileEntity = mc.theWorld.getTileEntity(pos);

            if (state.getBlock() instanceof BlockChest && tileEntity instanceof TileEntityChest) {
                if (onlyCloseChest.isToggled() && ((TileEntityChest) tileEntity).numPlayersUsing > 0) continue;
                if (onlyNotBelowChest.isToggled() && !BlockUtils.replaceable(pos.up())) continue;

                AxisAlignedBB curBox = BlockUtils.getCollisionBoundingBox(pos);
                if (curBox == null) continue;

                Vec3 point = RotationUtils.getNearestPoint(curBox, eyePos);
                double curDist = point.distanceTo(eyePos);
                float curYaw = PlayerRotation.getYaw(point);
                float curPitch = PlayerRotation.getPitch(point);

                if (!rayCast.isToggled()) {
                    if (RotationUtils.rayCast(curDist - 0.1, curYaw, curPitch) != null) {
                        continue;
                    }
                }

                if (curDist < distance) {
                    result = pos;
                    hitPos = point;
                    box = curBox;
                    distance = curDist;
                }
            }
        }

        if (result != null) {
            return new Pair<>(result, new MovingObjectPosition(
                    MovingObjectPosition.MovingObjectType.BLOCK,
                    hitPos.toVec3(), RotationUtils.getEnumFacing(hitPos, box), result
            ));
        }
        return null;
    }

    @Override
    public boolean click() {
        click = true;
        return true;
    }

    @Override
    public void onEnable() {
        clickMode.enable();
        click = false;
        target = null;
        lastYaw = lastPitch = null;
        clicked.clear();
        opened.clear();
    }

    @Override
    public void onDisable() {
        clickMode.disable();
    }
}
