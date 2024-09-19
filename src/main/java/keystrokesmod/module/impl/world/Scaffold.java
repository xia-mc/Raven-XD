package keystrokesmod.module.impl.world;

import keystrokesmod.Raven;
import keystrokesmod.event.*;
import keystrokesmod.mixins.impl.client.KeyBindingAccessor;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.client.Notifications;
import keystrokesmod.module.impl.combat.autoclicker.IAutoClicker;
import keystrokesmod.module.impl.combat.autoclicker.NormalAutoClicker;
import keystrokesmod.module.impl.exploit.disabler.hypixel.HypixelMotionDisabler;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.impl.other.anticheats.utils.world.PlayerRotation;
import keystrokesmod.module.impl.render.HUD;
import keystrokesmod.module.impl.world.scaffold.IScaffoldRotation;
import keystrokesmod.module.impl.world.scaffold.IScaffoldSprint;
import keystrokesmod.module.impl.world.scaffold.rotation.*;
import keystrokesmod.module.impl.world.scaffold.sprint.*;
import keystrokesmod.module.setting.impl.*;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.utility.*;
import keystrokesmod.utility.Timer;
import keystrokesmod.utility.aim.AimSimulator;
import keystrokesmod.utility.aim.RotationData;
import keystrokesmod.utility.render.RenderUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.util.*;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Scaffold extends IAutoClicker {
    private final ModeValue clickMode;
    private final ButtonSetting alwaysPlaceIfPossible;
    private final SliderSetting aimSpeed;
    private final SliderSetting motion;
    private final ModeValue rotation;
    private final ButtonSetting moveFix;
    public final SliderSetting strafe;
    private final ModeValue sprint;
    private final ButtonSetting fast;
    private final ButtonSetting cancelSprint;
    private final ButtonSetting legit;
    private final ButtonSetting recycleRotation;
    private final ButtonSetting hover;
    private final ButtonSetting sneak;
    private final SliderSetting sneakEveryBlocks;
    private final ButtonSetting jump;
    private final SliderSetting jumpEveryBlocks;
    private final SliderSetting sneakTime;
    private final ButtonSetting rotateWithMovement;
    private final ButtonSetting staticYaw;
    private final ButtonSetting reserveYaw;
    private final ButtonSetting staticPitch;
    private final ButtonSetting staticPitchOnJump;
    private final SliderSetting straightPitch;
    private final SliderSetting diagonalPitch;
    private final ModeSetting precision;
    private final ButtonSetting autoSwap;
    private final ButtonSetting useBiggestStack;
    private final ButtonSetting fastOnRMB;
    private final ButtonSetting highlightBlocks;
    private final ButtonSetting multiPlace;
    public final ButtonSetting safeWalk;
    private final ButtonSetting showBlockCount;
    private final ButtonSetting delayOnJump;
    private final ButtonSetting silentSwing;
    private final ButtonSetting noSwing;
    public final ButtonSetting tower;
    public final ButtonSetting sameY;
    public final ButtonSetting autoJump;
    private final ButtonSetting expand;
    private final SliderSetting expandDistance;
    private final ButtonSetting polar;

    public @Nullable MovingObjectPosition rayCasted = null;
    public MovingObjectPosition placeBlock;
    private int lastSlot;
    private static final String[] precisionModes = new String[]{"Very low", "Low", "Moderate", "High", "Very high", "Unlimited"};
    public float placeYaw;
    public float placePitch = 85;
    public int at;
    public int index;
    public boolean rmbDown;
    private double startPos = -1;
    private final Map<BlockPos, Timer> highlight = new HashMap<>();
    private boolean forceStrict;
    private boolean down;
    private boolean delay;
    public boolean place;
    private int add = 0;
    private int sneak$bridged = 0;
    private int jump$bridged = 0;
    private boolean placedUp;
    public int offGroundTicks = 0;
    public int onGroundTicks = 0;
    public boolean telly$noBlockPlace = false;
    private Float lastYaw = null, lastPitch = null;
    private boolean polar$waitingForExpand = false;
    private boolean jumpScaffold$fast$cycle = false;
    private HoverState hoverState = HoverState.DONE;

    public Scaffold() {
        super("Scaffold", category.world);
        this.registerSetting(clickMode = new ModeValue("Click mode", this)
                .add(new LiteralSubMode("Basic", this))
                .add(new NormalAutoClicker("Normal", this, false, true))
                .setDefaultValue("Basic")
        );
        this.registerSetting(alwaysPlaceIfPossible = new ButtonSetting("Always place if possible", false));
        this.registerSetting(rotation = new ModeValue("Rotation", this)
                .add(new NoneRotation("None", this))
                .add(new BackwardsRotation("Backwards", this))
                .add(new StrictRotation("Strict", this))
                .add(new PreciseRotation("Precise", this))
                .add(new SimpleTellyRotation("SimpleTelly", this))
                .add(new TellyRotation("Telly", this))
                .add(new ConstantRotation("Constant", this))
                .add(new SnapRotation("Snap", this))
                .setDefaultValue("Backwards")
        );
        this.registerSetting(aimSpeed = new SliderSetting("Aim speed", 20, 5, 20, 0.1, new ModeOnly(rotation, 0).reserve()));
        this.registerSetting(moveFix = new ButtonSetting("MoveFix", false, new ModeOnly(rotation, 0).reserve()));
        this.registerSetting(motion = new SliderSetting("Motion", 1.0, 0.5, 1.2, 0.01, () -> !moveFix.isToggled()));
        this.registerSetting(strafe = new SliderSetting("Strafe", 0, -45, 45, 5));
        this.registerSetting(sprint = new ModeValue("Sprint", this)
                .add(new DisabledSprint("Disabled", this))
                .add(new VanillaSprint("Vanilla", this))
                .add(new EdgeSprint("Edge", this))
                .add(new JumpSprint("JumpA", this))
                .add(new JumpSprint("JumpB", this))
                .add(new JumpSprint("JumpC", this))
                .add(new SideSprint("Hypixel", this))
                .add(new LegitSprint("Legit", this))
                .add(new SneakSprint("Sneak", this))
                .add(new OldIntaveSprint("OldIntave", this))
        );
        this.registerSetting(fast = new ButtonSetting("Fast", false, new ModeOnly(sprint, 3, 4, 5, 11)));
        this.registerSetting(precision = new ModeSetting("Precision", precisionModes, 4));
        this.registerSetting(cancelSprint = new ButtonSetting("Cancel sprint", false, new ModeOnly(sprint, 0).reserve()));
        this.registerSetting(legit = new ButtonSetting("Legit", false));
        this.registerSetting(hover = new ButtonSetting("Hover", false));
        this.registerSetting(recycleRotation = new ButtonSetting("Recycle rotation", false));
        this.registerSetting(sneak = new ButtonSetting("Sneak", false));
        this.registerSetting(sneakEveryBlocks = new SliderSetting("Sneak every blocks", 0, 1, 10, 1, sneak::isToggled));
        this.registerSetting(sneakTime = new SliderSetting("Sneak time", 50, 0, 500, 10, "ms", sneak::isToggled));
        this.registerSetting(jump = new ButtonSetting("Jump", false));
        this.registerSetting(jumpEveryBlocks = new SliderSetting("Jump every blocks", 0, 1, 10, 1, jump::isToggled));
        this.registerSetting(rotateWithMovement = new ButtonSetting("Rotate with movement", true));
        this.registerSetting(staticYaw = new ButtonSetting("Static yaw", false));
        this.registerSetting(reserveYaw = new ButtonSetting("Reserve yaw", false));
        this.registerSetting(staticPitch = new ButtonSetting("Static pitch", false));
        this.registerSetting(staticPitchOnJump = new ButtonSetting("Static pitch on jump", false, staticPitch::isToggled));
        this.registerSetting(straightPitch = new SliderSetting("Straight pitch", 75.7, 45, 90, 0.1, staticPitch::isToggled));
        this.registerSetting(diagonalPitch = new SliderSetting("Diagonal pitch", 75.6, 45, 90, 0.1, staticPitch::isToggled));
        this.registerSetting(autoSwap = new ButtonSetting("AutoSwap", true));
        this.registerSetting(useBiggestStack = new ButtonSetting("Use biggest stack", true, autoSwap::isToggled));
        this.registerSetting(delayOnJump = new ButtonSetting("Delay on jump", true));
        this.registerSetting(fastOnRMB = new ButtonSetting("Fast on RMB", false));
        this.registerSetting(highlightBlocks = new ButtonSetting("Highlight blocks", true));
        this.registerSetting(multiPlace = new ButtonSetting("Multi-place", false));
        this.registerSetting(safeWalk = new ButtonSetting("Safewalk", true));
        this.registerSetting(showBlockCount = new ButtonSetting("Show block count", true));
        this.registerSetting(silentSwing = new ButtonSetting("Silent swing", false));
        this.registerSetting(noSwing = new ButtonSetting("No swing", false, silentSwing::isToggled));
        this.registerSetting(tower = new ButtonSetting("Tower", false));
        this.registerSetting(sameY = new ButtonSetting("SameY", false));
        this.registerSetting(autoJump = new ButtonSetting("Auto jump", false));
        this.registerSetting(expand = new ButtonSetting("Expand", false));
        this.registerSetting(expandDistance = new SliderSetting("Expand distance", 4.5, 0, 10, 0.1, expand::isToggled));
        this.registerSetting(polar = new ButtonSetting("Polar", false, expand::isToggled));
    }

    public void onDisable() {
        clickMode.disable();
        rotation.disable();
        sprint.disable();

        placeBlock = null;
        if (lastSlot != -1) {
            SlotHandler.setCurrentSlot(lastSlot);
            lastSlot = -1;
        }
        delay = false;
        highlight.clear();
        at = index = 0;
        add = 0;
        startPos = -1;
        forceStrict = false;
        down = false;
        place = false;
        placedUp = false;
        offGroundTicks = 0;
        telly$noBlockPlace = false;
        lastYaw = lastPitch = null;
        polar$waitingForExpand = false;
        Utils.resetTimer();
    }

    public void onEnable() {
        clickMode.enable();
        rotation.disable();
        sprint.disable();

        lastSlot = -1;
        startPos = mc.thePlayer.posY;
        sneak$bridged = 0;
        jump$bridged = 0;

        if (hover.isToggled() && mc.thePlayer.onGround) {
            hoverState = HoverState.JUMP;
        } else {
            hoverState = HoverState.DONE;
        }
    }

    @SubscribeEvent
    public void onRotation(RotationEvent event) {
        if (!Utils.nullCheck()) {
            return;
        }
        if (expand.isToggled() && polar.isToggled() && !polar$waitingForExpand)
            return;

        final RotationData data = ((IScaffoldRotation) rotation.getSelected()).onRotation(placeYaw, placePitch, forceStrict, event);
        float yaw = data.getYaw();
        float pitch = data.getPitch();

        boolean instant = aimSpeed.getInput() == aimSpeed.getMax();

        if (lastYaw == null || lastPitch == null) {
            lastYaw = event.getYaw();
            lastPitch = event.getPitch();
        }

        if (staticYaw.isToggled()) {
            float delta = yaw % 45;
            if (delta > 22.5 && delta <= 45)
                yaw += 45 - delta;
            else if (delta < -22.5 && delta >= -45)
                yaw -= 45 + delta;
            else if (delta <= 22.5 && delta > 0)
                yaw -= delta;
            else if (delta >= -22.5 && delta < 0)
                yaw -= delta;
        }

        if (reserveYaw.isToggled())
            yaw += 180;

        if (staticPitch.isToggled() && (staticPitchOnJump.isToggled() || mc.thePlayer.onGround)) {
            double direction = MoveUtil.direction();
            double movingYaw = Math.round(direction / 45) * 45;
            boolean isMovingStraight = movingYaw % 90 == 0f;

            if (isMovingStraight)
                pitch = (float) straightPitch.getInput();
            else
                pitch = (float) diagonalPitch.getInput();
        }

        float finalYaw = instant ? yaw : AimSimulator.rotMove(yaw, lastYaw, (float) aimSpeed.getInput());
        float finalPitch = instant ? pitch : AimSimulator.rotMove(pitch, lastPitch, (float) aimSpeed.getInput());

        event.setYaw(lastYaw = finalYaw);
        event.setPitch(lastPitch = finalPitch);
        event.setMoveFix(moveFix.isToggled() ? RotationHandler.MoveFix.Silent : RotationHandler.MoveFix.None);

        if (clickMode.getInput() == 0)
            place = true;
    }

    @Override
    public boolean click() {
        place = true;
        if (legit.isToggled()) {
            Utils.sendClick(1, true);
            Utils.sendClick(1, false);
        }
        return true;
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (cancelSprint.isToggled()) {
            event.setSprinting(false);
        }

        if (expand.isToggled() && polar.isToggled()) {
            if (!mc.thePlayer.onGround) {
                polar$waitingForExpand = false;
                return;
            }

            if (!polar$waitingForExpand && BlockUtils.replaceable(RotationUtils.getExtendedPos(new BlockPos(mc.thePlayer).down(), mc.thePlayer.rotationYaw, 1))) {
                final double pos = EnumFacing.fromAngle(getYaw()).getAxis() == EnumFacing.Axis.X ? Math.abs(mc.thePlayer.posX % 1) : Math.abs(mc.thePlayer.posZ % 1);
                polar$waitingForExpand = pos > 0.75 && pos < 0.95 || pos > 0.05 && pos < 0.25;
            }
        }
    }

    @SubscribeEvent
    public void onJump(JumpEvent e) {
        delay = true;
    }

    @SubscribeEvent
    public void onMoveInput(@NotNull MoveInputEvent event) {
        if (expand.isToggled() && polar.isToggled()) {
            if (polar$waitingForExpand) {
                event.setSneak(true);
                event.setForward(0);
                event.setStrafe(0);
            } else {
                event.setSneak(false);
            }
        }
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent event) {
        if (expand.isToggled() && polar.isToggled()) {
            if (event.getPacket() instanceof S23PacketBlockChange && polar$waitingForExpand) {
                S23PacketBlockChange packet = (S23PacketBlockChange) event.getPacket();
                double distance = expandDistance.getInput();
                if (packet.getBlockState().getBlock() instanceof BlockAir
                        && mc.thePlayer.getDistanceSqToCenter(packet.getBlockPosition()) <= distance * distance) {
                    polar$waitingForExpand = false;
                    Notifications.sendNotification(Notifications.NotificationTypes.WARN, "Polar cancel a place!");
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPreUpdate(PreUpdateEvent e) { // place here
        if (mc.thePlayer.onGround) {
            offGroundTicks = 0;
            onGroundTicks++;
        } else {
            offGroundTicks++;
            onGroundTicks = 0;
        }

        switch (hoverState) {
            case JUMP:
                if (mc.thePlayer.onGround && !Utils.jumpDown()) {
                    mc.thePlayer.jump();
                }
                hoverState = HoverState.FALL;
                break;
            case FALL:
                if (mc.thePlayer.onGround)
                    hoverState = HoverState.DONE;
                break;
        }

        if ((rotation.getInput() != 4 && autoJump.isToggled()) && mc.thePlayer.onGround && MoveUtil.isMoving() && !Utils.jumpDown()) {
            mc.thePlayer.jump();
        }

        if (delay && delayOnJump.isToggled()) {
            delay = false;
            return;
        }

        if (lastSlot == -1) {
            lastSlot = SlotHandler.getCurrentSlot();
        }
        int slot = SlotHandler.getCurrentSlot();
        if (autoSwap.isToggled()) {
            if (useBiggestStack.isToggled()) {
                slot = getSlot();
            } else if (SlotHandler.getHeldItem() == null
                    || !(SlotHandler.getHeldItem().getItem() instanceof ItemBlock)
                    || !ContainerUtils.canBePlaced((ItemBlock) SlotHandler.getHeldItem().getItem())) {
                slot = getSlot();
            }
        }
        SlotHandler.setCurrentSlot(slot);

        final ItemStack heldItem = SlotHandler.getHeldItem();
        if (heldItem == null || !(heldItem.getItem() instanceof ItemBlock) || !ContainerUtils.canBePlaced((ItemBlock) heldItem.getItem()))
            return;

        if (keepYPosition() && !down) {
            startPos = Math.floor(mc.thePlayer.posY);
            down = true;
        } else if (!keepYPosition()) {
            down = false;
            placedUp = false;
        }
        if (keepYPosition() && (sprint.getInput() == 3 || sprint.getInput() == 4 || sprint.getInput() == 5 || sprint.getInput() == 12)) {
            if (mc.thePlayer.onGround) {
                if (!Utils.jumpDown()) {
                    mc.thePlayer.jump();
                }
                add = 0;
                if (Math.floor(mc.thePlayer.posY) == Math.floor(startPos) && sprint.getInput() == 5) {
                    placedUp = false;
                }
            }
        }
        if (keepYPosition() && fast.isToggled()) {
            if (offGroundTicks == 5 && HypixelMotionDisabler.isDisabled() && !isDiagonal()) {
                mc.thePlayer.motionY = MoveUtil.predictedMotion(mc.thePlayer.motionY, jumpScaffold$fast$cycle ? 1 : 2);
                jumpScaffold$fast$cycle = !jumpScaffold$fast$cycle;
            }
        }

        if (!((IScaffoldRotation) rotation.getSelected()).onPreSchedulePlace())
            return;
        if (!((IScaffoldSprint) sprint.getSelected()).onPreSchedulePlace())
            return;

        double original = startPos;
        if (sprint.getInput() == 3) {
            if (groundDistance() >= 2 && add == 0) {
                original++;
                add++;
            }
        } else if (sprint.getInput() == 4 || sprint.getInput() == 5) {
            if (groundDistance() > 0 && mc.thePlayer.posY >= Math.floor(mc.thePlayer.posY) && mc.thePlayer.fallDistance > 0 && ((!placedUp || isDiagonal()) || sprint.getInput() == 4)) {
                original++;
            }
        }

        Vec3 targetVec3 = getPlacePossibility(0, original);
        if (targetVec3 == null) {
            return;
        }
        BlockPos targetPos = new BlockPos(targetVec3.xCoord, targetVec3.yCoord, targetVec3.zCoord);

        if (mc.thePlayer.onGround && Utils.isMoving() && motion.getInput() != 1.0 && !moveFix.isToggled()) {
            MoveUtil.strafe(MoveUtil.speed() * motion.getInput());
        }

        rayCasted = null;
        float searchYaw = 25;
        switch ((int) precision.getInput()) {
            case 0:
                searchYaw = 35;
                break;
            case 1:
                searchYaw = 30;
                break;
            case 2:
                break;
            case 3:
                searchYaw = 15;
                break;
            case 4:
                searchYaw = 5;
                break;
            case 5:
                searchYaw = 360;
                break;
        }

        EnumFacingOffset enumFacing = getEnumFacing(targetVec3);
        if (enumFacing == null) {
            return;
        }
        targetPos = targetPos.add(enumFacing.getOffset().xCoord, enumFacing.getOffset().yCoord, enumFacing.getOffset().zCoord);
        float[] targetRotation = RotationUtils.getRotations(targetPos);
        float[] searchPitch = new float[]{78, 12};

        for (int i = 0; i < 2; i++) {
            if (i == 1 && Utils.overPlaceable(-1)) {
                searchYaw = 180;
                searchPitch = new float[]{65, 25};
            } else if (i == 1) {
                if (expand.isToggled() && !(tower.isToggled() && Utils.jumpDown()) && (!polar.isToggled() || polar$waitingForExpand)) {
                    final keystrokesmod.script.classes.Vec3 eyePos = Utils.getEyePos();
                    final BlockPos groundPos = new BlockPos(mc.thePlayer).down();
                    long expDist = Math.round(expandDistance.getInput());
                    for (int j = 0; j < expDist; j++) {
                        targetPos = RotationUtils.getExtendedPos(groundPos, mc.thePlayer.rotationYaw, j);

                        if (sameY.isToggled() || hoverState != HoverState.DONE) {
                            targetPos = new BlockPos(targetPos.getX(), startPos, targetPos.getZ());
                        }

                        if (!BlockUtils.replaceable(targetPos))
                            continue;

                        Optional<Triple<BlockPos, EnumFacing, keystrokesmod.script.classes.Vec3>> optional = RotationUtils.getPlaceSide(targetPos);
                        if (!optional.isPresent()) continue;

                        Triple<BlockPos, EnumFacing, keystrokesmod.script.classes.Vec3> placeSide = optional.get();

                        if (placeSide.getRight().distanceTo(eyePos) > expandDistance.getInput()) break;

                        rayCasted = new MovingObjectPosition(placeSide.getRight().toVec3(), placeSide.getMiddle(), placeSide.getLeft());
                        placeYaw = PlayerRotation.getYaw(placeSide.getRight());
                        placePitch = PlayerRotation.getPitch(placeSide.getRight());
                        break;
                    }
                    if (polar.isToggled() && rayCasted == null) {
                        polar$waitingForExpand = false;
                    }
                }
                break;
            }
            for (float checkYaw : generateSearchSequence(searchYaw)) {
                float playerYaw = isDiagonal() ? getYaw() : targetRotation[0];
                float fixedYaw = (float) (playerYaw - checkYaw + getRandom());
                double deltaYaw = Math.abs(playerYaw - fixedYaw);
                if (i == 1 && (inBetween(75, 95, (float) deltaYaw)) || deltaYaw > 500) {
                    continue;
                }
                for (float checkPitch : generateSearchSequence(searchPitch[1])) {
                    float fixedPitch = RotationUtils.clampTo90((float) (targetRotation[1] + checkPitch + getRandom()));
                    MovingObjectPosition raycast = RotationUtils.rayTraceCustom(mc.playerController.getBlockReachDistance(), fixedYaw, fixedPitch);
                    if (raycast != null) {
                        if (raycast.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                            if (raycast.getBlockPos().equals(targetPos) && raycast.sideHit == enumFacing.getEnumFacing()) {
                                if (rayCasted == null || !BlockUtils.isSamePos(raycast.getBlockPos(), rayCasted.getBlockPos())) {
                                    if (heldItem.getItem() instanceof ItemBlock && ((ItemBlock) heldItem.getItem()).canPlaceBlockOnSide(mc.theWorld, raycast.getBlockPos(), raycast.sideHit, mc.thePlayer, heldItem)) {
                                        if (rayCasted == null) {
                                            forceStrict = (forceStrict(checkYaw)) && i == 1;
                                            if (recycleRotation.isToggled()) {
                                                Optional<Triple<BlockPos, EnumFacing, keystrokesmod.script.classes.Vec3>> placeSide = RotationUtils.getPlaceSide(raycast.getBlockPos());
                                                if (placeSide.isPresent()) {
                                                    rayCasted = new MovingObjectPosition(placeSide.get().getRight().toVec3(), placeSide.get().getMiddle(), placeSide.get().getLeft());
                                                    placeYaw = PlayerRotation.getYaw(placeSide.get().getRight());
                                                    placePitch = PlayerRotation.getPitch(placeSide.get().getRight());
                                                    break;
                                                }
                                            }
                                            rayCasted = raycast;
                                            placeYaw = fixedYaw;
                                            placePitch = fixedPitch;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (rayCasted != null) {
                break;
            }
        }
        if (place) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);

            if (rayCasted == null && alwaysPlaceIfPossible.isToggled()) {
                MovingObjectPosition hitResult = RotationUtils.rayCast(4.5, RotationHandler.getRotationYaw(), RotationHandler.getRotationPitch());
                if (hitResult == null) return;
                placeBlock = hitResult;
            } else {
                if (rayCasted == null) return;
                placeBlock = rayCasted;
            }
            if (multiPlace.isToggled()) {
                place(placeBlock, true);
            }
            place(placeBlock, false);
            place = false;
            if (placeBlock.sideHit == EnumFacing.UP && keepYPosition()) {
                placedUp = true;
            }
        }
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent ev) {
        if (!Utils.nullCheck() || !showBlockCount.isToggled()) {
            return;
        }
        if (ev.phase == TickEvent.Phase.END) {
            if (mc.currentScreen != null) {
                return;
            }
            final ScaledResolution scaledResolution = new ScaledResolution(mc);
            int blocks = totalBlocks();
            String color = "§";
            if (blocks <= 5) {
                color += "c";
            }
            else if (blocks <= 15) {
                color += "6";
            }
            else if (blocks <= 25) {
                color += "e";
            }
            else {
                color = "";
            }
            mc.fontRendererObj.drawStringWithShadow(color + blocks + " §rblock" + (blocks == 1 ? "" : "s"), (float) scaledResolution.getScaledWidth() /2 + 8, (float) scaledResolution.getScaledHeight() /2 + 4, -1);
        }
    }

    public Vec3 getPlacePossibility(double offsetY, double original) { // rise
        List<Vec3> possibilities = new ArrayList<>();
        int range = 5;
        for (int x = -range; x <= range; ++x) {
            for (int y = -range; y <= range; ++y) {
                for (int z = -range; z <= range; ++z) {
                    final Block block = BlockUtils.blockRelativeToPlayer(x, y, z);
                    if (!block.getMaterial().isReplaceable()) {
                        for (int x2 = -1; x2 <= 1; x2 += 2) {
                            possibilities.add(new Vec3(mc.thePlayer.posX + x + x2, mc.thePlayer.posY + y, mc.thePlayer.posZ + z));
                        }
                        for (int y2 = -1; y2 <= 1; y2 += 2) {
                            possibilities.add(new Vec3(mc.thePlayer.posX + x, mc.thePlayer.posY + y + y2, mc.thePlayer.posZ + z));
                        }
                        for (int z2 = -1; z2 <= 1; z2 += 2) {
                            possibilities.add(new Vec3(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z + z2));
                        }
                    }
                }
            }
        }

        possibilities.removeIf(vec3 -> mc.thePlayer.getDistance(vec3.xCoord, vec3.yCoord, vec3.zCoord) > 5);

        if (possibilities.isEmpty()) {
            return null;
        }
        possibilities.sort(Comparator.comparingDouble(vec3 -> {
            final double d0 = (mc.thePlayer.posX) - vec3.xCoord;
            final double d1 = ((keepYPosition() ? original : mc.thePlayer.posY) - 1 + offsetY) - vec3.yCoord;
            final double d2 = (mc.thePlayer.posZ) - vec3.zCoord;
            return MathHelper.sqrt_double(d0 * d0 + d1 * d1 + d2 * d2);
        }));

        return possibilities.get(0);
    }

    public float[] generateSearchSequence(float value) {
        int length = (int) value * 2;
        float[] sequence = new float[length + 1];

        int index = 0;
        sequence[index++] = 0;

        for (int i = 1; i <= value; i++) {
            sequence[index++] = i;
            sequence[index++] = -i;
        }

        return sequence;
    }

    @SubscribeEvent
    public void onMouse(@NotNull MouseEvent mouseEvent) {
        if (mouseEvent.button == 1) {
            rmbDown = mouseEvent.buttonstate;
            if (placeBlock != null && rmbDown) {
                mouseEvent.setCanceled(true);
            }
        }
    }

    public boolean stopFastPlace() {
        return this.isEnabled() && placeBlock != null;
    }

    public static boolean isDiagonal() {
        float yaw = ((mc.thePlayer.rotationYaw % 360) + 360) % 360 > 180 ? ((mc.thePlayer.rotationYaw % 360) + 360) % 360 - 360 : ((mc.thePlayer.rotationYaw % 360) + 360) % 360;
        return (yaw >= -170 && yaw <= 170) && !(yaw >= -10 && yaw <= 10) && !(yaw >= 80 && yaw <= 100) && !(yaw >= -100 && yaw <= -80) || Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode()) || Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode());
    }

    public double groundDistance() {
        for (int i = 1; i <= 20; i++) {
            if (!mc.thePlayer.onGround && !(BlockUtils.getBlock(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - ((double) i / 10), mc.thePlayer.posZ)) instanceof BlockAir)) {
                return ((double) i / 10);
            }
        }
        return -1;
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent e) {
        if (!Utils.nullCheck() || !highlightBlocks.isToggled() || highlight.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<BlockPos, Timer>> iterator = highlight.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<BlockPos, Timer> entry = iterator.next();
            if (entry.getValue() == null) {
                entry.setValue(new Timer(750));
                entry.getValue().start();
            }
            int alpha = entry.getValue() == null ? 210 : 210 - entry.getValue().getValueInt(0, 210, 1);
            if (alpha == 0) {
                iterator.remove();
                continue;
            }
            RenderUtils.renderBlock(entry.getKey(), Utils.merge(Theme.getGradient((int) HUD.theme.getInput(), 0), alpha), true, false);
        }
    }

    public static boolean sprint() {
        if (ModuleManager.scaffold.isEnabled()
                && ModuleManager.scaffold.sprint.getInput() > 0
                && (!ModuleManager.scaffold.fastOnRMB.isToggled() || Mouse.isButtonDown(1))) {
            return ((IScaffoldSprint) ModuleManager.scaffold.sprint.getSelected()).isSprint();
        }
        return false;
    }

    private boolean forceStrict(float value) {
        return (inBetween(-170, -105, value) || inBetween(-80, 80, value) || inBetween(98, 170, value)) && !inBetween(-10, 10, value);
    }

    public boolean keepYPosition() {
        boolean sameYSca = sprint.getInput() == 4 || sprint.getInput() == 3 || sprint.getInput() == 5;
        return this.isEnabled() && Utils.keysDown() && (sameYSca || (sameY.isToggled() && !Utils.jumpDown()) || hoverState != HoverState.DONE) && !Utils.jumpDown() && (!fastOnRMB.isToggled() || Mouse.isButtonDown(1));
    }

    public boolean safewalk() {
        return this.isEnabled() && safeWalk.isToggled();
    }

    public boolean stopRotation() {
        return this.isEnabled() && (rotation.getInput() <= 1 || (rotation.getInput() == 2 && placeBlock != null));
    }

    private boolean inBetween(float min, float max, float value) {
        return value >= min && value <= max;
    }

    private double getRandom() {
        return Utils.randomizeInt(-90, 90) / 100.0;
    }

    public float getYaw() {
        float yaw = 180.0f;
        double moveForward = mc.thePlayer.movementInput.moveForward;
        double moveStrafe = mc.thePlayer.movementInput.moveStrafe;
        if (rotateWithMovement.isToggled()) {
            if (moveForward > 0.0) {
                if (moveStrafe > 0.0) {
                    yaw = 135.0f;
                } else if (moveStrafe < 0.0) {
                    yaw = -135.0f;
                }
            } else if (moveForward < 0.0) {
                if (moveStrafe > 0.0) {
                    yaw = 45.0f;
                } else if (moveStrafe < 0.0) {
                    yaw = -45.0f;
                } else {
                    yaw = 0.0f;
                }
            } else {
                if (moveStrafe > 0.0) {
                    yaw = 90.0f;
                }
                else if (moveStrafe < 0.0) {
                    yaw = -90.0f;
                }
            }
        }

        return mc.thePlayer.rotationYaw + yaw;
    }

    private @Nullable EnumFacingOffset getEnumFacing(final Vec3 position) {
        for (int x2 = -1; x2 <= 1; x2 += 2) {
            if (!BlockUtils.getBlock(position.xCoord + x2, position.yCoord, position.zCoord).getMaterial().isReplaceable()) {
                if (x2 > 0) {
                    return new EnumFacingOffset(EnumFacing.WEST, new Vec3(x2, 0, 0));
                } else {
                    return new EnumFacingOffset(EnumFacing.EAST, new Vec3(x2, 0, 0));
                }
            }
        }

        for (int y2 = -1; y2 <= 1; y2 += 2) {
            if (!BlockUtils.getBlock(position.xCoord, position.yCoord + y2, position.zCoord).getMaterial().isReplaceable()) {
                if (y2 < 0) {
                    return new EnumFacingOffset(EnumFacing.UP, new Vec3(0, y2, 0));
                }
            }
        }

        for (int z2 = -1; z2 <= 1; z2 += 2) {
            if (!BlockUtils.getBlock(position.xCoord, position.yCoord, position.zCoord + z2).getMaterial().isReplaceable()) {
                if (z2 < 0) {
                    return new EnumFacingOffset(EnumFacing.SOUTH, new Vec3(0, 0, z2));
                } else {
                    return new EnumFacingOffset(EnumFacing.NORTH, new Vec3(0, 0, z2));
                }
            }
        }

        return null;
    }

    public void place(MovingObjectPosition block, boolean extra) {
        if (rotation.getInput() == 4 && telly$noBlockPlace) return;

        if (sneak.isToggled()) {
            if (sneak$bridged >= sneakEveryBlocks.getInput()) {
                sneak$bridged = 0;
                ((KeyBindingAccessor) mc.gameSettings.keyBindSneak).setPressed(true);
                Raven.getExecutor().schedule(() -> ((KeyBindingAccessor) mc.gameSettings.keyBindSneak).setPressed(Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())), (long) sneakTime.getInput(), TimeUnit.MILLISECONDS);
            }
        }

        if (jump.isToggled()) {
            if (jump$bridged >= jumpEveryBlocks.getInput()) {
                jump$bridged = 0;
                if (mc.thePlayer.onGround)
                    mc.thePlayer.jump();
            }
        }

        ItemStack heldItem = SlotHandler.getHeldItem();
        if (heldItem == null || !(heldItem.getItem() instanceof ItemBlock)) {
            return;
        }

        if (legit.isToggled()) {
            return;
        }

        ScaffoldPlaceEvent event = new ScaffoldPlaceEvent(block, extra);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return;

        block = event.getHitResult();
        extra = event.isExtra();

        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, heldItem, block.getBlockPos(), block.sideHit, block.hitVec)) {
            sneak$bridged++;
            jump$bridged++;
            if (silentSwing.isToggled()) {
                if (!noSwing.isToggled())
                    PacketUtils.sendPacket(new C0APacketAnimation());
            } else {
                mc.thePlayer.swingItem();
            }
            if (!extra) {
                highlight.put(block.getBlockPos().offset(block.sideHit), null);
            }
        }
    }

    public static int getSlot() {
        int slot = -1;
        int highestStack = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack itemStack = mc.thePlayer.inventory.mainInventory[i];
            if (itemStack != null && itemStack.getItem() instanceof ItemBlock && ContainerUtils.canBePlaced((ItemBlock) itemStack.getItem()) && itemStack.stackSize > 0) {
                if (mc.thePlayer.inventory.mainInventory[i].stackSize > highestStack) {
                    highestStack = mc.thePlayer.inventory.mainInventory[i].stackSize;
                    slot = i;
                }
            }
        }
        return slot;
    }

    public int totalBlocks() {
        if (!Utils.nullCheck()) return 0;

        try {
            int totalBlocks = 0;
            for (int i = 0; i < 9; ++i) {
                final ItemStack stack = mc.thePlayer.inventory.mainInventory[i];
                if (stack != null && stack.getItem() instanceof ItemBlock && ContainerUtils.canBePlaced((ItemBlock) stack.getItem()) && stack.stackSize > 0) {
                    totalBlocks += stack.stackSize;
                }
            }
            return totalBlocks;
        } catch (Throwable e) {
            return 0;
        }
    }

    static class EnumFacingOffset {
        EnumFacing enumFacing;
        Vec3 offset;

        EnumFacingOffset(EnumFacing enumFacing, Vec3 offset) {
            this.enumFacing = enumFacing;
            this.offset = offset;
        }

        EnumFacing getEnumFacing() {
            return enumFacing;
        }

        Vec3 getOffset() {
            return offset;
        }
    }

    @Override
    public String getInfo() {
        return sprint.getSelected().getPrettyName();
    }

    @SubscribeEvent
    public void onSafeWalk(@NotNull SafeWalkEvent event) {
        if (safewalk())
            event.setSafeWalk(true);
    }

    enum HoverState {
        JUMP,
        FALL,
        DONE
    }
}