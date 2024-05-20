package keystrokesmod.module.impl.combat;

import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.movement.LongJump;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

public class Velocity extends Module {
    public static SliderSetting horizontal;
    public static SliderSetting vertical;
    private final SliderSetting chance;
    private final ButtonSetting onlyWhileTargeting;
    private final ButtonSetting disableS;

    public Velocity() {
        super("Velocity", category.combat, 0);
        this.registerSetting(horizontal = new SliderSetting("Horizontal", 90.0D, 0.0D, 100.0D, 1.0D));
        this.registerSetting(vertical = new SliderSetting("Vertical", 100.0D, 0.0D, 100.0D, 1.0D));
        this.registerSetting(chance = new SliderSetting("Chance", 100.0D, 0.0D, 100.0D, 1.0D, "%"));
        this.registerSetting(onlyWhileTargeting = new ButtonSetting("Only while targeting", false));
        this.registerSetting(disableS = new ButtonSetting("Disable while holding S", false));
    }

    @Override
    public String getInfo() {
        return (horizontal.getInput() == 100 ? "" : (int) horizontal.getInput() + "h") + (horizontal.getInput() != 100 && vertical.getInput() != 100 ? " " : "") + (vertical.getInput() == 100 ? "" : (int) vertical.getInput() + "v");
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingUpdateEvent ev) {
        if (Utils.nullCheck() && !LongJump.stopModules) {
            if (ModuleManager.antiKnockback.isEnabled()) {
                return;
            }
            if (mc.thePlayer.maxHurtTime <= 0 || mc.thePlayer.hurtTime != mc.thePlayer.maxHurtTime) {
                return;
            }
            if (onlyWhileTargeting.isToggled() && (mc.objectMouseOver == null || mc.objectMouseOver.entityHit == null)) {
                return;
            }
            if (disableS.isToggled() && Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode())) {
                return;
            }
            if (chance.getInput() != 100) {
                double ch = Math.random();
                if (ch >= chance.getInput() / 100.0D) {
                    return;
                }
            }
            if (horizontal.getInput() != 100.0D) {
                mc.thePlayer.motionX *= horizontal.getInput() / 100;
                mc.thePlayer.motionZ *= horizontal.getInput() / 100;
            }
            if (vertical.getInput() != 100.0D) {
                mc.thePlayer.motionY *= vertical.getInput() / 100;
            }
        }
    }
}
