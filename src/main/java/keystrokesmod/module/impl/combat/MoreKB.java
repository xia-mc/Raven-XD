package keystrokesmod.module.impl.combat;

import keystrokesmod.Raven;
import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import java.util.concurrent.TimeUnit;

public class MoreKB extends Module {
    private final String[] MODES = new String[]{"Legit", "Silent"};
    private final ModeSetting mode;
    private final SliderSetting chance;
    private final SliderSetting delay;
    private final SliderSetting rePressDelay;
    private long lastFinish = -1;
    private final ButtonSetting playersOnly;
    private final ButtonSetting notWhileRunner;
    private final ButtonSetting sprintReset;
    private final ButtonSetting sneak;

    private boolean silentNoSprint = false;
    private boolean silentSneak = false;

    public MoreKB() {
        super("MoreKB", category.combat);
        this.registerSetting(mode = new ModeSetting("Mode", MODES, 0));
        this.registerSetting(chance = new SliderSetting("Chance", 100, 0, 100, 1, "%"));
        this.registerSetting(delay = new SliderSetting("Delay", 500, 200, 750, 1, "ms"));
        this.registerSetting(rePressDelay = new SliderSetting("Re-press delay", 100, 1, 500, 1, "ms"));
        this.registerSetting(playersOnly = new ButtonSetting("Players only", true));
        this.registerSetting(notWhileRunner = new ButtonSetting("Not while runner", false));
        this.registerSetting(sprintReset = new ButtonSetting("Sprint reset", true));
        this.registerSetting(sneak = new ButtonSetting("Sneak", false));
    }

    @Override
    public void onDisable() {
        silentNoSprint = false;
        silentSneak = false;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPreMotion(PreMotionEvent event) {
        if (silentNoSprint) event.setSprinting(false);
        if (silentSneak) event.setSneaking(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onAttack(AttackEntityEvent event) {
        final long currentTimeMillis = System.currentTimeMillis();
        if (!Utils.nullCheck() || event.entityPlayer != mc.thePlayer || currentTimeMillis - lastFinish < delay.getInput()) return;
        if (playersOnly.isToggled() && !(event.target instanceof EntityPlayer)) return;
        if (notWhileRunner.isToggled() && !Utils.inFov(180, event.target, mc.thePlayer)) return;
        else if (!(event.target instanceof EntityLivingBase)) return;
        if (((EntityLivingBase) event.target).deathTime != 0) return;
        if (AntiBot.isBot(event.target)) return;

        if (Math.random() > chance.getInput()) return;
        // code
        switch ((int) mode.getInput()) {
            case 0:
                if (sprintReset.isToggled()) {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
                    Raven.getExecutor().schedule(() -> KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode())),
                            (long) rePressDelay.getInput(), TimeUnit.MILLISECONDS);
                }
                if (sneak.isToggled()) {
                    KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), true);
                    Raven.getExecutor().schedule(() -> KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())),
                            (long) rePressDelay.getInput(), TimeUnit.MILLISECONDS);
                }
                break;
            case 1:
                if (sprintReset.isToggled()) {
                    silentNoSprint = true;
                    Raven.getExecutor().schedule(() -> {
                        silentNoSprint = false;
                        }, (long) rePressDelay.getInput(), TimeUnit.MILLISECONDS);
                }
                if (sneak.isToggled()) {
                    silentSneak = true;
                    Raven.getExecutor().schedule(() -> {
                        silentSneak = false;
                        }, (long) rePressDelay.getInput(), TimeUnit.MILLISECONDS);
                }
                break;
        }

        lastFinish = currentTimeMillis;
    }

    @Override
    public String getInfo() {
        return MODES[(int) mode.getInput()];
    }
}
