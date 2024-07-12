package keystrokesmod.module.impl.movement;

import keystrokesmod.event.*;
import keystrokesmod.mixins.impl.client.KeyBindingAccessor;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.impl.world.Scaffold;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.utility.*;
import keystrokesmod.utility.render.RenderUtils;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.RandomUtils;
import org.jetbrains.annotations.NotNull;

public class Fly extends Module {
    private final ModeSetting mode;
    public static SliderSetting horizontalSpeed;
    private final SliderSetting verticalSpeed;
    private final SliderSetting maxBalance;
    private final ButtonSetting autoDisable;
    private final ButtonSetting showBPS;
    private final ButtonSetting stopMotion;
    private boolean d;

    private int offGroundTicks = 0;
    private boolean started, notUnder, clipped, teleport;


    private long balance = 0;
    private long startTime = -1;
    private Timer.BalanceState balanceState = Timer.BalanceState.NONE;
    private long lastReport = -1;

    public Fly() {
        super("Fly", category.movement);
        this.registerSetting(mode = new ModeSetting("Fly", new String[]{"Vanilla", "Fast", "Fast 2", "AirWalk", "Old GrimAC", "BlocksMC", "GrimACBoat", "AirPlace"}, 0));
        final ModeOnly canChangeSpeed = new ModeOnly(mode, 0, 1, 2, 6);
        final ModeOnly balanceMode = new ModeOnly(mode, 4);
        this.registerSetting(horizontalSpeed = new SliderSetting("Horizontal speed", 2.0, 0.0, 9.0, 0.1, canChangeSpeed));
        this.registerSetting(verticalSpeed = new SliderSetting("Vertical speed", 2.0, 0.0, 9.0, 0.1, canChangeSpeed));
        this.registerSetting(maxBalance = new SliderSetting("Max balance", 6000, 3000, 30000, 1000, "ms", balanceMode));
        this.registerSetting(autoDisable = new ButtonSetting("Auto disable", true, balanceMode));
        this.registerSetting(showBPS = new ButtonSetting("Show BPS", false));
        this.registerSetting(stopMotion = new ButtonSetting("Stop motion", false));
    }

    @Override
    public String getInfo() {
        return mode.getOptions()[(int) mode.getInput()];
    }

    public void onEnable() {
        this.d = mc.thePlayer.capabilities.isFlying;

        notUnder = false;
        started = false;
        clipped = false;
        teleport = false;

        if ((int) mode.getInput() == 5) {
            Utils.sendMessage("Start the fly under the block and walk forward.");
        }
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            if (teleport) {
                event.setCanceled(true);
                teleport = false;
                Utils.sendMessage("Teleported!");
            }
        }
    }

    @SubscribeEvent
    public void onRotation(RotationEvent event) {
        if (mode.getInput() == 7) {
            event.setPitch(90);
        }
    }

    @SubscribeEvent
    public void onPlayerInput(PrePlayerInputEvent event) {
        if ((int) mode.getInput() != 5) return;

        final AxisAlignedBB bb = mc.thePlayer.getEntityBoundingBox().offset(0, 1, 0);

        if (mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, bb).isEmpty() || started) {
            switch (offGroundTicks) {
                case 0:
                    if (notUnder) {
                        if (clipped) {
                            started = true;
                            event.setSpeed(10);
                            mc.thePlayer.motionY = 0.42f;
                            notUnder = false;
                        }
                    }
                    break;

                case 1:
                    if (started) event.setSpeed(9.6);
                    break;

                default:
//                    if (mc.thePlayer.fallDistance > 0 && started) {
//                        mc.thePlayer.motionY += 2.5 / 100f;
//                    }
                    break;
            }
        } else {
            notUnder = true;

            if (clipped) return;

            clipped = true;

            PacketUtils.sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false));
            PacketUtils.sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY - 0.1, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false));
            PacketUtils.sendPacket(new C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false));

            teleport = true;
        }

        MoveUtil.strafe();

        Utils.getTimer().timerSpeed = 0.4f;
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer.onGround) {
            offGroundTicks = 0;
        } else {
            offGroundTicks++;
        }

        switch ((int) mode.getInput()) {
            case 0:
                mc.thePlayer.motionY = 0.0;
                mc.thePlayer.capabilities.setFlySpeed((float)(0.05000000074505806 * horizontalSpeed.getInput()));
                mc.thePlayer.capabilities.isFlying = true;
                break;
            case 1:
                mc.thePlayer.onGround = true;
                if (mc.currentScreen == null) {
                    if (Utils.jumpDown()) {
                        mc.thePlayer.motionY = 0.3 * verticalSpeed.getInput();
                    }
                    else if (Utils.jumpDown()) {
                        mc.thePlayer.motionY = -0.3 * verticalSpeed.getInput();
                    }
                    else {
                        mc.thePlayer.motionY = 0.0;
                    }
                }
                else {
                    mc.thePlayer.motionY = 0.0;
                }
                mc.thePlayer.capabilities.setFlySpeed(0.2f);
                mc.thePlayer.capabilities.isFlying = true;
                setSpeed(0.85 * horizontalSpeed.getInput());
                break;
            case 2:
                double nextDouble = RandomUtils.nextDouble(1.0E-7, 1.2E-7);
                if (mc.thePlayer.ticksExisted % 2 == 0) {
                    nextDouble = -nextDouble;
                }
                if (!mc.thePlayer.onGround) {
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + nextDouble, mc.thePlayer.posZ);
                }
                mc.thePlayer.motionY = 0.0;
                setSpeed(0.4 * horizontalSpeed.getInput());
                break;
            case 6:
                /*
                 * @see ac.grim.grimac.predictionengine.UncertaintyHandler#hasHardCollision
                 * SUPER⭐GrimAC⭐TIME
                 */
                AxisAlignedBB playerBox = mc.thePlayer.getEntityBoundingBox();
                AxisAlignedBB grimACBox = playerBox.expand(1, 1, 1);
                for (Entity entity : mc.theWorld.loadedEntityList) {
                    if (entity instanceof EntityBoat) {
                        AxisAlignedBB boatBox = entity.getEntityBoundingBox();
                        if (boatBox.intersectsWith(grimACBox) && !(boatBox.intersectsWith(playerBox))) {  // if grimAC disabled simulation
                            // normal fly code
                            if (Utils.jumpDown()) {
                                mc.thePlayer.motionY = 0.5 * verticalSpeed.getInput();
                            } else if (mc.thePlayer.isSneaking()) {
                                mc.thePlayer.motionY = -0.5 * verticalSpeed.getInput();
                            } else {
                                mc.thePlayer.motionY = 0.0;
                            }
                            if (MoveUtil.isMoving())
                                MoveUtil.strafe(horizontalSpeed.getInput());
                            else
                                MoveUtil.stop();
                        }
                    }
                }
                break;
            case 7:
                SlotHandler.setCurrentSlot(Scaffold.getSlot());

                if (mc.thePlayer.onGround) {
                    if (!Utils.jumpDown()) mc.thePlayer.jump();
                } else if (mc.thePlayer.motionY < 0) {
                    if (!Utils.jumpDown() && mc.thePlayer.motionY > -0.25) {
                        return;
                    }

                    BlockPos pos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ).down();
                    if (BlockUtils.replaceable(pos)) {
                        mc.playerController.onPlayerRightClick(
                                mc.thePlayer, mc.theWorld, SlotHandler.getHeldItem(),
                                pos, EnumFacing.UP, new Vec3(mc.thePlayer.posX, pos.getY(), mc.thePlayer.posZ)
                        );
                        mc.thePlayer.swingItem();
                    }
                }
                break;
        }
    }

    public void onDisable() {
        balance$reset();
        if (!Utils.nullCheck()) return;

        if (mc.thePlayer.capabilities.allowFlying) {
            mc.thePlayer.capabilities.isFlying = this.d;
        }
        else {
            mc.thePlayer.capabilities.isFlying = false;
        }
        this.d = false;
        switch ((int) mode.getInput()) {
            case 0:
            case 1: {
                mc.thePlayer.capabilities.setFlySpeed(0.05F);
                break;
            }
            case 2: {
                break;
            }
        }
        if (stopMotion.isToggled()) {
            mc.thePlayer.motionZ = 0;
            mc.thePlayer.motionY = 0;
            mc.thePlayer.motionX = 0;
        }
        if ((int) mode.getInput() == 5) {
            MoveUtil.stop();
        }
    }

    private void balance$reset() {
        Utils.resetTimer();
        balance = 0;
        balanceState = Timer.BalanceState.NONE;
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent e) {
        RenderUtils.renderBPS(showBPS.isToggled(), e);

        if ((int) mode.getInput() == 4) {
            final long curTime = System.currentTimeMillis();

            if (System.currentTimeMillis() - lastReport > 1000) {
                lastReport = System.currentTimeMillis();
                Utils.sendMessage(balance + ".0");
            }
            switch (balanceState) {
                case NONE:
                    startTime = curTime;
                    Utils.getTimer().timerSpeed = 0;
                    balanceState = Timer.BalanceState.SLOW;
                    lastReport = System.currentTimeMillis();
                    break;
                case SLOW:
                    balance += curTime - startTime;
                    if (balance >= maxBalance.getInput()) {
                        balance = (long) maxBalance.getInput();
                        balanceState = Timer.BalanceState.TIMER;
                        startTime = curTime;
                    } else {
                        startTime = curTime;
                        Utils.getTimer().timerSpeed = 0;
                    }
                    break;
                case TIMER:
                    balance -= (curTime - startTime) * 10;
                    if (balance <= 0) {
                        balance$reset();
                        if (autoDisable.isToggled())
                            disable();
                        break;
                    }
                    startTime = curTime;
                    Utils.getTimer().timerSpeed = 10;
                    break;
            }
        }
    }

    @SubscribeEvent
    public void onBlockAABB(BlockAABBEvent event) {
        if ((int) mode.getInput() == 3 || ((int) mode.getInput() == 4 && balanceState == Timer.BalanceState.TIMER)) {
            // Sets The Bounding Box To The Players Y Position.
            if (event.getBlock() instanceof BlockAir) {
                final double x = event.getBlockPos().getX(), y = event.getBlockPos().getY(), z = event.getBlockPos().getZ();

                if (y < mc.thePlayer.posY) {
                    event.setBoundingBox(AxisAlignedBB.fromBounds(-15, -1, -15, 15, 1, 15).offset(x, y, z));
                }
            }
        }
    }

    public static void setSpeed(final double n) {
        if (n == 0.0) {
            mc.thePlayer.motionZ = 0;
            mc.thePlayer.motionX = 0;
            return;
        }
        double n3 = mc.thePlayer.movementInput.moveForward;
        double n4 = mc.thePlayer.movementInput.moveStrafe;
        float rotationYaw = mc.thePlayer.rotationYaw;
        if (n3 == 0.0 && n4 == 0.0) {
            mc.thePlayer.motionZ = 0;
            mc.thePlayer.motionX = 0;
        }
        else {
            if (n3 != 0.0) {
                if (n4 > 0.0) {
                    rotationYaw += ((n3 > 0.0) ? -45 : 45);
                }
                else if (n4 < 0.0) {
                    rotationYaw += ((n3 > 0.0) ? 45 : -45);
                }
                n4 = 0.0;
                if (n3 > 0.0) {
                    n3 = 1.0;
                }
                else if (n3 < 0.0) {
                    n3 = -1.0;
                }
            }
            final double radians = Math.toRadians(rotationYaw + 90.0f);
            final double sin = Math.sin(radians);
            final double cos = Math.cos(radians);
            mc.thePlayer.motionX = n3 * n * cos + n4 * n * sin;
            mc.thePlayer.motionZ = n3 * n * sin - n4 * n * cos;
        }
    }
}
