package keystrokesmod.module.impl.combat.aimassist;

import keystrokesmod.mixins.impl.client.PlayerControllerMPAccessor;
import keystrokesmod.module.impl.client.Notifications;
import keystrokesmod.module.impl.combat.AimAssist;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.RotationUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TejasAssist extends SubMode<AimAssist> {
    public static ButtonSetting throughBlock, clickAim, aimPitch, weaponOnly, breakBlocks, blatantMode, ignoreTeammates;
    public static SliderSetting speedYaw, complimentYaw, speedPitch, complimentPitch, distance, pitchOffSet, maxAngle;

    public TejasAssist(String name, AimAssist parent) {
        super(name, parent);
        this.registerSetting(clickAim = new ButtonSetting("Click aim", true));
        this.registerSetting(breakBlocks = new ButtonSetting("Break blocks", true));
        this.registerSetting(weaponOnly = new ButtonSetting("Weapon only", false));
        this.registerSetting(blatantMode = new ButtonSetting("Blatant mode", false));
        this.registerSetting(aimPitch = new ButtonSetting("Aim pitch", false));
        this.registerSetting(ignoreTeammates = new ButtonSetting("Ignore teammates", false));
        this.registerSetting(throughBlock = new ButtonSetting("Through block", true));
        this.registerSetting(speedYaw = new SliderSetting("Speed 1 (yaw)", 45.0D, 5.0D, 100.0D, 1.0D));
        this.registerSetting(maxAngle = new SliderSetting("Max angle", 180, 1, 360, 5));
        this.registerSetting(complimentYaw = new SliderSetting("Speed 2 (yaw)", 15.0D, 2D, 97.0D, 1.0D));
        this.registerSetting(speedPitch = new SliderSetting("Speed 1 (pitch)", 45.0D, 5.0D, 100.0D, 1.0D));
        this.registerSetting(complimentPitch = new SliderSetting("Speed 2 (pitch)", 15.0D, 2D, 97.0D, 1.0D));
        this.registerSetting(pitchOffSet = new SliderSetting("pitchOffSet (blocks)", 4D, -2, 2, 0.050D));
        this.registerSetting(distance = new SliderSetting("Distance", 5, 1, 8, 0.1));
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event) {
        if (noAction()) {
            return;
        }
        final EntityPlayer target = getEnemy();
        if (target == null) return;
        if (blatantMode.isToggled()) {
            Utils.aim(target, (float) pitchOffSet.getInput(), false);
        } else {
            double n = Utils.fovFromEntity(target);
            if ((n > 1.0D) || (n < -1.0D)) {
                double complimentSpeed = n * (ThreadLocalRandom.current().nextDouble(complimentYaw.getInput() - 1.47328, complimentYaw.getInput() + 2.48293) / 100);
                float val = (float) (-(complimentSpeed + (n / (101.0D - (float) ThreadLocalRandom.current().nextDouble(speedYaw.getInput() - 4.723847, speedYaw.getInput())))));
                mc.thePlayer.rotationYaw += val;
            }
            if (aimPitch.isToggled()) {
                double complimentSpeed = Utils.PitchFromEntity(target, (float) pitchOffSet.getInput()) * (ThreadLocalRandom.current().nextDouble(complimentPitch.getInput() - 1.47328, complimentPitch.getInput() + 2.48293) / 100);
                float val = (float) (-(complimentSpeed + (n / (101.0D - (float) ThreadLocalRandom.current().nextDouble(speedPitch.getInput() - 4.723847, speedPitch.getInput())))));
                mc.thePlayer.rotationPitch += val;
            }
        }
    }

    private boolean noAction() {
        if (mc.currentScreen != null || !mc.inGameHasFocus) return true;
        if (weaponOnly.isToggled() && !Utils.holdingWeapon()) return true;
        if (clickAim.isToggled() && !Utils.isLeftClicking()) return true;
        return breakBlocks.isToggled() && ((PlayerControllerMPAccessor) mc.playerController).isHittingBlock();
    }

    private @Nullable EntityPlayer getEnemy() {
        final int fov = (int) maxAngle.getInput();
        final List<EntityPlayer> players = mc.theWorld.playerEntities;
        final Vec3 playerPos = new Vec3(mc.thePlayer);

        EntityPlayer target = null;
        double targetFov = Double.MAX_VALUE;
        for (final EntityPlayer entityPlayer : players) {
            if (entityPlayer != mc.thePlayer && entityPlayer.deathTime == 0) {
                double dist = playerPos.distanceTo(entityPlayer);
                if (Utils.isFriended(entityPlayer))
                    continue;
                if (AntiBot.isBot(entityPlayer))
                    continue;
                if (ignoreTeammates.isToggled() && Utils.isTeamMate(entityPlayer))
                    continue;
                if (dist > distance.getInput())
                    continue;
                if (fov != 360 && !Utils.inFov(fov, entityPlayer))
                    continue;
                if (!throughBlock.isToggled() && RotationUtils.rayCast(dist, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch) != null)
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
