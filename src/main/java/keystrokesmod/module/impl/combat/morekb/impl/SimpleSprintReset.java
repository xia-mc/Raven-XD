package keystrokesmod.module.impl.combat.morekb.impl;

import keystrokesmod.module.impl.combat.morekb.IMoreKB;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class SimpleSprintReset extends SubMode<IMoreKB> {
    private final SliderSetting minRePressDelay;
    private final SliderSetting maxRePressDelay;
    private final SliderSetting minDelayBetween;
    private final SliderSetting maxDelayBetween;
    private final SliderSetting chance;
    private final ButtonSetting playersOnly;
    private final ButtonSetting notWhileRunner;

    private int delayTicksLeft = 0;
    private int reSprintTicksLeft = -1;

    public SimpleSprintReset(String name, @NotNull IMoreKB parent) {
        super(name, parent);
        this.registerSetting(minRePressDelay = new SliderSetting("Min Re-press delay", 2, 0, 10, 1, "ticks"));
        this.registerSetting(maxRePressDelay = new SliderSetting("Max Re-press delay", 4, 0, 10, 1, "ticks"));
        this.registerSetting(minDelayBetween = new SliderSetting("Min delay between", 10, 4, 13, 1, "ticks"));
        this.registerSetting(maxDelayBetween = new SliderSetting("Min delay between", 10, 4, 13, 1, "ticks"));
        this.registerSetting(chance = new SliderSetting("Chance", 100, 0, 100, 1, "%"));
        this.registerSetting(playersOnly = new ButtonSetting("Players only", true));
        this.registerSetting(notWhileRunner = new ButtonSetting("Not while runner", false));
    }

    @Override
    public void onUpdate() throws Exception {
        if (reSprintTicksLeft == 0) {
            parent.reSprint();
            reSprintTicksLeft = -1;
        } else if (reSprintTicksLeft > 0) {
            reSprintTicksLeft--;
        }

        if (delayTicksLeft > 0)
            delayTicksLeft--;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onAttack(AttackEntityEvent event) {
        if (!Utils.nullCheck() || event.entityPlayer != mc.thePlayer || delayTicksLeft > 0) return;
        if (!(event.target instanceof EntityLivingBase)) return;
        if (playersOnly.isToggled() && !(event.target instanceof EntityPlayer)) return;
        if (notWhileRunner.isToggled() && !Utils.inFov(180, event.target, mc.thePlayer)) return;
        if (((EntityLivingBase) event.target).deathTime != 0) return;
        if (AntiBot.isBot(event.target)) return;

        if (Math.random() > chance.getInput()) return;

        parent.stopSprint();

        reSprintTicksLeft = Utils.randomizeInt(minRePressDelay.getInput(), maxRePressDelay.getInput());
        delayTicksLeft = reSprintTicksLeft + Utils.randomizeInt(minDelayBetween.getInput(), maxDelayBetween.getInput());
    }
}
