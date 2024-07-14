package keystrokesmod.module.impl.combat;

import akka.japi.Pair;
import keystrokesmod.event.PostMotionEvent;
import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.RotationEvent;
import keystrokesmod.mixins.impl.client.KeyBindingAccessor;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.impl.other.anticheats.utils.world.PlayerRotation;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.*;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KillAuraV2 extends Module {
    private int cps;
    private int targetIndex = 0;
    private float lastYaw;
    private float lastPitch;
    public static boolean aiming;
    public static boolean blocking;
    public static boolean wasBlocking;
    public static EntityLivingBase target;
    private final TimerUtil attackTimer = new TimerUtil();
    private final TimerUtil switchTimer = new TimerUtil();
    public static final List<EntityLivingBase> targets = new ArrayList<>();
    
    private final ButtonSetting targetPlayer = new ButtonSetting("Players", true);
    private final ButtonSetting targetAnimals = new ButtonSetting("Animals", false);
    private final ButtonSetting targetMobs = new ButtonSetting("Mobs", false);
    private final ButtonSetting targetInvisible = new ButtonSetting("Invisible", false);

    private final ModeSetting mode = new ModeSetting("Mode", new String[]{"Single", "Switch"}, 0);
    public final SliderSetting switchDelay = new SliderSetting("Switch delay",200,0,1000,50, new ModeOnly(mode, 1));

    private final SliderSetting rotationSpeed = new SliderSetting("Rotation speed", 20, 2, 20, 0.1);
    private final ModeSetting rotationMode = new ModeSetting("Rotation mode", new String[]{"Instant", "Nearest"}, 0);
    private final ModeSetting moveFixMode = new ModeSetting("MoveFix mode", RotationHandler.MoveFix.MODES, 2);

    private final ButtonSetting autoBlock = new ButtonSetting("AutoBlock", false);
    private final ModeSetting autoBlockMode = new ModeSetting("AutoBlock mode", new String[]{"Fake", "Watchdog", "GrimAC 1.8", "GrimAC 1.12"}, 0, autoBlock::isToggled);
    private final ButtonSetting fixNoSlowFlag = new ButtonSetting("Fix NoSlow flag", false, () -> autoBlock.isToggled() && autoBlockMode.getInput() == 1);
    private final ModeSetting sortMode = new ModeSetting("Sort Mode", new String[]{"Distance", "Hurt Time", "Health", "Armor"}, 0, autoBlock::isToggled);

    private final SliderSetting minCPS = new SliderSetting("Min CPS", 10, 1, 20, 1);
    private final SliderSetting maxCPS = new SliderSetting("Max CPS", 20, 1, 20, 1);
    private final SliderSetting preAimRange = new SliderSetting("PreAim range", 3.5, 3, 10, 0.1);
    public static final SliderSetting attackRange = new SliderSetting("Attack range", 3, 3, 6, 0.1);

    private static final ButtonSetting ThroughWalls = new ButtonSetting("Through walls", false);
    private final ButtonSetting RayCast = new ButtonSetting("Ray cast", true);

    private int autoBlock$watchdog$blockingTime = 0;

    public KillAuraV2() {
        super("KillAuraV2", category.experimental);
        this.registerSetting(mode, switchDelay, minCPS, maxCPS, rotationMode, moveFixMode, rotationSpeed, autoBlock, autoBlockMode, fixNoSlowFlag, preAimRange, attackRange, sortMode, ThroughWalls, RayCast, targetPlayer, targetAnimals, targetMobs, targetInvisible);
    }

    @Override
    public String getInfo() {
        return this.mode.getOptions()[(int) this.mode.getInput()];
    }

    @Override
    public void guiUpdate() {
        Utils.correctValue(minCPS, maxCPS);
        Utils.correctValue(attackRange, preAimRange);
    }

    private void attack() {
        if (target != null && RotationUtils.isMouseOver(RotationHandler.getRotationYaw(), RotationHandler.getRotationPitch(), target, (float) attackRange.getInput())) {
            this.attackEntity(target);
            if (mc.thePlayer.fallDistance > 0.0f && !mc.thePlayer.onGround && !mc.thePlayer.isOnLadder() && !mc.thePlayer.isInWater() && !mc.thePlayer.isPotionActive(Potion.blindness) && mc.thePlayer.ridingEntity == null) {
                mc.thePlayer.onCriticalHit(target);
            }
            // EnchantmentHelper#getModifierForCreature()
            if (EnchantmentHelper.func_152377_a(mc.thePlayer.getHeldItem(), target.getCreatureAttribute()) > 0.0f) {
                mc.thePlayer.onEnchantmentCritical(target);
                PacketUtils.sendPacket(new C0APacketAnimation());
            }
        }
    }

    private void attackEntity(final Entity target) {
        Utils.attackEntity(target, true);
        this.attackTimer.reset();
    }

    @Override
    public void onDisable() {
        target = null;
        targets.clear();
        aiming = false;
        blocking = false;
        if (wasBlocking) {
            int autoBlock = (int) this.autoBlockMode.getInput();
            switch (autoBlock) {
                case 2:  // grim 1.8
                case 3:  // grim 1.12
                    ((KeyBindingAccessor) mc.gameSettings.keyBindUseItem).setPressed(false);
                    break;
                case 1:  // watchdog
                    PacketUtils.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                    break;
            }
        }
        wasBlocking = false;
        autoBlock$watchdog$blockingTime = 0;
        super.onDisable();
    }

    @SubscribeEvent
    public void onRotation(RotationEvent event) {
        if (minCPS.getInput() > maxCPS.getInput()) {
            minCPS.setValue(minCPS.getInput() - 1);
        }

        if (ModuleManager.scaffold.isEnabled()) return;
        // Gets all entities in specified range, sorts them using your specified sort mode, and adds them to target list

        this.sortTargets();
        if (target == null) {
            lastYaw = event.getYaw();
            lastPitch = event.getPitch();
        }

        aiming = !targets.isEmpty();
        blocking = autoBlock.isToggled() && aiming && Utils.holdingSword();
        if (aiming) {
            switch ((int) mode.getInput()) {
                case 0:
                    if (!targets.isEmpty()) {
                        target = targets.get(0);
                    } else {
                        target = null;
                    }
                    break;
                case 1:
                    if (switchTimer.hasTimeElapsed((int) switchDelay.getInput(), true)) {
                        targetIndex = (targetIndex + 1) % targets.size();
                    }
                    if (targetIndex < targets.size()) {
                        target = targets.get(targetIndex);
                    } else {
                        target = null;
                    }
                    break;
            }

            float yaw = RotationHandler.getRotationYaw();
            float pitch = RotationHandler.getRotationPitch();
            final double minRotationSpeed = this.rotationSpeed.getInput();
            final double maxRotationSpeed = this.rotationSpeed.getInput();
            final float rotationSpeed = (float) Utils.randomizeDouble(minRotationSpeed, maxRotationSpeed);
            switch ((int) rotationMode.getInput()) {
                case 0:
                    if (target != null) {
                        Vec3 eyePos = Utils.getEyePos(target);
                        yaw = PlayerRotation.getYaw(eyePos);
                        pitch = PlayerRotation.getPitch(eyePos);
                    }
                    break;
                case 1:
                    if (target != null) {
                        Pair<Float, Float> aimResult = AimSimulator.getLegitAim(target, mc.thePlayer, true, true, false, null, 0);
                        yaw = aimResult.first();
                        pitch = aimResult.second();
                    }
            }
            event.setYaw(lastYaw = AimSimulator.rotMove(yaw, lastYaw, rotationSpeed));
            event.setPitch(lastPitch = AimSimulator.rotMove(pitch, lastPitch, rotationSpeed));
            event.setMoveFix(RotationHandler.MoveFix.values()[(int) moveFixMode.getInput()]);

            if (RayCast.isToggled() && !RotationUtils.isMouseOver(lastYaw, lastPitch, target, (float) attackRange.getInput()))
                return;

            if (attackTimer.hasTimeElapsed(cps, true)) {
                final int maxValue = (int) ((minCPS.getMax() - maxCPS.getInput()) * 5.0);
                final int minValue = (int) ((minCPS.getMax() - minCPS.getInput()) * 5.0);
                cps = Utils.randomizeInt(minValue, maxValue);
                attack();
            }

        } else {
            attackTimer.reset();
            target = null;
        }
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        int autoBlock = (int) autoBlockMode.getInput();
        if (blocking) {
            switch (autoBlock) {
                case 0:  // fake
                    break;
                case 1:  // watchdog
                    if (autoBlock$watchdog$blockingTime < 10 || !fixNoSlowFlag.isToggled()) {
                        PacketUtils.sendPacket(new C02PacketUseEntity(target, C02PacketUseEntity.Action.INTERACT));
                        PacketUtils.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                        wasBlocking = true;
                        autoBlock$watchdog$blockingTime++;
                    } else {
                        if (wasBlocking) {
                            PacketUtils.sendPacket(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
                        }
                        wasBlocking = false;
                        autoBlock$watchdog$blockingTime = 0;
                    }
                    break;
                case 3:  // grim 1.12
                    if (SlotHandler.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemSword) {
                    PacketUtils.sendPacket(new C0FPacketConfirmTransaction(Utils.randomizeInt(0, 2147483647), (short) Utils.randomizeInt(0, -32767), true));
                    PacketUtils.sendPacket(new C0APacketAnimation());
                    mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem());
                    wasBlocking = true;
                    }
                    break;
            }
        } else if (wasBlocking && autoBlock == 2 || autoBlock == 3) {
            ((KeyBindingAccessor) mc.gameSettings.keyBindUseItem).setPressed(false);
            wasBlocking = false;
        } else if (wasBlocking && autoBlock == 1) {
            PacketUtils.sendPacketNoEvent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN));
            wasBlocking = false;
        }
    }

    @SubscribeEvent
    public void onPostMotion(PostMotionEvent event) {
        if (blocking) {
            if ((int) autoBlockMode.getInput() == 2) {  // Grim 1.8
                mc.playerController.sendUseItem(mc.thePlayer, mc.theWorld, SlotHandler.getHeldItem());
            }
        }
    }

    public void sortTargets() {
        targets.clear();
        for (Entity entity : mc.theWorld.getLoadedEntityList()) {
            if (entity instanceof EntityLivingBase) {
                EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
                if (mc.thePlayer.getDistanceToEntity(entity) <= preAimRange.getInput() && isValid(entity) && mc.thePlayer != entityLivingBase && !Utils.isFriended(entityLivingBase.getName()) && !AntiBot.isBot(entity)) {
                    targets.add(entityLivingBase);
                }
            }
        }
        switch ((int) sortMode.getInput()) {
            case 0:  // Distance
                targets.sort(Comparator.comparingDouble(mc.thePlayer::getDistanceToEntity));
                break;
            case 1:  // Hurt Time
                targets.sort(Comparator.comparingInt(entity -> entity.hurtTime));
                break;
            case 2:  // Health
                targets.sort(Comparator.comparingDouble(EntityLivingBase::getHealth));
                break;
            case 3:  // Armor
                targets.sort(Comparator.comparingInt(EntityLivingBase::getTotalArmorValue));
                break;
        }
    }

    public boolean isValid(Entity entity) {

        if (entity instanceof EntityPlayer && targetPlayer.isToggled() && !entity.isInvisible() && mc.thePlayer.canEntityBeSeen(entity))
            return true;

        if (entity instanceof EntityPlayer && targetInvisible.isToggled() && entity.isInvisible())
            return true;

        if(entity instanceof EntityPlayer && ThroughWalls.isToggled() && !mc.thePlayer.canEntityBeSeen(entity))
            return true;

        if (entity instanceof EntityAnimal && targetAnimals.isToggled())
            return true;

        if (entity instanceof EntityMob && targetMobs.isToggled())
            return true;

        return entity.isInvisible() && targetInvisible.isToggled();
    }
}