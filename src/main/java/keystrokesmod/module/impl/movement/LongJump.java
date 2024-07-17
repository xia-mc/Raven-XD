package keystrokesmod.module.impl.movement;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.PrePlayerInputEvent;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.mixins.impl.client.KeyBindingAccessor;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Reflection;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.render.RenderUtils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S14PacketEntity;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;

import static keystrokesmod.Raven.mc;

public class LongJump extends Module {
    public static final String[] MODES = {"Fireball", "Fireball Auto", "Self Damage"};
    private final ModeSetting mode;
    private final SliderSetting horizonBoost;
    private final SliderSetting horizonMotionMultiplier;
    private final SliderSetting verticalMotion;
    private final ButtonSetting reverseYaw;
    private final SliderSetting pitch;
    private final SliderSetting aimTicks;
    private final ButtonSetting stopAtFirst;
    private final ButtonSetting jumpAtEnd;
    private final ButtonSetting showBPS;
    private final ButtonSetting stopOnTeleport;
    private int ticks = 0;
    private boolean start;
    private boolean done;
    public static boolean stopModules;
    private boolean waitForDamage = false;
    private int aimedTicks = Integer.MAX_VALUE;

    public LongJump() {
        super("Long Jump", category.movement);
        this.registerSetting(mode = new ModeSetting("Mode", MODES, 0));
        this.registerSetting(horizonBoost = new SliderSetting("Horizon boost", 1.0, 1.0, 1.5, 0.01));
        this.registerSetting(horizonMotionMultiplier = new SliderSetting("Horizon motion multiplier", 1.0, 0.9, 1.1, 0.005));
        this.registerSetting(verticalMotion = new SliderSetting("Vertical motion", 0.01, 0.01, 0.6, 0.01));
        this.registerSetting(stopAtFirst = new ButtonSetting("Stop at first", false));
        ModeOnly mode1 = new ModeOnly(mode, 1);
        this.registerSetting(reverseYaw = new ButtonSetting("Reverse yaw", false, mode1));
        this.registerSetting(pitch = new SliderSetting("Pitch", 90, 80, 90, 0.5, mode1));
        this.registerSetting(aimTicks = new SliderSetting("Aim ticks", 2, 1, 10, 1, mode1));
        this.registerSetting(jumpAtEnd = new ButtonSetting("Jump at end.", false, new ModeOnly(mode, 0, 1)));
        this.registerSetting(showBPS = new ButtonSetting("Show BPS", false));
        this.registerSetting(stopOnTeleport = new ButtonSetting("Stop on teleport", true));
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event) {
        RenderUtils.renderBPS(showBPS.isToggled(), event);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPreMotion(PreMotionEvent event) {
        switch ((int) mode.getInput()) {
            case 1:
                if (!waitForDamage) {
                    if (reverseYaw.isToggled()) {
                        event.setYaw(mc.thePlayer.rotationYaw + 180);
                    }
                    event.setPitch((float) pitch.getInput());
                    if (aimedTicks == Integer.MAX_VALUE){
                        aimedTicks = mc.thePlayer.ticksExisted;
                    }
                }
                break;
            case 2:
                if (ticks < 33) {
                    event.setOnGround(false);
                    mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;
                } else if (ticks == 50) {
                    event.setOnGround(true);
                }
                break;
        }

        if (((!start && !done) || ticks <= 0) && stopAtFirst.isToggled()) {
            mc.thePlayer.posX = mc.thePlayer.lastTickPosX;
            mc.thePlayer.posZ = mc.thePlayer.lastTickPosZ;
            mc.thePlayer.motionX = mc.thePlayer.motionZ = 0;
        }
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent event) throws IllegalAccessException {
        if (event.getPacket() instanceof S12PacketEntityVelocity && Utils.nullCheck()) {
            S12PacketEntityVelocity packet = (S12PacketEntityVelocity) event.getPacket();
            if (packet.getEntityID() != mc.thePlayer.getEntityId()) return;
            switch ((int) mode.getInput()) {
                case 0:
                case 1:
                    if (ticks == 0) {
                        Reflection.S12PacketEntityVelocityXMotion.set(packet, (int) Math.floor(packet.getMotionX() * horizonBoost.getInput()));
                        Reflection.S12PacketEntityVelocityZMotion.set(packet, (int) Math.floor(packet.getMotionZ() * horizonBoost.getInput()));
                        start = true;
                    }
                    break;
                case 2:
                    if (ticks == 50)
                        event.setCanceled(true);
                    break;
            }
        }
        try {
            if (event.getPacket() instanceof S14PacketEntity
                    && ((S14PacketEntity) event.getPacket()).getEntity(mc.theWorld).equals(mc.thePlayer)
                    && stopOnTeleport.isToggled()) {
                Utils.sendModuleMessage(this, "Teleport!");
                disable();
            }
        } catch (Exception e) {
            Utils.sendMessage(e.getLocalizedMessage());
        }
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        switch ((int) mode.getInput()) {
            case 1:
                if (!waitForDamage && mc.thePlayer.ticksExisted - aimedTicks >= aimTicks.getInput()) {
                    int shouldSlot = getFireball();
                    if (shouldSlot != mc.thePlayer.inventory.currentItem) {
                        mc.thePlayer.inventory.currentItem = shouldSlot;
                    } else {
                        mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem());
                        waitForDamage = true;
                    }
                }
            case 0:
                if (mc.thePlayer.hurtTime >= 3) {
                    start = true;
                }

                if (start) {
                    ticks++;
                }

                if (ticks > 0 && ticks < 30) {
                    mc.thePlayer.motionX *= horizonMotionMultiplier.getInput();
                    mc.thePlayer.motionZ *= horizonMotionMultiplier.getInput();
                    mc.thePlayer.motionY = verticalMotion.getInput();
                } else if (ticks >= 30) {
                    done = true;
                    start = false;
                }

                if (mc.thePlayer.hurtTime == 0 && done) {
                    if (jumpAtEnd.isToggled())
                        mc.thePlayer.motionY = 0.4;
                    disable();
                }
                break;
            case 2:
                mc.thePlayer.setSprinting(true);
                if (ticks < 34) {
                    if (mc.thePlayer.onGround) mc.thePlayer.jump();
                } else if (ticks > 34 && mc.thePlayer.onGround && !waitForDamage) {
                    ((KeyBindingAccessor) mc.gameSettings.keyBindForward).setPressed(true);
                    mc.thePlayer.moveForward = 1.0F;
                    MoveUtil.strafe(0.415);
                    mc.thePlayer.motionY = 0.42;
                    waitForDamage = true;
                } else if (waitForDamage && mc.thePlayer.hurtTime == 5) {
                    MoveUtil.strafe(horizonBoost.getInput());
                    mc.thePlayer.motionY = verticalMotion.getInput();
                    disable();
                }
                ticks++;
                break;
        }
    }

    @SubscribeEvent
    public void onPreInput(PrePlayerInputEvent event) {
        if ((int) mode.getInput() == 2) {
            if (ticks < 40) {
                event.setForward(0);
                event.setStrafe(0);
            }
        }
    }

    public void onDisable() {
        if (mode.getInput() == 2) {
            ((KeyBindingAccessor) mc.gameSettings.keyBindForward).setPressed(false);
        }

        start = false;
        done = false;
        waitForDamage = false;
        aimedTicks = Integer.MAX_VALUE;
        ticks = 0;
        stopModules = false;
    }

    public void onEnable() {
        if (getFireball() == -1 && mode.getInput() == 1) {
            Utils.sendMessage("§cNo fireball found.");
            this.disable();
            return;
        }
        stopModules = true;
        if (mode.getInput() == 0)
            waitForDamage = true;
    }

    private int getFireball() {
        int n = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack getStackInSlot = mc.thePlayer.inventory.getStackInSlot(i);
            if (getStackInSlot != null && getStackInSlot.getItem() == Items.fire_charge) {
                n = i;
                break;
            }
        }
        return n;
    }

    @Override
    public String getInfo() {
        return MODES[(int) mode.getInput()];
    }
}
