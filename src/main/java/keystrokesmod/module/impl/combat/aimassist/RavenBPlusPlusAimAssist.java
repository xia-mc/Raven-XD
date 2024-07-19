package keystrokesmod.module.impl.combat.aimassist;

import keystrokesmod.mixins.impl.client.PlayerControllerMPAccessor;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.client.Notifications;
import keystrokesmod.module.impl.combat.AimAssist;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Mouse;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RavenBPlusPlusAimAssist extends SubMode<AimAssist> {
    public static SliderSetting speedYaw, complimentYaw, speedPitch, complimentPitch;
    public static SliderSetting fov;
    public static SliderSetting distance;
    public static SliderSetting pitchOffSet;
    public static ButtonSetting clickAim;
    public static ButtonSetting aimPitch;
    public static ButtonSetting weaponOnly;
    public static ButtonSetting breakBlocks;
    public static ButtonSetting blatantMode;
    public static ButtonSetting ignoreTeammates;
    public RavenBPlusPlusAimAssist(String name, AimAssist parent) {
        super(name, parent);
        this.registerSetting(speedYaw = new SliderSetting("Speed 1 (yaw)", 45.0D, 5.0D, 100.0D, 1.0D));
        this.registerSetting(complimentYaw = new SliderSetting("Speed 2 (yaw)", 15.0D, 2D, 97.0D, 1.0D));
        this.registerSetting(speedPitch = new SliderSetting("Speed 1 (pitch)", 45.0D, 5.0D, 100.0D, 1.0D));
        this.registerSetting(complimentPitch = new SliderSetting("Speed 2 (pitch)", 15.0D, 2D, 97.0D, 1.0D));
        this.registerSetting(pitchOffSet = new SliderSetting("pitchOffSet (blocks)", 4D, -2, 2, 0.050D));
        this.registerSetting(clickAim = new ButtonSetting("Click aim", true));
        this.registerSetting(breakBlocks = new ButtonSetting("Break blocks", true));
        this.registerSetting(weaponOnly = new ButtonSetting("Weapon only", false));
        this.registerSetting(blatantMode = new ButtonSetting("Blatant mode", false));
        this.registerSetting(aimPitch = new ButtonSetting("Aim pitch", false));
        this.registerSetting(ignoreTeammates = new ButtonSetting("Ignore teammates", false));
    }
    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event) {
        try {
            if (Utils.nullCheck()) return;

            if (noAction()) return;
            Notifications.sendNotification(Notifications.NotificationTypes.INFO,"Aiming 1", 2000);
                Notifications.sendNotification(Notifications.NotificationTypes.INFO,"Aiming 2", 2000);
                Entity en = this.getEnemy();
                if (en != null) {
                    Notifications.sendNotification(Notifications.NotificationTypes.INFO, "Aiming at: " + en.getName(), 2000);
                    if (blatantMode.isToggled()) {
                        Utils.aim(en, (float) pitchOffSet.getInput(), false);
                    } else {
                        double n = Utils.fovFromEntity(en);
                        if ((n > 1.0D) || (n < -1.0D)) {
                            double complimentSpeed = n * (ThreadLocalRandom.current().nextDouble(complimentYaw.getInput() - 1.47328, complimentYaw.getInput() + 2.48293) / 100);
                            float val = (float) (-(complimentSpeed + (n / (101.0D - (float) ThreadLocalRandom.current().nextDouble(speedYaw.getInput() - 4.723847, speedYaw.getInput())))));
                            mc.thePlayer.rotationYaw += val;
                        }
                        if (aimPitch.isToggled()) {
                            double complimentSpeed = Utils.PitchFromEntity(en, (float) pitchOffSet.getInput()) * (ThreadLocalRandom.current().nextDouble(complimentPitch.getInput() - 1.47328, complimentPitch.getInput() + 2.48293) / 100);
                            float val = (float) (-(complimentSpeed + (n / (101.0D - (float) ThreadLocalRandom.current().nextDouble(speedPitch.getInput() - 4.723847, speedPitch.getInput())))));
                            mc.thePlayer.rotationPitch += val;
                        }
                    }
                }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private boolean noAction() {
        if (mc.currentScreen != null || !mc.inGameHasFocus) return true;
        if (weaponOnly.isToggled() && !Utils.holdingWeapon()) return true;
        if (clickAim.isToggled() && !Utils.isLeftClicking()) return true;
        return breakBlocks.isToggled() && ((PlayerControllerMPAccessor) mc.playerController).isHittingBlock();
    }
    private @Nullable EntityPlayer getEnemy() {
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
