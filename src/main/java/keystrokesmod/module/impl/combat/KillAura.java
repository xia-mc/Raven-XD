package keystrokesmod.module.impl.combat;

import akka.japi.Pair;
import keystrokesmod.Raven;
import keystrokesmod.event.*;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.combat.autoclicker.DragClickAutoClicker;
import keystrokesmod.module.impl.combat.autoclicker.IAutoClicker;
import keystrokesmod.module.impl.combat.autoclicker.NormalAutoClicker;
import keystrokesmod.module.impl.combat.autoclicker.RecordAutoClicker;
import keystrokesmod.module.impl.other.RecordClick;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.*;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.*;
import keystrokesmod.utility.aim.AimSimulator;
import keystrokesmod.utility.render.RenderUtils;
import lombok.Getter;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Mouse;

import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.minecraft.util.EnumFacing.DOWN;

public class KillAura extends IAutoClicker {
    public static EntityLivingBase target;
    private final ModeValue clickMode;
    public ModeSetting autoBlockMode;
    private final SliderSetting fov;
    private final ModeSetting attackMode;
    public final SliderSetting attackRange;
    private final SliderSetting swingRange;
    private final SliderSetting blockRange;
    private final SliderSetting preAimRange;

    private final ModeSetting rotationMode;
    private final ModeSetting moveFixMode;
    private final ModeSetting rayCastMode;
    private final ButtonSetting nearest;
    private final SliderSetting nearestAccuracy;
    private final ButtonSetting lazy;
    private final SliderSetting lazyAccuracy;
    private final ButtonSetting constant;
    private final ButtonSetting constantOnlyIfNotMoving;
    private final ButtonSetting noise;
    private final SliderSetting noiseHorizontal;
    private final SliderSetting noiseVertical;
    private final SliderSetting noiseAimSpeed;
    private final SliderSetting noiseDelay;
    private final ButtonSetting delayAim;
    private final SliderSetting delayAimAmount;
    private final SliderSetting rotationSpeed;

    private final ModeSetting sortMode;
    private final SliderSetting switchDelay;
    private final SliderSetting targets;
    private final ButtonSetting targetInvisible;
    private final ButtonSetting targetPlayer;
    private final ButtonSetting targetEntity;

    private final ButtonSetting disableInInventory;
    private final ButtonSetting disableWhileBlocking;
    private final ButtonSetting disableWhileMining;
    private final ButtonSetting fixSlotReset;
    private final ButtonSetting fixNoSlowFlag;
    private final SliderSetting postDelay;
    private final ButtonSetting hitThroughBlocks;
    private final ButtonSetting ignoreTeammates;
    public ButtonSetting manualBlock;
    private final ButtonSetting requireMouseDown;
    private final ButtonSetting silentSwing;
    private final ButtonSetting weaponOnly;

    private final ButtonSetting dot;
    private final SliderSetting dotSize;

    private final String[] rotationModes = new String[]{"None", "Silent", "Lock view"};
    private final List<EntityLivingBase> availableTargets = new ArrayList<>();
    public AtomicBoolean block = new AtomicBoolean();
    private long lastSwitched = System.currentTimeMillis();
    private boolean switchTargets;
    private byte entityIndex;
    private boolean swing;
    @Getter
    private boolean attack;
    private boolean blocking;
    public boolean blinking;
    public boolean lag;
    private boolean swapped;
    public boolean rmbDown;
    private float[] rotations = new float[]{0, 0};
    private final ConcurrentLinkedQueue<Packet<?>> blinkedPackets = new ConcurrentLinkedQueue<>();

    private int blockingTime = 0;
    private final AimSimulator aimSimulator = new AimSimulator();

    public KillAura() {
        super("KillAura", category.combat);
        this.registerSetting(clickMode = new ModeValue("Click mode", this)
                .add(new NormalAutoClicker("Normal", this, true, true))
                .add(new DragClickAutoClicker("Drag Click", this, true, true))
                .add(new RecordAutoClicker("Record", this, true, true))
                .setDefaultValue("Normal")
        );
        String[] autoBlockModes = new String[]{"Manual", "Vanilla", "Post", "Swap", "Interact A", "Interact B", "Fake", "Partial"};
        this.registerSetting(autoBlockMode = new ModeSetting("Autoblock", autoBlockModes, 0));
        this.registerSetting(attackMode = new ModeSetting("Attack mode", new String[]{"Legit", "Packet"}, 1));
        this.registerSetting(new DescriptionSetting("Range"));
        this.registerSetting(attackRange = new SliderSetting("Attack range", 3.0, 3.0, 6.0, 0.1));
        this.registerSetting(swingRange = new SliderSetting("Swing range", 3.0, 3.0, 8.0, 0.1));
        this.registerSetting(blockRange = new SliderSetting("Block range", 6.0, 3.0, 12.0, 0.1));
        this.registerSetting(preAimRange = new SliderSetting("PreAim range", 6.0, 3.0, 12.0, 0.1));
        this.registerSetting(fov = new SliderSetting("FOV", 360.0, 30.0, 360.0, 4.0));
        this.registerSetting(new DescriptionSetting("Rotation"));
        this.registerSetting(rotationMode = new ModeSetting("Rotation", rotationModes, 1));
        final ModeOnly doRotation = new ModeOnly(rotationMode, 1, 2);
        this.registerSetting(rotationSpeed = new SliderSetting("Rotation speed", 10, 0, 10, 0.05, doRotation));
        this.registerSetting(moveFixMode = new ModeSetting("Move fix", RotationHandler.MoveFix.MODES, 0, new ModeOnly(rotationMode, 1)));
        this.registerSetting(rayCastMode = new ModeSetting("Ray cast", new String[]{"None", "Normal", "Strict"}, 1, doRotation));
        this.registerSetting(nearest = new ButtonSetting("Nearest", false, doRotation));
        this.registerSetting(nearestAccuracy = new SliderSetting("Nearest accuracy", 1, 0.8, 1, 0.01, doRotation.extend(nearest::isToggled)));
        this.registerSetting(lazy = new ButtonSetting("Lazy", false, doRotation));
        this.registerSetting(lazyAccuracy = new SliderSetting("Lazy accuracy", 0.95, 0.6, 1, 0.01, doRotation.extend(lazy::isToggled)));
        this.registerSetting(constant = new ButtonSetting("Constant", false, doRotation));
        this.registerSetting(constantOnlyIfNotMoving = new ButtonSetting("Constant only if not moving", false, doRotation.extend(constant::isToggled)));
        this.registerSetting(noise = new ButtonSetting("Noise", false, doRotation));
        this.registerSetting(noiseHorizontal = new SliderSetting("Noise horizontal", 0.35, 0.01, 1, 0.01, doRotation.extend(noise::isToggled)));
        this.registerSetting(noiseVertical = new SliderSetting("Noise vertical", 0.5, 0.01, 1, 0.01, doRotation.extend(noise::isToggled)));
        this.registerSetting(noiseAimSpeed = new SliderSetting("Noise aim speed", 0.35, 0.01, 1, 0.01, doRotation.extend(noise::isToggled)));
        this.registerSetting(noiseDelay = new SliderSetting("Noise delay", 100, 50, 500, 10, doRotation.extend(noise::isToggled)));
        this.registerSetting(delayAim = new ButtonSetting("Delay aim", false, doRotation));
        this.registerSetting(delayAimAmount = new SliderSetting("Delay aim amount", 5, 5, 100, 1, doRotation.extend(delayAim::isToggled)));
        this.registerSetting(new DescriptionSetting("Targets"));
        String[] sortModes = new String[]{"Health", "HurtTime", "Distance", "Yaw"};
        this.registerSetting(sortMode = new ModeSetting("Sort mode", sortModes, 0));
        this.registerSetting(targets = new SliderSetting("Targets", 1.0, 1.0, 10.0, 1.0));
        this.registerSetting(switchDelay = new SliderSetting("Switch delay", 200.0, 50.0, 1000.0, 25.0, "ms", () -> targets.getInput() > 1));
        this.registerSetting(targetInvisible = new ButtonSetting("Target invisible", true));
        this.registerSetting(targetPlayer = new ButtonSetting("Target player", true));
        this.registerSetting(targetEntity = new ButtonSetting("Target entity", false));
        this.registerSetting(new DescriptionSetting("Miscellaneous"));
        this.registerSetting(disableInInventory = new ButtonSetting("Disable in inventory", true));
        this.registerSetting(disableWhileBlocking = new ButtonSetting("Disable while blocking", false));
        this.registerSetting(disableWhileMining = new ButtonSetting("Disable while mining", false));
        this.registerSetting(fixSlotReset = new ButtonSetting("Fix slot reset", false));
        this.registerSetting(fixNoSlowFlag = new ButtonSetting("Fix NoSlow flag", false));
        this.registerSetting(postDelay = new SliderSetting("Post delay", 10, 1, 20, 1, fixNoSlowFlag::isToggled));
        this.registerSetting(hitThroughBlocks = new ButtonSetting("Hit through blocks", true));
        this.registerSetting(ignoreTeammates = new ButtonSetting("Ignore teammates", true));
        this.registerSetting(manualBlock = new ButtonSetting("Manual block", false));
        this.registerSetting(requireMouseDown = new ButtonSetting("Require mouse down", false));
        this.registerSetting(silentSwing = new ButtonSetting("Silent swing while blocking", false));
        this.registerSetting(weaponOnly = new ButtonSetting("Weapon only", false));
        this.registerSetting(new DescriptionSetting("Visual"));
        this.registerSetting(dot = new ButtonSetting("Dot", false));
        this.registerSetting(dotSize = new SliderSetting("Dot size", 0.1, 0.05, 0.2, 0.05, dot::isToggled));
    }

    public void onEnable() {
        clickMode.enable();
        this.rotations = new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch};
    }

    @Override
    public void guiUpdate() {
        Utils.correctValue(attackRange, swingRange);
        Utils.correctValue(swingRange, preAimRange);
    }

    public void onDisable() {
        clickMode.disable();
        resetVariables();
        if (Utils.nullCheck()) mc.thePlayer.stopUsingItem();
    }

    private float[] getRotations() {
        aimSimulator.setNearest(nearest.isToggled(), nearestAccuracy.getInput());
        aimSimulator.setLazy(lazy.isToggled(), lazyAccuracy.getInput());
        aimSimulator.setNoise(noise.isToggled(),
                new Pair<>((float) noiseHorizontal.getInput(), (float) noiseVertical.getInput()),
                noiseAimSpeed.getInput(), (long) noiseDelay.getInput());
        aimSimulator.setDelay(delayAim.isToggled(), (int) delayAimAmount.getInput());

        if (constant.isToggled() && !noAimToEntity() && !(constantOnlyIfNotMoving.isToggled() && (MoveUtil.isMoving() || MoveUtil.isMoving(target))))
            return rotations;

        Pair<Float, Float> result = aimSimulator.getRotation(target);

        if (rotationSpeed.getInput() == 10)
            return new float[]{result.first(), result.second()};

        return new float[]{
                AimSimulator.rotMove(result.first(), rotations[0], (float) rotationSpeed.getInput()),
                AimSimulator.rotMove(result.second(), rotations[1], (float) rotationSpeed.getInput())
        };
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent ev) {
        if (!Utils.nullCheck()) {
            return;
        }
        if (ev.phase != TickEvent.Phase.START) {
            return;
        }
        if (target != null) {
            rotations = getRotations();
            if (rotationMode.getInput() == 2) {
                mc.thePlayer.rotationYaw = rotations[0];
                mc.thePlayer.rotationPitch = rotations[1];
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Vec3 hitPos = aimSimulator.getHitPos();
        if (target != null && rotations != null && dot.isToggled() && hitPos != null) {
            RenderUtils.drawDot(hitPos, dotSize.getInput(), 0xFF0670BE);
        }
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent e) {
        if (gameNoAction() || playerNoAction()) {
            resetVariables();
            return;
        }

        block();

        if (ModuleManager.bedAura != null && ModuleManager.bedAura.isEnabled() && !ModuleManager.bedAura.allowAura.isToggled() && ModuleManager.bedAura.currentBlock != null) {
            resetBlinkState(true);
            return;
        }
        if ((mc.thePlayer.isBlocking() || block.get()) && disableWhileBlocking.isToggled()) {
            resetBlinkState(true);
            return;
        }
        boolean swingWhileBlocking = !silentSwing.isToggled() || !block.get();
        if (swing && attack && HitSelect.canSwing()) {
            if (swingWhileBlocking) {
                mc.thePlayer.swingItem();
                RecordClick.click();
            } else {
                mc.thePlayer.sendQueue.addToSendQueue(new C0APacketAnimation());
                RecordClick.click();
            }
        }
        int input = (int) autoBlockMode.getInput();
        if (block.get() && (input == 3 || input == 4 || input == 5 || input == 9) && Utils.holdingSword()) {
            setBlockState(block.get(), false, false);
            if (ModuleManager.bedAura.stopAutoblock) {
                resetBlinkState(false);
                ModuleManager.bedAura.stopAutoblock = false;
                return;
            }
            switch (input) {
                case 3:
                    if (lag) {
                        blinking = true;
                        if (Raven.badPacketsHandler.playerSlot != mc.thePlayer.inventory.currentItem % 8 + 1) {
                            mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange( mc.thePlayer.inventory.currentItem % 8 + 1));
                            Raven.badPacketsHandler.playerSlot = mc.thePlayer.inventory.currentItem % 8 + 1;
                            swapped = true;
                        }
                        lag = false;
                    } else {
                        // check here for ghost later
                        mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                        Raven.badPacketsHandler.playerSlot = mc.thePlayer.inventory.currentItem;  // todo recode this with slot handler
                        swapped = false;
                        attackAndInteract(target, true);
                        sendBlock();
                        releasePackets();
                        lag = true;
                    }
                    break;
                case 4:
                case 5:
                    if (lag) {
                        blinking = true;
                        unBlock();
                        lag = false;
                    }
                    else {
                        attackAndInteract(target, autoBlockMode.getInput() == 5); // attack while blinked
                        releasePackets(); // release
                        sendBlock(); // block after releasing unblock
                        lag = true;
                    }
                    break;
                case 9:
                    if (lag) {
                        unBlock();
                        lag = false;
                    } else {
                        if (blocking) {
                            unBlock();
                        } else {
                            attackAndInteract(target, true);// attack while blinked
                            sendBlock();
                            lag = true;
                        }
                    }
                    break;
            }
            return;
        }
        else if (blinking || lag) {
            resetBlinkState(true);
        }
        if (target == null) {
            return;
        }
        if (attack) {
            resetBlinkState(true);
            attack = false;
            if (noAimToEntity()) {
                return;
            }
            switchTargets = true;
            Utils.attackEntity(target, swingWhileBlocking);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onPreMotion(RotationEvent e) {
        if (gameNoAction() || playerNoAction()) {
            return;
        }
        setTarget(new float[]{e.getYaw(), e.getPitch()});
        if (target != null && rotationMode.getInput() == 1) {
            e.setYaw(rotations[0]);
            e.setPitch(rotations[1]);
            e.setMoveFix(RotationHandler.MoveFix.values()[(int) moveFixMode.getInput()]);
        } else {
            this.rotations = new float[]{mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch};
        }
        if (autoBlockMode.getInput() == 2 && block.get() && Utils.holdingSword()) {
            mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1));
            mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
        }
    }

    @SubscribeEvent
    public void onPostMotion(PostMotionEvent e) {
        if (autoBlockMode.getInput() == 2 && block.get() && Utils.holdingSword()) {
            mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(SlotHandler.getHeldItem()));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onSendPacket(SendPacketEvent e) {
        if (!Utils.nullCheck() || !blinking) {
            return;
        }
        Packet<?> packet = e.getPacket();
        if (packet.getClass().getSimpleName().startsWith("S")) {
            return;
        }
        blinkedPackets.add(e.getPacket());
        e.setCanceled(true);
    }

    @SubscribeEvent
    public void onReceivePacket(ReceivePacketEvent e) {
        if (gameNoAction() || !fixSlotReset.isToggled()) {
            return;
        }
        if (Utils.holdingSword() && (mc.thePlayer.isBlocking() || block.get())) {
            if (e.getPacket() instanceof S2FPacketSetSlot) {
                if (mc.thePlayer.inventory.currentItem == ((S2FPacketSetSlot) e.getPacket()).func_149173_d() - 36 && mc.currentScreen == null) {
                    if (((S2FPacketSetSlot) e.getPacket()).func_149174_e() == null || (((S2FPacketSetSlot) e.getPacket()).func_149174_e().getItem() != mc.thePlayer.getHeldItem().getItem())) {
                        return;
                    }
                    e.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onMouse(final @NotNull MouseEvent mouseEvent) {
        if (mouseEvent.button == 0 && mouseEvent.buttonstate) {
            if (target != null || swing) {
                mouseEvent.setCanceled(true);
            }
        }
        else if (mouseEvent.button == 1) {
            rmbDown = mouseEvent.buttonstate;
            if (autoBlockMode.getInput() >= 1 && Utils.holdingSword() && block.get() && autoBlockMode.getInput() != 7) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
                if (target == null && mc.objectMouseOver != null) {
                    if (mc.objectMouseOver.entityHit != null && AntiBot.isBot(mc.objectMouseOver.entityHit)) {
                        return;
                    }
                    final BlockPos getBlockPos = mc.objectMouseOver.getBlockPos();
                    if (getBlockPos != null && (BlockUtils.check(getBlockPos, Blocks.chest) || BlockUtils.check(getBlockPos, Blocks.ender_chest))) {
                        return;
                    }
                }
                mouseEvent.setCanceled(true);
            }
        }
    }

    @Override
    public String getInfo() {
        return rotationModes[(int) rotationMode.getInput()];
    }

    public boolean noAimToEntity() {
        if (target == null) return true;
        if (rotationMode.getInput() == 0) return false;

        boolean noAim = false;
        switch ((int) rayCastMode.getInput()) {
            default:
            case 2:
                noAim = !RotationUtils.isMouseOver(RotationHandler.getRotationYaw(), RotationHandler.getRotationPitch(), target, (float) attackRange.getInput());
            case 1:
                if (noAim) break;
                Object[] rayCasted = Reach.getEntity(attackRange.getInput(), -0.05, rotationMode.getInput() == 1 ? rotations : null);
                noAim = rayCasted == null || rayCasted[0] != target;
                break;
            case 0:
                return false;
        }

        return noAim;
    }

    private void resetVariables() {
        target = null;
        availableTargets.clear();

        block.set(false);
        swing = false;
        rmbDown = false;
        attack = false;
        block();
        resetBlinkState(true);
        swapped = false;
        blockingTime = 0;
    }

    private void block() {
        if (!block.get() && !blocking) {
            return;
        }
        if (manualBlock.isToggled() && !rmbDown) {
            block.set(false);
        }
        if (!Utils.holdingSword()) {
            block.set(false);
        }
        switch ((int) autoBlockMode.getInput()) {
            case 0:  // manual
                setBlockState(false, false, true);
                break;
            case 1: // vanilla
                setBlockState(block.get(), true, true);
                break;
            case 2: // post
                setBlockState(block.get(), false, true);
                break;
            case 3: // interact
            case 4:
            case 5:
                setBlockState(block.get(), false, false);
                break;
            case 6: // fake
                setBlockState(block.get(), false, false);
                break;
            case 7: // partial
                boolean down = (target == null || target.hurtTime >= 5) && block.get();
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), down);
                Reflection.setButton(1, down);
                blocking = down;
                break;
        }
        if (block.get()) {
            blockingTime++;
        } else {
            blockingTime = 0;
        }
    }

    private void setBlockState(boolean state, boolean sendBlock, boolean sendUnBlock) {
        if (Utils.holdingSword()) {
            if (sendBlock && !blocking && state && Utils.holdingSword() && !Raven.badPacketsHandler.C07) {
                sendBlock();
            } else if (sendUnBlock && blocking && !state) {
                unBlock();
            }
        }
        blocking = Reflection.setBlocking(state);
    }

    private void setTarget(float[] rotations) {
        availableTargets.clear();
        block.set(false);
        swing = false;

        final Vec3 eyePos = Utils.getEyePos();
        mc.theWorld.loadedEntityList.stream()
                .filter(Objects::nonNull)
                .filter(entity -> entity != mc.thePlayer)
                .filter(entity -> entity instanceof EntityLivingBase)
                .map(entity -> (EntityLivingBase) entity)
                .filter(entity -> {
                    if (entity instanceof EntityArmorStand) return false;
                    if (entity instanceof EntityPlayer) {
                        if (!targetPlayer.isToggled()) return false;
                        if (Utils.isFriended((EntityPlayer) entity)) {
                            return false;
                        }
                        if (entity.deathTime != 0) {
                            return false;
                        }
                        return !AntiBot.isBot(entity) && !(ignoreTeammates.isToggled() && Utils.isTeamMate(entity));
                    } else return targetEntity.isToggled();
                })
                .filter(entity -> targetInvisible.isToggled() || !entity.isInvisible())
                .filter(entity -> hitThroughBlocks.isToggled() || !behindBlocks(rotations, entity))
                .filter(entity -> fov.getInput() == 360 || Utils.inFov((float) fov.getInput(), entity))
                .map(entity -> new Pair<>(entity, eyePos.distanceTo(RotationUtils.getNearestPoint(entity.getEntityBoundingBox(), eyePos))))
                .forEach(pair -> {
                    // need a more accurate distance check as this can ghost on hypixel
                    if (pair.second() <= blockRange.getInput() && autoBlockMode.getInput() > 0) {
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
                        block.set(true);
                    }
                    if (pair.second() <= swingRange.getInput()) {
                        swing = true;
                    }
                    if (pair.second() <= preAimRange.getInput()) {
                        availableTargets.add(pair.first());
                    }
                });

        if (Math.abs(System.currentTimeMillis() - lastSwitched) > switchDelay.getInput() && switchTargets) {
            switchTargets = false;
            if (entityIndex < availableTargets.size() - 1) {
                entityIndex++;
            } else {
                entityIndex = 0;
            }
            lastSwitched = System.currentTimeMillis();
        }
        if (!availableTargets.isEmpty()) {
            Comparator<EntityLivingBase> comparator = null;
            switch ((int) sortMode.getInput()) {
                case 0:
                    comparator = Comparator.comparingDouble(entityPlayer -> (double)entityPlayer.getHealth());
                    break;
                case 1:
                    comparator = Comparator.comparingDouble(entityPlayer2 -> (double)entityPlayer2.hurtTime);
                    break;
                case 2:
                    comparator = Comparator.comparingDouble(entity -> mc.thePlayer.getDistanceSqToEntity(entity));
                    break;
                case 3:
                    comparator = Comparator.comparingDouble(entity2 -> RotationUtils.distanceFromYaw(entity2, false));
                    break;
            }
            availableTargets.sort(comparator);
            if (entityIndex > (int) targets.getInput() - 1 || entityIndex > availableTargets.size() - 1) {
                entityIndex = 0;
            }
            target = availableTargets.get(entityIndex);
        } else {
            target = null;
        }
    }

    private boolean gameNoAction() {
        if (!Utils.nullCheck()) {
            return true;
        }
        if (ModuleManager.bedAura.isEnabled() && !ModuleManager.bedAura.allowAura.isToggled() && ModuleManager.bedAura.currentBlock != null) {
            return true;
        }
        if (ModuleManager.blink.isEnabled()) return true;
        return mc.thePlayer.isDead;
    }

    private boolean playerNoAction() {
        if (!Mouse.isButtonDown(0) && requireMouseDown.isToggled()) {
            return true;
        } else if (!Utils.holdingWeapon() && weaponOnly.isToggled()) {
            return true;
        } else if (isMining() && disableWhileMining.isToggled()) {
            return true;
        } else if (fixNoSlowFlag.isToggled() && blockingTime > (int) postDelay.getInput()) {
            unBlock();
            blockingTime = 0;
        } else if (ModuleManager.scaffold.isEnabled()) {
            return true;
        }
        return mc.currentScreen != null && disableInInventory.isToggled();
    }

    private void attackAndInteract(EntityLivingBase target, boolean sendInteractAt) {
        if (target != null && attack) {
            attack = false;
            if (noAimToEntity()) {
                return;
            }
            if (ModuleManager.bedAura.rotate) {
                return;
            }
            switchTargets = true;
            Utils.attackEntity(target, !silentSwing.isToggled());
            if (sendInteractAt) {
                Vec3 hitVec = aimSimulator.getHitPos();
                if (hitVec != null) {
                    hitVec = new Vec3(hitVec.x - target.posX, hitVec.y - target.posY, hitVec.z - target.posZ);
                    mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(target, hitVec.toVec3()));
                }
            }
            mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(target, C02PacketUseEntity.Action.INTERACT));
        } else if (ModuleManager.antiFireball != null && ModuleManager.antiFireball.isEnabled() && ModuleManager.antiFireball.fireball != null && ModuleManager.antiFireball.attack) {
            Utils.attackEntity(ModuleManager.antiFireball.fireball, !ModuleManager.antiFireball.silentSwing.isToggled());
            mc.thePlayer.sendQueue.addToSendQueue(new C02PacketUseEntity(ModuleManager.antiFireball.fireball, C02PacketUseEntity.Action.INTERACT));
        }
    }

    private void sendBlock() {
        mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
    }

    private boolean isMining() {
        return Mouse.isButtonDown(0) && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK;
    }

    private void unBlock() {
        if (!Utils.holdingSword()) {
            return;
        }
        mc.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, DOWN));
        blockingTime = 0;
    }

    public void resetBlinkState(boolean unblock) {
        if (!Utils.nullCheck()) return;
        releasePackets();
        blocking = false;
        if (Raven.badPacketsHandler.playerSlot != mc.thePlayer.inventory.currentItem && swapped) {
            mc.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
            Raven.badPacketsHandler.playerSlot = mc.thePlayer.inventory.currentItem;
            swapped = false;
        }
        if (lag && unblock) {
            unBlock();
        }
        lag = false;
    }

    private void releasePackets() {
        try {
            synchronized (blinkedPackets) {
                for (Packet<?> packet : blinkedPackets) {
                    if (packet instanceof C09PacketHeldItemChange) {
                        Raven.badPacketsHandler.playerSlot = ((C09PacketHeldItemChange) packet).getSlotId();
                    }
                    PacketUtils.sendPacketNoEvent(packet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Utils.sendModuleMessage(this, "&cThere was an error releasing blinked packets");
        }
        blinkedPackets.clear();
        blinking = false;
    }

    public static boolean behindBlocks(float[] rotations, EntityLivingBase target) {
        try {
            Vec3 eyePos = Utils.getEyePos();
            MovingObjectPosition hitResult = RotationUtils.rayCast(
                    RotationUtils.getNearestPoint(target.getEntityBoundingBox(), eyePos).distanceTo(eyePos) - 0.01,
                    RotationHandler.getRotationYaw(), RotationHandler.getRotationPitch()
            );
            return hitResult != null;
        } catch (NullPointerException ignored) {
        }
        return false;
    }

    @Override
    public boolean click() {
        switch ((int) attackMode.getInput()) {
            case 0:
                if (target != null && mc.thePlayer.getDistanceToEntity(target) <= swingRange.getInput()) {
                    Utils.sendClick(0, true);
                    Utils.sendClick(0, false);
                    return true;
                }
                return false;
            default:
            case 1:
                if (swing)
                    attack = true;
                return swing;
        }
    }
}