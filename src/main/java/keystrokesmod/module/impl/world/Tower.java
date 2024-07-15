package keystrokesmod.module.impl.world;

import keystrokesmod.Raven;
import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.other.anticheats.utils.world.BlockUtils;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.utility.Reflection;
import keystrokesmod.utility.Utils;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.input.Keyboard;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static keystrokesmod.module.ModuleManager.scaffold;

public class Tower extends Module {
    private final ModeSetting mode;
    private final SliderSetting speed;
    private final SliderSetting diagonalSpeed;
    private final SliderSetting slowedSpeed;
    private final SliderSetting slowedTicks;
    private final ButtonSetting disableWhileCollided;
    private final ButtonSetting disableWhileHurt;
    private final ButtonSetting sprintJumpForward;
    private final ButtonSetting hypixelNoStrafe;
    private final SliderSetting hypixelOffGroundSpeed;
    private final ButtonSetting lowHop;
    private final ButtonSetting lowHop2; // New setting for lowhop2 mode
    private int slowTicks;
    private boolean wasTowering;
    private int offGroundTicks = 0;
    private BlockPos toweredBlock = null;

    // Additional fields from lowhop
    private int onGroundTicks;
    private boolean watchdog;

    // Additional fields from lowhop2
    private int inAirTicks;

    public Tower() {
        super("Tower", category.world);
        this.registerSetting(new DescriptionSetting("Works with SafeWalk & Scaffold"));
        String[] modes = new String[]{"Vanilla", "Hypixel", "BlocksMC"};
        this.registerSetting(mode = new ModeSetting("Mode", modes, 0));
        final ModeOnly mode0 = new ModeOnly(mode, 0);
        final ModeOnly mode1 = new ModeOnly(mode, 1);
        this.registerSetting(speed = new SliderSetting("Speed", 0.95, 0.5, 1, 0.01));
        this.registerSetting(diagonalSpeed = new SliderSetting("Diagonal speed", 5, 0, 10, 0.1, mode0));
        this.registerSetting(slowedSpeed = new SliderSetting("Slowed speed", 2, 0, 9, 0.1, mode0));
        this.registerSetting(slowedTicks = new SliderSetting("Slowed ticks", 1, 0, 20, 1, mode0));
        this.registerSetting(hypixelOffGroundSpeed = new SliderSetting("Hypixel off ground speed", 0.5, 0.0, 1.0, 0.01, mode1));
        this.registerSetting(hypixelNoStrafe = new ButtonSetting("Hypixel no strafe", false, mode1));
        this.registerSetting(lowHop = new ButtonSetting("Low hop", false, mode1));
        this.registerSetting(lowHop2 = new ButtonSetting("Low hop 2", false, mode1)); // Register lowhop2 setting
        this.registerSetting(disableWhileCollided = new ButtonSetting("Disable while collided", false));
        this.registerSetting(disableWhileHurt = new ButtonSetting("Disable while hurt", false));
        this.registerSetting(sprintJumpForward = new ButtonSetting("Sprint jump forward", true));
        this.canBeEnabled = false;

        // Initialize additional fields from lowhop and lowhop2
        onGroundTicks = 0;
        watchdog = false;
        inAirTicks = 0;
    }

    @Override
    public void onDisable() {
        wasTowering = false;
        offGroundTicks = 0;
        toweredBlock = null;
        // Reset additional fields
        onGroundTicks = 0;
        watchdog = false;
        inAirTicks = 0;
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent e) throws IllegalAccessException {
        if (canTower()) {
            wasTowering = true;

            switch ((int) mode.getInput()) {
                case 0:
                    Utils.setSpeed(Math.max((diagonal() ? diagonalSpeed.getInput() : speed.getInput()) * 0.1 - 0.25, 0));
                    mc.thePlayer.jump();
                    break;
                case 1:
                    Reflection.jumpTicks.set(mc.thePlayer, 0);
                    e.setSprinting(false);

                    toweredBlock = null;
                    double moveSpeed = e.isOnGround() ? speed.getInput() : hypixelOffGroundSpeed.getInput();
                    if (hypixelNoStrafe.isToggled()) {
                        if (Math.abs(mc.thePlayer.motionX) >= Math.abs(mc.thePlayer.motionZ)) {
                            mc.thePlayer.motionX *= moveSpeed;
                            mc.thePlayer.motionZ = 0;
                        } else {
                            mc.thePlayer.motionZ *= moveSpeed;
                            mc.thePlayer.motionX = 0;
                        }
                    } else {
                        mc.thePlayer.motionX *= moveSpeed;
                        mc.thePlayer.motionZ *= moveSpeed;
                    }
                    break;
                case 2:
                    if (mc.thePlayer.onGround)
                        mc.thePlayer.motionY = 0.42F;
                    mc.thePlayer.motionX *= speed.getInput();
                    mc.thePlayer.motionZ *= speed.getInput();
                    break;
            }
        } else if (mode.getInput() == 0) {
            if (wasTowering && slowedTicks.getInput() > 0 && modulesEnabled()) {
                if (slowTicks++ < slowedTicks.getInput()) {
                    Utils.setSpeed(Math.max(slowedSpeed.getInput() * 0.1 - 0.25, 0));
                } else {
                    slowTicks = 0;
                    wasTowering = false;
                }
            } else {
                if (wasTowering) {
                    wasTowering = false;
                }
                slowTicks = 0;
            }
            reset();
        }

        // Implement low hop logic from provided code
        if (lowHop.isToggled()) {
            Entity player = client.getPlayer();
            onGroundTicks = player.onGround() ? onGroundTicks + 1 : 0;
            watchdog = (watchdog || onGroundTicks == 1) && onGroundTicks < 2;
            Vec3 motion = client.getMotion();
            if (onGroundTicks > 0) state.y += 1E-14;
            if (watchdog) {
                if (motion.y == 0.16477328182606651) client.setMotion(motion.x, 0.14961479459521598, motion.z);
                if (motion.y == 0.0682225000311085) client.setMotion(motion.x, 0.0532225003663811, motion.z);
                if (motion.y == -0.0262419501516868) client.setMotion(motion.x, -0.027141950136226, motion.z);
                if (motion.y == -0.104999113177072) client.setMotion(motion.x, -0.31999911675335113, motion.z);
                if (motion.y == -0.3919991420476618) client.setMotion(motion.x, -0.3968991421057737, motion.z);
            }
        }

        // Implement low hop 2 logic from provided code
        if (lowHop2.isToggled()) {
            if (modules.isEnabled("Scaffold") && client.keybinds.isKeyDown(57)) {
                Entity player = client.getPlayer();
                inAirTicks = player.onGround() ? 0 : inAirTicks + 1;
                Vec3 motion = client.getMotion();
                if (player.onGround()) client.setMotion(motion.x, 0.4191, motion.z);
                if (inAirTicks == 1) client.setMotion(motion.x, 0.327318, motion.z);
                if (inAirTicks == 5) client.setMotion(motion.x, -0.005, motion.z);
                if (inAirTicks == 6) client.setMotion(motion.x, -1.0, motion.z);
            }
        }
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent event) {
        if (canTower() && (int) mode.getInput() == 2) {
            if (mc.thePlayer.motionY > -0.0784000015258789 && event.getPacket() instanceof C08PacketPlayerBlockPlacement) {
                final C08PacketPlayerBlockPlacement wrapper = ((C08PacketPlayerBlockPlacement) event.getPacket());

                if (wrapper.getPosition().equals(new BlockPos(mc.thePlayer.posX, mc.thePlayer
