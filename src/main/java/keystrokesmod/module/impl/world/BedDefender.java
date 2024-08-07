package keystrokesmod.module.impl.world;

import keystrokesmod.event.MoveInputEvent;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.event.RotationEvent;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.combat.autoclicker.IAutoClicker;
import keystrokesmod.module.impl.combat.autoclicker.NormalAutoClicker;
import keystrokesmod.module.impl.combat.autoclicker.RecordAutoClicker;
import keystrokesmod.module.impl.minigames.BedWars;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.impl.other.anticheats.utils.world.PlayerRotation;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeValue;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.*;
import net.minecraft.block.BlockBed;
import net.minecraft.item.ItemBlock;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class BedDefender extends IAutoClicker {
    private final ModeValue clickMode;
    private final SliderSetting aimSpeed;
    private final ButtonSetting autoSwitch;
    private final ButtonSetting rayCast;
    private final ButtonSetting silentSwing;

    private Float lastYaw, lastPitch = null;
    private Vec3 targetToAim = null;
    private boolean doneAim = false;
    private boolean click = false;

    public BedDefender() {
        super("BedDefender", category.world);
        this.registerSetting(clickMode = new ModeValue("Click mode", this)
                .add(new NormalAutoClicker("Normal", this, false, true))
                .add(new RecordAutoClicker("Record", this, false, true))
        );
        this.registerSetting(aimSpeed = new SliderSetting("Aim speed", 10, 2, 20, 0.1));
        this.registerSetting(autoSwitch = new ButtonSetting("Auto switch", false));
        this.registerSetting(rayCast = new ButtonSetting("Ray cast", false));
        this.registerSetting(silentSwing = new ButtonSetting("Silent swing", false));
    }

    @Override
    public void onEnable() {
        clickMode.enable();
    }

    @Override
    public void onDisable() {
        clickMode.disable();

        lastYaw = lastPitch = null;
        targetToAim = null;
        doneAim = false;
        click = false;
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        if (ModuleManager.bedwars.isEnabled() && BedWars.outsideSpawn) {
            targetToAim = null;
            return;
        }

        List<Triple<BlockPos, EnumFacing, Vec3>> poses = getNeedToDefender();
        if (poses.isEmpty()) {
            lastYaw = lastPitch = null;
            targetToAim = null;
            doneAim = false;
            return;
        }


        for (Triple<BlockPos, EnumFacing, Vec3> place : poses) {
            if (SlotHandler.getHeldItem() == null
                    || !(SlotHandler.getHeldItem().getItem() instanceof ItemBlock)
                    || !ContainerUtils.canBePlaced(((ItemBlock) SlotHandler.getHeldItem().getItem()))) {
                if (autoSwitch.isToggled()) {
                    SlotHandler.setCurrentSlot(Scaffold.getSlot());
                }
                targetToAim = null;
                return;
            }

            if (rayCast.isToggled()) {
                MovingObjectPosition hitResult = RotationUtils.rayCast(4.5, PlayerRotation.getYaw(place.getRight()), PlayerRotation.getPitch(place.getRight()));
                if (hitResult == null || hitResult.hitVec.distanceTo(place.getRight().toVec3()) > 0.05)
                    continue;
            }

            if (targetToAim == null || !place.getRight().equals(targetToAim)) {
                targetToAim = place.getRight();
                doneAim = false;
            } else if (doneAim) {
                if (click) {
                    if (silentSwing.isToggled()) {
                        PacketUtils.sendPacket(new C0APacketAnimation());
                    } else {
                        mc.thePlayer.swingItem();
                    }
                    mc.playerController.onPlayerRightClick(
                            mc.thePlayer, mc.theWorld,
                            SlotHandler.getHeldItem(),
                            place.getLeft(), place.getMiddle(), place.getRight().toVec3()
                    );
                }
            }
            return;
        }
    }

    @SubscribeEvent
    public void onMoveInput(@NotNull MoveInputEvent event) {
        if (targetToAim != null)
            event.setSneak(true);
    }

    @SubscribeEvent
    public void onRotation(RotationEvent event) {
        if (targetToAim != null) {
            if (lastYaw == null || lastPitch == null) {
                lastYaw = event.getYaw();
                lastPitch = event.getPitch();
            }

            event.setYaw(AimSimulator.rotMove(PlayerRotation.getYaw(targetToAim), lastYaw, (float) aimSpeed.getInput()));
            event.setPitch(AimSimulator.rotMove(PlayerRotation.getPitch(targetToAim), lastPitch, (float) aimSpeed.getInput()));

            if (lastYaw == event.getYaw() && lastPitch == event.getPitch())
                doneAim = true;

            lastYaw = event.getYaw();
            lastPitch = event.getPitch();
        } else {
            lastYaw = null;
            lastPitch = null;
            doneAim = false;
        }
    }

    private @NotNull List<Triple<BlockPos, EnumFacing, Vec3>> getNeedToDefender() {
        final BlockPos playerPos = new BlockPos(mc.thePlayer).up();
        final Vec3 eyePos = Utils.getEyePos();
        final List<BlockPos> box = BlockUtils.getAllInBox(playerPos.add(-5, -5, -5), playerPos.add(5, 5, 5));

        final List<Triple<BlockPos, EnumFacing, Vec3>> result = new ArrayList<>();
        for (BlockPos block : box) {
            if (BlockUtils.getBlock(block) instanceof BlockBed) {
                List<BlockPos> surroundBlocks = Arrays.asList(
                        block.north(), block.south(), block.west(), block.east(), block.up()
                );
                for (BlockPos pos : surroundBlocks) {
                    if (!BlockUtils.replaceable(pos))
                        continue;

                    Optional<Triple<BlockPos, EnumFacing, Vec3>> place = RotationUtils.getPlaceSide(pos);
                    if (!place.isPresent()) continue;
                    if (place.get().getRight().distanceTo(eyePos) > 4.5) continue;
                    result.add(place.get());
                }
            }
        }
        return result;
    }

    @Override
    public boolean click() {
        click = true;
        return true;
    }
}
