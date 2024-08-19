package keystrokesmod.module.impl.combat.velocity;

import keystrokesmod.event.PostVelocityEvent;
import keystrokesmod.module.impl.combat.Velocity;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class IntaveVelocity extends SubMode<Velocity> {
    private final SliderSetting xzOnHit;
    private final SliderSetting xzOnSprintHit;
    private final ButtonSetting reduceUnnecessarySlowdown;
    private final SliderSetting chance;
    private final ButtonSetting jump;
    private final ButtonSetting jumpInInv;
    private final SliderSetting jumpChance;
    private final ButtonSetting notWhileSpeed;
    private final ButtonSetting notWhileJumpBoost;
    private final ButtonSetting debug;

    private boolean reduced = false;

    public IntaveVelocity(String name, @NotNull Velocity parent) {
        super(name, parent);
        this.registerSetting(xzOnHit = new SliderSetting("XZ on hit", 0.6, 0, 1, 0.01));
        this.registerSetting(xzOnSprintHit = new SliderSetting("XZ on sprint hit", 0.6, 0, 1, 0.01));
        this.registerSetting(reduceUnnecessarySlowdown = new ButtonSetting("Reduce unnecessary slowdown", false));
        this.registerSetting(chance = new SliderSetting("Chance", 100, 0, 100, 1, "%"));
        this.registerSetting(jump = new ButtonSetting("Jump", false));
        this.registerSetting(jumpInInv = new ButtonSetting("Jump in inv", false, jump::isToggled));
        this.registerSetting(jumpChance = new SliderSetting("Jump chance", 80, 0, 100, 1, "%", jump::isToggled));
        this.registerSetting(notWhileSpeed = new ButtonSetting("Not while speed", false));
        this.registerSetting(notWhileJumpBoost = new ButtonSetting("Not while jump boost", false));
        this.registerSetting(debug = new ButtonSetting("Debug", false));
    }

    @Override
    public void onEnable() {
        reduced = false;
    }

    @SubscribeEvent
    public void onPostVelocity(PostVelocityEvent event) {
        if (noAction()) return;

        if (jump.isToggled()) {
            if (Math.random() > jumpChance.getInput() / 100) return;

            if (mc.thePlayer.onGround && (jumpInInv.isToggled() || mc.currentScreen == null))
                mc.thePlayer.jump();
        }
        reduced = false;
    }

    @SubscribeEvent
    public void onAttack(@NotNull AttackEntityEvent event) {
        if (event.target instanceof EntityLivingBase && mc.thePlayer.hurtTime > 0) {
            if (noAction()) return;
            if (Math.random() > chance.getInput() / 100) return;
            if (reduceUnnecessarySlowdown.isToggled() && reduced) return;

            if (mc.thePlayer.isSprinting()) {
                mc.thePlayer.motionX *= xzOnSprintHit.getInput();
                mc.thePlayer.motionZ *= xzOnSprintHit.getInput();
            } else {
                mc.thePlayer.motionX *= xzOnHit.getInput();
                mc.thePlayer.motionZ *= xzOnHit.getInput();
            }
            reduced = true;
            if (debug.isToggled())
                Utils.sendMessage(String.format("Reduced %.3f %.3f", mc.thePlayer.motionX, mc.thePlayer.motionZ));
        }
    }

    private boolean noAction() {
        return mc.thePlayer.getActivePotionEffects().parallelStream()
                .anyMatch(effect -> notWhileSpeed.isToggled() && effect.getPotionID() == Potion.moveSpeed.getId()
                        || notWhileJumpBoost.isToggled() && effect.getPotionID() == Potion.jump.getId());
    }
}
