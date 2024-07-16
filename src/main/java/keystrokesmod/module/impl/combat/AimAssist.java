package keystrokesmod.module.impl.combat;

import akka.japi.Pair;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.mixins.impl.client.PlayerControllerMPAccessor;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.other.anticheats.utils.world.PlayerRotation;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.AimSimulator;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class AimAssist extends Module {
    private final ButtonSetting clickAim;
    private final ButtonSetting aimWhileOnTarget;
    private final ButtonSetting strafeIncrease;
    private final ButtonSetting checkBlockBreak;
    private final ButtonSetting aimVertically;
    private final SliderSetting verticalSpeed;
    private final SliderSetting horizontalSpeed;
    private final SliderSetting maxAngle;
    private final SliderSetting distance;
    private final ButtonSetting weaponOnly;
    private final ButtonSetting ignoreTeammates;

    private Double yawNoise = null;
    private Double pitchNoise = null;
    private long nextNoiseRefreshTime = -1;
    private long nextNoiseEmptyTime = 200;
    public AimAssist() {
        super("AimAssist", category.combat, 0);
        this.registerSetting(clickAim = new ButtonSetting("Click aim", true));
        this.registerSetting(aimWhileOnTarget = new ButtonSetting("Aim while on target", true));
        this.registerSetting(strafeIncrease = new ButtonSetting("Strafe increase", false));
        this.registerSetting(checkBlockBreak = new ButtonSetting("Check block break", false));
        this.registerSetting(aimVertically = new ButtonSetting("Aim vertically", false));
        this.registerSetting(verticalSpeed = new SliderSetting("Vertical speed", 5, 1, 10, 0.1, aimVertically::isToggled));
        this.registerSetting(horizontalSpeed = new SliderSetting("Horizontal speed", 5, 1, 10, 0.1));
        this.registerSetting(maxAngle = new SliderSetting("Max angle", 180, 1, 360, 5));
        this.registerSetting(distance = new SliderSetting("Distance", 5, 1, 8, 0.1));
        this.registerSetting(weaponOnly = new ButtonSetting("Weapon only", false));
        this.registerSetting(ignoreTeammates = new ButtonSetting("Ignore teammates", false));
    }

    @Override
    public void onDisable() {
        yawNoise = pitchNoise = null;
        nextNoiseRefreshTime = -1;
    }

    @Override
    public void onUpdate() {
        if (noAction()) {
            return;
        }

        final EntityPlayer target = getEnemy();
        if (target == null) return;
        final boolean onTarget = mc.objectMouseOver != null
                && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY
                && mc.objectMouseOver.entityHit == target;

        double deltaYaw = yawNoise;
        double deltaPitch = pitchNoise;

        double hSpeed = horizontalSpeed.getInput();
        double vSpeed = verticalSpeed.getInput();


        if (onTarget) {
            if (aimWhileOnTarget.isToggled()) {
                hSpeed *= 0.85;
                vSpeed *= 0.85;
            } else {
                hSpeed = 0;
                vSpeed = 0;
            }
        }

        if (strafeIncrease.isToggled()) {
            int mouseX = Math.abs(mc.mouseHelper.deltaX);
            int mouseY = Math.abs(mc.mouseHelper.deltaY);

            if (mouseX > 100)
                hSpeed = 0;
            else
                hSpeed = Math.min(hSpeed, (100 - mouseX) / 35.0);

            if (mouseY > 100)
                vSpeed = 0;
            else
                vSpeed = Math.min(hSpeed, (100 - mouseY) / 35.0);
        }

        final Pair<Pair<Float, Float>, Pair<Float, Float>> rotation = getRotation(target.getEntityBoundingBox());
        final Pair<Float, Float> yaw = rotation.first();
        final Pair<Float, Float> pitch = rotation.second();

        boolean move = false;

        final float curYaw = mc.thePlayer.rotationYaw;
        final float curPitch = mc.thePlayer.rotationPitch;
        if (yaw.first() > curYaw) {
            move = true;
            final float after = AimSimulator.rotMove(yaw.first(), curYaw, (float) hSpeed);
            deltaYaw += after - curYaw;
        } else if (yaw.second() < curYaw) {
            move = true;
            final float after = AimSimulator.rotMove(yaw.second(), curYaw, (float) hSpeed);
            deltaYaw += after - curYaw;
        }
        if (aimVertically.isToggled()) {
            if (pitch.first() > curPitch) {
                move = true;
                final float after = AimSimulator.rotMove(pitch.first(), curPitch, (float) vSpeed);
                deltaPitch += after - curPitch;
            } else if (pitch.second() < curPitch) {
                move = true;
                final float after = AimSimulator.rotMove(pitch.second(), curPitch, (float) vSpeed);
                deltaPitch += after - curPitch;
            }
        }

        if (move) {
            deltaYaw += (Math.random() - 0.5) * Math.min(0.8, deltaPitch / 10.0);
            deltaPitch += (Math.random() - 0.5) * Math.min(0.8, deltaYaw / 10.0);
        }

        mc.thePlayer.rotationYaw += (float) deltaYaw;
        mc.thePlayer.rotationPitch += (float) deltaPitch;
    }

    private @NotNull Pair<Pair<Float, Float>, Pair<Float, Float>> getRotation(@NotNull AxisAlignedBB boundingBox) {
        float minYaw = Float.MAX_VALUE;
        float maxYaw = Float.MIN_VALUE;

        for (Double x : Arrays.asList(boundingBox.minX, boundingBox.maxX)) {
            for (Double z : Arrays.asList(boundingBox.minZ, boundingBox.maxZ)) {
                float yaw = PlayerRotation.getYaw(new Vec3(x, 0, z));

                if (yaw < minYaw)
                    minYaw = yaw;
                if (yaw > maxYaw)
                    maxYaw = yaw;
            }
        }

        float pitch1 = PlayerRotation.getPitch(new Vec3(0, boundingBox.minY + 1.12, 0));
        float pitch2 = PlayerRotation.getPitch(new Vec3(0, boundingBox.maxY + 0.12, 0));

        return new Pair<>(
                new Pair<>(minYaw, maxYaw),
                new Pair<>(Math.min(pitch1, pitch2), Math.max(pitch1, pitch2))
        );
    }

    private boolean noAction() {
        if (mc.currentScreen != null || !mc.inGameHasFocus) return true;
        if (weaponOnly.isToggled() && !Utils.holdingWeapon()) return true;
        if (yawNoise == null || pitchNoise == null) return true;
        if (clickAim.isToggled() && !Utils.ilc()) return true;
        return checkBlockBreak.isToggled() && ((PlayerControllerMPAccessor) mc.playerController).isHittingBlock();
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event) {
        long time = System.currentTimeMillis();
        if (nextNoiseRefreshTime == -1 || time >= nextNoiseRefreshTime + nextNoiseEmptyTime) {
            nextNoiseRefreshTime = (long) (time + Math.random() * 60 + 80);
            nextNoiseEmptyTime = (long) (Math.random() * 100 + 180);
            yawNoise = (Math.random() - 0.5) * 2 * ((Math.random() - 0.5) * 0.3 + 0.8);
            pitchNoise = (Math.random() - 0.5) * 2 * ((Math.random() - 0.5) * 0.35 + 0.6);
        } else if (time >= nextNoiseRefreshTime) {
            yawNoise = 0d;
            pitchNoise = 0d;
        }
    }

    private @Nullable EntityPlayer getEnemy() {
        final int fov = (int) maxAngle.getInput();
        final List<EntityPlayer> players = mc.theWorld.playerEntities;
        final Vec3 playerPos = new Vec3(mc.thePlayer);

        EntityPlayer target = null;
        double targetFov = Double.MAX_VALUE;
        for (final EntityPlayer entityPlayer : players) {
            if (entityPlayer != mc.thePlayer && entityPlayer.deathTime == 0) {
                if (Utils.isFriended(entityPlayer))
                    continue;
                if (AntiBot.isBot(entityPlayer))
                    continue;
                if (ignoreTeammates.isToggled() && Utils.isTeamMate(entityPlayer))
                    continue;
                if (playerPos.distanceTo(entityPlayer) > distance.getInput())
                    continue;
                if (fov != 360 && !Utils.inFov(fov, entityPlayer))
                    continue;

                double curFov = Math.abs(Utils.getFov(entityPlayer.posX, entityPlayer.posZ));
                if (curFov < targetFov) {
                    target = entityPlayer;
                    targetFov = curFov;
                }
            }
        }
        return target;
    }
}
