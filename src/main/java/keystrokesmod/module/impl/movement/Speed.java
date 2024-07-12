package keystrokesmod.module.impl.movement;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.PrePlayerInputEvent;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.utility.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import static keystrokesmod.module.ModuleManager.scaffold;

public class Speed extends Module {
    private final ModeSetting mode;
    private final SliderSetting vulcan$lowHop;
    private final SliderSetting grimAC$boost;
    private final ButtonSetting autoJump;
    private final ButtonSetting liquidDisable;
    private final ButtonSetting sneakDisable;
    private final ButtonSetting stopMotion;
    private final ButtonSetting stopSprint;
    private final String[] modes = new String[]{"Hypixel", "BlocksMC", "Vulcan", "GrimAC"};
    private int offGroundTicks = 0;
    public static int ticksSinceVelocity = Integer.MAX_VALUE;

    boolean strafe, cooldown = false;
    int cooldownTicks = 0;

    double lastAngle = 0;

    int groundYPos = -1;

    private boolean reset;
    private double speed;

    public Speed() {
        super("Speed", Module.category.movement);
        this.registerSetting(mode = new ModeSetting("Mode", modes, 0));
        this.registerSetting(vulcan$lowHop = new SliderSetting("Low hop", 2, 0, 4, 1, "ticks", new ModeOnly(mode, 2)));
        ModeOnly grimAC = new ModeOnly(mode, 3);
        this.registerSetting(grimAC$boost = new SliderSetting("Boost", 4, 0, 10, 1, grimAC));
        this.registerSetting(autoJump = new ButtonSetting("Auto jump", false, grimAC));
        this.registerSetting(liquidDisable = new ButtonSetting("Disable in liquid", true));
        this.registerSetting(sneakDisable = new ButtonSetting("Disable while sneaking", true));
        this.registerSetting(stopMotion = new ButtonSetting("Stop motion", false));
        this.registerSetting(stopSprint = new ButtonSetting("Stop sprint", false));
    }

    @Override
    public String getInfo() {
        return modes[(int) mode.getInput()];
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (stopSprint.isToggled()) {
            event.setSprinting(false);
        }

        if (noAction()) return;
        if ((int) mode.getInput() == 6) {
            if (!MoveUtil.isMoving()) {
                event.setPosX(event.getPosX() + (Math.random() - 0.5) / 100);
                event.setPosZ(event.getPosZ() + (Math.random() - 0.5) / 100);
            }

            PacketUtils.sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
        }
    }

    @Override
    public void onEnable() {
        ticksSinceVelocity = Integer.MAX_VALUE;
    }

    public void onUpdate() {
        if (ticksSinceVelocity < Integer.MAX_VALUE) ticksSinceVelocity++;
        
        if (mc.thePlayer.onGround) {
            offGroundTicks = 0;
        } else {
            offGroundTicks++;
        }

        if (noAction()) return;
        switch ((int) mode.getInput()) {
            case 0:
                if (!Utils.jumpDown() && Utils.isMoving() && mc.currentScreen == null) {
                    mc.thePlayer.setSprinting(true);
                    if (mc.thePlayer.onGround) {
                        MoveUtil.strafe(MoveUtil.getAllowedHorizontalDistance() - Math.random() / 100f);
                        mc.thePlayer.jump();

                        double angle = Math.atan(mc.thePlayer.motionX / mc.thePlayer.motionZ) * (180 / Math.PI);

                        if (Math.abs(lastAngle - angle) > 20 && ticksSinceVelocity > 20) {
                            int speed = mc.thePlayer.isPotionActive(Potion.moveSpeed) ? mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1 : 0;

                            switch (speed) {
                                case 0:
                                    MoveUtil.moveFlying(-0.005);
                                    break;

                                case 1:
                                    MoveUtil.moveFlying(-0.035);
                                    break;

                                default:
                                    MoveUtil.moveFlying(-0.04);
                                    break;
                            }
                        }
                    }
                }
                break;
            case 3:
                if (!Utils.nullCheck() || !MoveUtil.isMoving()) break;
                if (mc.thePlayer.onGround && autoJump.isToggled()) {
                    mc.thePlayer.jump();
                }

                int collisions = 0;
                AxisAlignedBB grimPlayerBox = mc.thePlayer.getEntityBoundingBox().expand(1.0, 1.0, 1.0);
                for (Entity entity : mc.theWorld.loadedEntityList) {
                    if (canCauseSpeed(entity) && (grimPlayerBox.intersectsWith(entity.getEntityBoundingBox()))) {
                        collisions += 1;
                    }
                }
                double yaw = Math.toRadians(MoveYaw());
                double boost = grimAC$boost.getInput() / 100 * collisions;
                mc.thePlayer.addVelocity(-Math.sin(yaw) * boost, 0.0, Math.cos(yaw) * boost);
                break;
        }
    }

    public static double MoveYaw(){
        return  (MoveUtil.direction() * 180f / Math.PI);
    }

    private boolean canCauseSpeed(Entity entity) {
        return entity != mc.thePlayer && entity instanceof EntityLivingBase;
    }

    private boolean noAction() {
        return ((mc.thePlayer.isInWater()
                || mc.thePlayer.isInLava()) && liquidDisable.isToggled())
                || (mc.thePlayer.isSneaking() && sneakDisable.isToggled())
                || scaffold.isEnabled();
    }

    @SubscribeEvent
    public void onPlayerInput(PrePlayerInputEvent event) {
        if (noAction()) return;

        switch ((int) mode.getInput()) {
            case 1:
                final double base = MoveUtil.getAllowedHorizontalDistance();

                if (!Utils.jumpDown() && Utils.isMoving() && mc.currentScreen == null) {
                    switch (offGroundTicks) {
                        case 0:
                            mc.thePlayer.motionY = MoveUtil.jumpBoostMotion(0.42f);
                            speed = base * 2.15;
                            break;

                        case 1:
                            speed -= 0.8 * (speed - base);
                            break;

                        default:
                            speed -= speed / MoveUtil.BUNNY_FRICTION;
                            break;
                    }

                    reset = false;
                } else if (!reset) {
                    speed = 0;

                    reset = true;
                    speed = MoveUtil.getAllowedHorizontalDistance();
                }

                if (mc.thePlayer.isCollidedHorizontally || BlockUtils.getSurroundBlocks(mc.thePlayer).stream()
                        .map(BlockUtils::getBlockState)
                        .map(IBlockState::getBlock)
                        .allMatch(Block::isFullCube)) {
                    speed = MoveUtil.getAllowedHorizontalDistance();
                }

                event.setSpeed(Math.max(speed, base), Math.random() / 2000);
                break;
            case 2:
                if (!MoveUtil.isMoving()) break;
                switch (offGroundTicks) {
                    case 0:
                        mc.thePlayer.jump();

                        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                            MoveUtil.strafe(0.6);
                        } else {
                            MoveUtil.strafe(0.485);
                        }
                        break;

                    case 9:
                        if (!(blockRelativeToPlayer(0, mc.thePlayer.motionY,
                                0) instanceof BlockAir)) {
                            MoveUtil.strafe();
                        }
                        break;

                    case 2:
                    case 1:
                        MoveUtil.strafe();
                        break;

                    case 5:
                        mc.thePlayer.motionY = MoveUtil.predictedMotion(mc.thePlayer.motionY, (int) vulcan$lowHop.getInput());
                        break;
                }
                break;
        }
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            speed = 0;
        }
    }

    public Block blockRelativeToPlayer(final double offsetX, final double offsetY, final double offsetZ) {
        return mc.theWorld.getBlockState(new BlockPos(mc.thePlayer).add(offsetX, offsetY, offsetZ)).getBlock();
    }

    private boolean isYAxisChange() {
        MovingObjectPosition hitResult = RotationUtils.rayCast(3, 0, 90);
        return hitResult == null || hitResult.getBlockPos().getY() != groundYPos;
    }

    public void onDisable() {
        if (stopMotion.isToggled()) {
            MoveUtil.stop();
        }
        cooldownTicks = 0;
        cooldown = false;
        strafe = false;
        speed = 0;
        Utils.resetTimer();
    }
}
