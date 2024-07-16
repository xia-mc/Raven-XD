package keystrokesmod.module.impl.world;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.utility.Reflection;
import keystrokesmod.utility.Utils;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

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
    private final ModeSetting lowHop;
    private int slowTicks;
    private boolean wasTowering;
    private int offGroundTicks = 0;
    private int onGroundTicks = 0;
    private boolean lowHopTest1$watchdog = false;

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
        this.registerSetting(lowHop = new ModeSetting("Low hop", new String[]{"None", "Default", "Test1", "Test2"}, 0));
        this.registerSetting(disableWhileCollided = new ButtonSetting("Disable while collided", false));
        this.registerSetting(disableWhileHurt = new ButtonSetting("Disable while hurt", false));
        this.registerSetting(sprintJumpForward = new ButtonSetting("Sprint jump forward", true));
        this.canBeEnabled = false;
    }

    @Override
    public void onDisable() {
        wasTowering = false;
        offGroundTicks = 0;
        onGroundTicks = 0;
        lowHopTest1$watchdog = false;
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

                    if (lowHop.getInput() == 2) {
                        onGroundTicks = mc.thePlayer.onGround ? onGroundTicks + 1 : 0;
                        lowHopTest1$watchdog = (lowHopTest1$watchdog || onGroundTicks == 1) && onGroundTicks < 2;
                        if (onGroundTicks > 0)
                            e.setPosY(e.getPosY() + 1E-14);

                        if (lowHopTest1$watchdog) {
                            if (mc.thePlayer.motionY == 0.16477328182606651) mc.thePlayer.motionY = 0.14961479459521598;
                            if (mc.thePlayer.motionY == 0.0682225000311085) mc.thePlayer.motionY = 0.0532225003663811;
                            if (mc.thePlayer.motionY == -0.0262419501516868) mc.thePlayer.motionY = -0.027141950136226;
                            if (mc.thePlayer.motionY == -0.104999113177072) mc.thePlayer.motionY = -0.31999911675335113;
                            if (mc.thePlayer.motionY == -0.3919991420476618) mc.thePlayer.motionY = -0.3968991421057737;
                        }
                    }

                    break;   // WTF?? why it works?? without this break???
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
                }
                else {
                    slowTicks = 0;
                    wasTowering = false;
                }
            }
            else {
                if (wasTowering) {
                    wasTowering = false;
                }
                slowTicks = 0;
            }
            reset();
        }
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent event) {
        if (canTower() && (int) mode.getInput() == 2) {
            if (mc.thePlayer.motionY > -0.0784000015258789 && event.getPacket() instanceof C08PacketPlayerBlockPlacement) {
                final C08PacketPlayerBlockPlacement wrapper = ((C08PacketPlayerBlockPlacement) event.getPacket());

                if (wrapper.getPosition().equals(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.4, mc.thePlayer.posZ))) {
                    mc.thePlayer.motionY = -0.0784000015258789;
                }
            }
        }
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer.onGround) {
            offGroundTicks = 0;
        } else {
            offGroundTicks++;
        }

        if (canTower() && mode.getInput() == 1 && Utils.isMoving()) {
            switch ((int) lowHop.getInput()) {
                default:
                case 0:
                    break;
                case 1:
                    switch (offGroundTicks) {
                        case 0:
                            mc.thePlayer.motionY = 0.4196;
                            break;
                        case 3:
                        case 4:
                            mc.thePlayer.motionY = 0;
                            break;
                        case 5:
                            mc.thePlayer.motionY = 0.4191;
                            break;
                        case 6:
                            mc.thePlayer.motionY = 0.3275;
                            break;
                        case 11:
                            mc.thePlayer.motionY = -0.5;
                            break;
                    }
                    break;
                case 3:
                    switch (offGroundTicks) {
                        case 0:
                            mc.thePlayer.motionY = 0.4191;
                            break;
                        case 1:
                            mc.thePlayer.motionY = 0.327318;
                            break;
                        case 4:
                            mc.thePlayer.motionY = 0.065;
                            break;
                        case 5:
                            mc.thePlayer.motionY = -0.005;
                            break;
                        case 6:
                            mc.thePlayer.motionY = -1.0;
                            break;
                    }
                    break;
            }
        }
    }

    private void reset() {
    }

    private boolean canTower() {
        if (scaffold.totalBlocks() == 0) return false;
        if (mc.currentScreen != null) return false;
        if (!Utils.nullCheck() || !Utils.jumpDown()) {
            return false;
        }
        else if (disableWhileHurt.isToggled() && mc.thePlayer.hurtTime >= 9) {
            return false;
        }
        else if (disableWhileCollided.isToggled() && mc.thePlayer.isCollidedHorizontally) {
            return false;
        }
        else return modulesEnabled();
    }

    private boolean modulesEnabled() {
        return  ((ModuleManager.safeWalk.isEnabled() && ModuleManager.safeWalk.tower.isToggled() && SafeWalk.canSafeWalk()) || (scaffold.isEnabled() && scaffold.tower.isToggled()));
    }

    public boolean canSprint() {
        return canTower() && this.sprintJumpForward.isToggled() && Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()) && Utils.jumpDown();
    }

    private boolean diagonal() {
        return (Math.abs(mc.thePlayer.motionX) > 0.05 && Math.abs(mc.thePlayer.motionZ) > 0.05);
    }
}
