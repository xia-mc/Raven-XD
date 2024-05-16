package keystrokesmod.module.impl.combat;

import keystrokesmod.Raven;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
public class AimAssist extends Module {
    private final SliderSetting speed;
    private final SliderSetting fov;
    private final SliderSetting distance;
    private final ButtonSetting clickAim;
    private final ButtonSetting weaponOnly;
    private final ButtonSetting aimInvis;
    private final ButtonSetting blatantMode;
    private final ButtonSetting ignoreTeammates;

    public AimAssist() {
        super("AimAssist", category.combat, 0);
        this.registerSetting(speed = new SliderSetting("Speed", 45.0D, 1.0D, 100.0D, 1.0D));
        this.registerSetting(fov = new SliderSetting("FOV", 90.0D, 15.0D, 180.0D, 1.0D));
        this.registerSetting(distance = new SliderSetting("Distance", 4.5D, 1.0D, 10.0D, 0.5D));
        this.registerSetting(clickAim = new ButtonSetting("Click aim", true));
        this.registerSetting(weaponOnly = new ButtonSetting("Weapon only", false));
        this.registerSetting(aimInvis = new ButtonSetting("Aim invis", false));
        this.registerSetting(blatantMode = new ButtonSetting("Blatant mode", false));
        this.registerSetting(ignoreTeammates = new ButtonSetting("Ignore teammates", false));
    }

    public void onUpdate() {
        if (mc.currentScreen == null && mc.inGameHasFocus) {
            if (!weaponOnly.isToggled() || Utils.holdingWeapon()) {
                if (!clickAim.isToggled() || Utils.ilc()) {
                    Entity en = this.getEnemy();
                    if (en != null) {
                        if (Raven.debugger) {
                            Utils.sendMessage(this.getName() + " &e" + en.getName());
                        }
                        if (blatantMode.isToggled()) {
                            Utils.aim(en, 0.0F, false);
                        } else {
                            double n = Utils.n(en);
                            if (n > 1.0D || n < -1.0D) {
                                float val = (float) (-(n / (101.0D - (speed.getInput()))));
                                mc.thePlayer.rotationYaw += val;
                            }
                        }
                    }

                }
            }
        }
    }

    private Entity getEnemy() {
        final int n = (int)fov.getInput();
        for (final EntityPlayer entityPlayer : mc.theWorld.playerEntities) {
            if (entityPlayer != mc.thePlayer && entityPlayer.deathTime == 0) {
                if (Utils.isFriended(entityPlayer)) {
                    continue;
                }
                if (ignoreTeammates.isToggled() && Utils.isTeamMate(entityPlayer)) {
                    continue;
                }
                if (!aimInvis.isToggled() && entityPlayer.isInvisible()) {
                    continue;
                }
                if (mc.thePlayer.getDistanceToEntity(entityPlayer) > distance.getInput()) {
                    continue;
                }
                if (AntiBot.isBot(entityPlayer)) {
                    continue;
                }
                if (!blatantMode.isToggled() && n != 360 && !Utils.inFov((float)n, entityPlayer)) {
                    continue;
                }
                return entityPlayer;
            }
        }
        return null;
    }
}
