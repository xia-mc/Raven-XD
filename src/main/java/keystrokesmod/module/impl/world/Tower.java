package keystrokesmod.module.impl.world;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.other.anticheats.utils.world.BlockUtils;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.utility.Utils;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;

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
    private final ButtonSetting lowHop2; // New button setting for lowhop2
    private int slowTicks;
    private boolean wasTowering;
    private int offGroundTicks = 0;
    private BlockPos toweredBlock = null;
    private int onGroundTicks;
    private boolean watchdog;
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

        // Initialize additional fields
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
    public void onPreMotion(PreMotionEvent e) {
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
            Entity player = mc.thePlayer;
            onGroundTicks = player.onGround ? onGroundTicks + 1 : 0;
            watchdog = (watchdog || onGroundTicks == 1) && onGroundTicks < 2;
            Vec3 motion = mc.thePlayer.getMotion();
            if (onGroundTicks > 0) {
                mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + 1E-14, mc.thePlayer.posZ);
            }
            if (watchdog) {
                if (motion.y == 0.16477328182606651) {
                    mc.thePlayer.setMotion(motion.x, 0.14961479459521598, motion.z);
                }
                if (motion.y == 0.0682225000311085) {
                    mc.thePlayer.setMotion(motion.x, 0.0532225003663811, motion.z);
                }
                if (motion.y == -0.0262419501516868) {
                    mc.thePlayer.setMotion(motion.x, -0.027141950136226, motion.z);
                }
                if (motion.y == -0.104999113177072) {
                    mc.thePlayer.setMotion(motion.x, -0.31999911675335113, motion.z);
                }
                if (motion.y == -0.3919991420476618) {
                    mc.thePlayer.setMotion(motion.x, -0.3968991421057737, motion.z);
                }
            }
        }

        // Implement low hop 2 logic
        if (lowHop2.isToggled() && ModuleManager.scaffold.isEnabled() && Keyboard.isKeyDown(57)) {
            Entity player = mc.thePlayer;
            inAirTicks = player.onGround ? 0 : inAirTicks + 1;
            Vec3 motion = mc.thePlayer.getMotion();
            if (player.onGround) {
                mc.thePlayer.setMotion(motion.x, 0.4191, motion.z);
            } else {
                if (inAirTicks == 1) {
                    mc.thePlayer.setMotion(motion.x, 0.327318, motion.z);
                }
                if (inAirTicks == 5) {
                    mc.thePlayer.setMotion(motion.x, -0.005, motion.z);
                }
                if (inAirTicks == 6) {
                    mc.thePlayer.setMotion(motion.x, -1.0, motion.z);
                }
            }
        }
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent event) {
        if (canTower() && (int) mode.getInput() == 2) {
            if (mc.thePlayer.motionY > -0.0784000015258789 && event.getPacket() instanceof C08PacketPlayerBlockPlacement) {
                final C08PacketPlayerBlockPlacement wrapper = ((C08PacketPlayerBlockPlacement) event.getPacket());
                BlockPos pos = wrapper.getPosition();
                if (pos.equals(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.4, mc.thePlayer.posZ))) {
                    mc.thePlayer.motionY = -0.0784000015258789;
                }
            }
        }
    }

    private void reset() {
        // Add reset logic here if needed
    }

    private boolean canTower() {
    return ModuleManager.scaffold.isEnabled() && ModuleManager.scaffold.tower.isToggled() &&
            mc.currentScreen == null && !Utils.nullCheck() && Utils.jumpDown() &&
            !(disableWhileHurt.isToggled() && mc.thePlayer.hurtTime >= 9) &&
            !(disableWhileCollided.isToggled() && mc.thePlayer.isCollidedHorizontally) &&
            modulesEnabled();
    }
