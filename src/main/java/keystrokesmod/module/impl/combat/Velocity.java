package keystrokesmod.module.impl.combat;

import keystrokesmod.Raven;
import keystrokesmod.event.*;
import keystrokesmod.mixins.impl.entity.EntityPlayerSPAccessor;
import keystrokesmod.mixins.impl.network.S12PacketEntityVelocityAccessor;
import keystrokesmod.mixins.impl.network.S27PacketExplosionAccessor;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.movement.LongJump;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.utility.*;
import net.minecraft.block.BlockAir;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import static keystrokesmod.utility.Utils.isLobby;

public class Velocity extends Module {
    private static final String[] MODES = new String[]{"Normal", "Hypixel", "Intave", "GrimAC", "Karhu", "Tick", "7-Zip", "Matrix"};
    public static ModeSetting mode;
    public static SliderSetting horizontal;
    public static SliderSetting vertical;
    public static SliderSetting chance;
    private final SliderSetting intave$xzOnHit;
    private final SliderSetting intave$xzOnSprintHit;
    private final SliderSetting grimAC$reduce;
    private final SliderSetting tick$delay;
    private final SliderSetting zip$delay;
    private final ButtonSetting cancelExplosion;
    private final ButtonSetting cancelAir;
    private final ButtonSetting damageBoost;
    private final SliderSetting boostMultiplier;
    private final SliderSetting boostDelay;
    private final ButtonSetting groundCheck;
    private final ButtonSetting lobbyCheck;
    private final ButtonSetting onlyFirstHit;
    private final SliderSetting resetTime;
    private final ButtonSetting debug;

    private boolean attacked = false;
    public static boolean slowDown = false;
    private EntityLivingBase lastAttack = null;
    private long lastVelocityTime = -1;
    private boolean gotVelocity = false;
    private long startDelayTime = -1;
    private final Queue<Packet<INetHandlerPlayClient>> delayedPacket = new ConcurrentLinkedQueue<>();

    public Velocity() {
        super("Velocity", category.combat);
        this.registerSetting(new DescriptionSetting("Reduce knock-back."));
        this.registerSetting(mode = new ModeSetting("Mode", MODES, 1));
        final ModeOnly canChangeMode = new ModeOnly(mode, 0, 1, 5);
        this.registerSetting(horizontal = new SliderSetting("Horizontal", 0.0, -100.0, 100.0, 1.0, canChangeMode));
        this.registerSetting(vertical = new SliderSetting("Vertical", 0.0, 0.0, 100.0, 1.0, canChangeMode));
        this.registerSetting(chance = new SliderSetting("Chance", 100, 0, 100, 1, "%", canChangeMode));
        this.registerSetting(intave$xzOnHit = new SliderSetting("XZ on hit", 0.6, 0, 1, 0.01, new ModeOnly(mode, 2)));
        this.registerSetting(intave$xzOnSprintHit = new SliderSetting("XZ on sprint hit", 0.6, 0, 1, 0.01, new ModeOnly(mode, 2)));
        this.registerSetting(grimAC$reduce = new SliderSetting("Reduce", 5, 0, 5, 1, new ModeOnly(mode, 3)));
        this.registerSetting(tick$delay = new SliderSetting("Delay", 50, 10, 400, 10, "ms", new ModeOnly(mode, 5)));
        this.registerSetting(zip$delay = new SliderSetting("Delay", 1000, 500, 10000, 250, "ms", new ModeOnly(mode, 6)));
        this.registerSetting(cancelExplosion = new ButtonSetting("Cancel explosion packet", true, canChangeMode));
        this.registerSetting(cancelAir = new ButtonSetting("Cancel air", false, canChangeMode));
        this.registerSetting(damageBoost = new ButtonSetting("Damage boost", false));
        this.registerSetting(boostMultiplier = new SliderSetting("Boost multiplier", 1.2, 0.5, 2.5, 0.1, damageBoost::isToggled));
        this.registerSetting(boostDelay = new SliderSetting("Boost delay", 0, 0, 1000, 5, "ms", damageBoost::isToggled));
        this.registerSetting(groundCheck = new ButtonSetting("Ground check", false, damageBoost::isToggled));
        this.registerSetting(lobbyCheck = new ButtonSetting("Lobby check", false));
        ModeOnly onlyFirstHitModes = new ModeOnly(mode, 0, 1, 5, 6);
        this.registerSetting(onlyFirstHit = new ButtonSetting("Only first hit", false, onlyFirstHitModes));
        this.registerSetting(resetTime = new SliderSetting("Reset time", 5000, 500, 10000, 500, "ms", onlyFirstHitModes.extend(onlyFirstHit::isToggled)));
        this.registerSetting(debug = new ButtonSetting("Debug", false, new ModeOnly(mode, 2, 3, 6, 7)));
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        if (lobbyCheck.isToggled() && isLobby()) {
            return;
        }

        try {
            if (!delayedPacket.isEmpty() && System.currentTimeMillis() - startDelayTime > zip$delay.getInput()) {
                releasePackets();
            }

            switch ((int) mode.getInput()) {
                case 2:
                    if (attacked && !slowDown && mc.thePlayer.hurtTime > 0) {
                        final double motionX = mc.thePlayer.motionX;
                        final double motionZ = mc.thePlayer.motionZ;

                        if (mc.thePlayer.isSprinting()) {
                            mc.thePlayer.motionX = motionX * intave$xzOnSprintHit.getInput();
                            mc.thePlayer.motionZ = motionZ * intave$xzOnSprintHit.getInput();
                            mc.thePlayer.setSprinting(false);
                        } else {
                            mc.thePlayer.motionX = motionX * intave$xzOnHit.getInput();
                            mc.thePlayer.motionZ = motionZ * intave$xzOnHit.getInput();
                        }
                        if (debug.isToggled()) Utils.sendMessage(String.format("reduced %.2f %.2f", motionX - mc.thePlayer.motionX, motionZ - mc.thePlayer.motionZ));
                    }
                    break;
                case 7:
                    if (attacked && !slowDown && mc.thePlayer.hurtTime > 0) {
                        final double motionX = mc.thePlayer.motionX;
                        final double motionZ = mc.thePlayer.motionZ;

                        if (mc.thePlayer.isSprinting()) {
                            if (Math.abs(motionX) < 0.625 && Math.abs(motionZ) < 0.625) {
                                mc.thePlayer.motionX = motionX * 0.4;
                                mc.thePlayer.motionZ = motionZ * 0.4;
                            } else if (Math.abs(motionX) < 1.25 && Math.abs(motionZ) < 1.25) {
                                mc.thePlayer.motionX = motionX * 0.67;
                                mc.thePlayer.motionZ = motionZ * 0.67;
                            }
                            mc.thePlayer.setSprinting(false);
                        }
                        if (debug.isToggled()) Utils.sendMessage(String.format("reduced %.2f %.2f", motionX - mc.thePlayer.motionX, motionZ - mc.thePlayer.motionZ));
                    }
                    break;
            }
            attacked = false;
            slowDown = false;
        } catch (NullPointerException e) {
            Utils.sendMessage(e.getLocalizedMessage());
        }
    }

    @SubscribeEvent
    public void onRotation(PreMotionEvent event) {
        if (lobbyCheck.isToggled() && isLobby()) {
            return;
        }

        if (mode.getInput() == 3) {
            if (gotVelocity && lastAttack != null
                    && !KillAura.behindBlocks(new float[]{RotationHandler.getRotationYaw(), RotationHandler.getRotationPitch()}, lastAttack)) {
                final double motionX = mc.thePlayer.motionX;
                final double motionZ = mc.thePlayer.motionZ;
                if (((EntityPlayerSPAccessor) mc.thePlayer).isServerSprint() && MoveUtil.isMoving()) {
                    grimAC$reduce();
                } else {
                    mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                    grimAC$reduce();
                    mc.getNetHandler().addToSendQueue(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
                }
                if (debug.isToggled()) Utils.sendMessage(String.format("reduced %.2f %.2f", motionX - mc.thePlayer.motionX, motionZ - mc.thePlayer.motionZ));
            }
            gotVelocity = false;
        }
    }

    private void grimAC$reduce() {
        for (int i = 0; i < (int) grimAC$reduce.getInput(); i++) {
            PacketUtils.sendPacketNoEvent(new C0APacketAnimation());
            PacketUtils.sendPacketNoEvent(new C02PacketUseEntity(lastAttack, C02PacketUseEntity.Action.ATTACK));
            mc.thePlayer.motionX *= 0.6;
            mc.thePlayer.motionZ *= 0.6;
        }
    }

    @SubscribeEvent
    public void onAttack(@NotNull AttackEntityEvent event) {
        if (event.target instanceof EntityLivingBase) {
            attacked = true;
            lastAttack = (EntityLivingBase) event.target;
        }
    }

    private boolean overAir() {
        if (mc.thePlayer.onGround) return false;

        final AxisAlignedBB boundingBox = BlockUtils.getCollisionBoundingBox(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ));
        if (boundingBox == null) return true;

        return boundingBox.maxY != mc.thePlayer.posY;
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent e) {
        if (!Utils.nullCheck() || LongJump.stopModules || e.isCanceled()) {
            return;
        }
        if (chance.getInput() != 100 && Math.random() * 100 > chance.getInput()) {
            return;
        }
        final long time = System.currentTimeMillis();
        if (e.getPacket() instanceof S32PacketConfirmTransaction && mode.getInput() == 6) {
            if (time - startDelayTime <= zip$delay.getInput()) {
                e.setCanceled(true);
                delayedPacket.add((S32PacketConfirmTransaction) e.getPacket());
            }
        } else if (e.getPacket() instanceof S12PacketEntityVelocity) {
            if (((S12PacketEntityVelocity) e.getPacket()).getEntityID() == mc.thePlayer.getEntityId()) {
                if (onlyFirstHit.isToggled() && time - lastVelocityTime < resetTime.getInput()) {
                    return;
                }
                lastVelocityTime = time;
                gotVelocity = true;
                if (lobbyCheck.isToggled() && isLobby()) {
                    return;
                }
                switch ((int) mode.getInput()) {
                    case 0:
                        if (cancel()) {
                            ((S12PacketEntityVelocityAccessor) e.getPacket()).setMotionX(0);
                            ((S12PacketEntityVelocityAccessor) e.getPacket()).setMotionY(0);
                            ((S12PacketEntityVelocityAccessor) e.getPacket()).setMotionZ(0);
                            return;
                        }
                        ((S12PacketEntityVelocityAccessor) e.getPacket()).setMotionX((int) (((S12PacketEntityVelocity) e.getPacket()).getMotionX() * horizontal.getInput() / 100));
                        ((S12PacketEntityVelocityAccessor) e.getPacket()).setMotionY((int) (((S12PacketEntityVelocity) e.getPacket()).getMotionY() * vertical.getInput() / 100));
                        ((S12PacketEntityVelocityAccessor) e.getPacket()).setMotionZ((int) (((S12PacketEntityVelocity) e.getPacket()).getMotionZ() * horizontal.getInput() / 100));
                        break;
                    case 1:
                        e.setCanceled(true);
                        if (cancel()) {
                            return;
                        }
                        S12PacketEntityVelocity s12PacketEntityVelocity = (S12PacketEntityVelocity) e.getPacket();
                        if (horizontal.getInput() == 0 && vertical.getInput() > 0) {
                            mc.thePlayer.motionY = ((double) s12PacketEntityVelocity.getMotionY() / 8000) * vertical.getInput()/100;
                        }
                        else if (horizontal.getInput() > 0 && vertical.getInput() == 0) {
                            mc.thePlayer.motionX = ((double) s12PacketEntityVelocity.getMotionX() / 8000) * horizontal.getInput()/100;
                            mc.thePlayer.motionZ = ((double) s12PacketEntityVelocity.getMotionZ() / 8000) * horizontal.getInput()/100;
                        }
                        else {
                            mc.thePlayer.motionX = ((double) s12PacketEntityVelocity.getMotionX() / 8000) * horizontal.getInput()/100;
                            mc.thePlayer.motionY = ((double) s12PacketEntityVelocity.getMotionY() / 8000) * vertical.getInput()/100;
                            mc.thePlayer.motionZ = ((double) s12PacketEntityVelocity.getMotionZ() / 8000) * horizontal.getInput()/100;
                        }
                        e.setCanceled(true);
                        break;
                    case 5:
                        Raven.getExecutor().schedule(() -> {
                            if (mc.thePlayer.hurtTime > 0) {
                                mc.thePlayer.motionX *= horizontal.getInput() / 100;
                                mc.thePlayer.motionY *= vertical.getInput() / 100;
                                mc.thePlayer.motionZ *= horizontal.getInput() / 100;
                            }
                        }, (long) tick$delay.getInput(), TimeUnit.MILLISECONDS);
                        break;
                    case 6:
                        e.setCanceled(true);
                        if (startDelayTime == -1) {
                            startDelayTime = time;
                        }
                        if (time - startDelayTime <= zip$delay.getInput()) {
                            e.setCanceled(true);
                            delayedPacket.add((S12PacketEntityVelocity) e.getPacket());
                        }
                        break;
                }
                if (damageBoost.isToggled()) {
                    if (boostDelay.getInput() == 0) {
                        boost();
                    } else {
                        Raven.getExecutor().schedule(this::boost, (long) boostDelay.getInput(), TimeUnit.MILLISECONDS);
                    }
                }
            }
        } else if (e.getPacket() instanceof S27PacketExplosion) {
            if (onlyFirstHit.isToggled() && time - lastVelocityTime < resetTime.getInput()) {
                return;
            }
            lastVelocityTime = time;
            if (lobbyCheck.isToggled() && isLobby()) {
                return;
            }
            if (cancelExplosion.isToggled() || cancel()) {
                e.setCanceled(true);
                return;
            }
            switch ((int) mode.getInput()) {
                case 0:
                    ((S27PacketExplosionAccessor) e.getPacket()).setMotionX((float) (((S27PacketExplosion) e.getPacket()).func_149149_c() * horizontal.getInput() / 100));
                    ((S27PacketExplosionAccessor) e.getPacket()).setMotionY((float) (((S27PacketExplosion) e.getPacket()).func_149144_d() * vertical.getInput()) / 100);
                    ((S27PacketExplosionAccessor) e.getPacket()).setMotionZ((float) (((S27PacketExplosion) e.getPacket()).func_149147_e() * horizontal.getInput() / 100));
                    break;
                case 1:
                    e.setCanceled(true);
                    S27PacketExplosion s27PacketExplosion = (S27PacketExplosion) e.getPacket();
                    if (horizontal.getInput() == 0 && vertical.getInput() > 0) {
                        mc.thePlayer.motionY += s27PacketExplosion.func_149144_d() * vertical.getInput()/100;
                    }
                    else if (horizontal.getInput() > 0 && vertical.getInput() == 0) {
                        mc.thePlayer.motionX += s27PacketExplosion.func_149149_c() * horizontal.getInput()/100;
                        mc.thePlayer.motionZ += s27PacketExplosion.func_149147_e() * horizontal.getInput()/100;
                    }
                    else {
                        mc.thePlayer.motionX += s27PacketExplosion.func_149149_c() * horizontal.getInput()/100;
                        mc.thePlayer.motionY += s27PacketExplosion.func_149144_d() * vertical.getInput()/100;
                        mc.thePlayer.motionZ += s27PacketExplosion.func_149147_e() * horizontal.getInput()/100;
                    }
                    e.setCanceled(true);
                    break;
            }
        }
    }

    @SubscribeEvent
    public void onBlockAABBE(BlockAABBEvent event) {
        if (mode.getInput() != 4) return;

        if (event.getBlock() instanceof BlockAir && mc.thePlayer.hurtTime > 0) {
            final double x = event.getBlockPos().getX(), y = event.getBlockPos().getY(), z = event.getBlockPos().getZ();

            if (y == Math.floor(mc.thePlayer.posY) + 1) {
                event.setBoundingBox(AxisAlignedBB.fromBounds(0, 0, 0, 1, 0, 1).offset(x, y, z));
            }
        }
    }

    private void boost() {
        if (groundCheck.isToggled() && !mc.thePlayer.onGround) {
            return;
        }
        MoveUtil.strafe(MoveUtil.speed() * boostMultiplier.getInput()); // from croat
    }

    private boolean cancel() {
        return (vertical.getInput() == 0 && horizontal.getInput() == 0) || ModuleManager.bedAura.cancelKnockback() || (cancelAir.isToggled() && overAir());
    }

    @Override
    public String getInfo() {
        return MODES[(int) mode.getInput()];
    }

    @Override
    public void onDisable() {
        releasePackets();
    }

    private void releasePackets() {
        if (!delayedPacket.isEmpty()) {
            for (Packet<INetHandlerPlayClient> packet : delayedPacket) {
                PacketUtils.receivePacketNoEvent(packet);
            }
            delayedPacket.clear();

            if (debug.isToggled()) {
                Utils.sendMessage(String.format("Released. %.2f  %.2f  %.2f", mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ));
            }
        }
        startDelayTime = -1;
    }
}
