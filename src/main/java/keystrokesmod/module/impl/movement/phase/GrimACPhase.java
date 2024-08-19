package keystrokesmod.module.impl.movement.phase;

import keystrokesmod.event.BlockAABBEvent;
import keystrokesmod.event.WorldChangeEvent;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.movement.Phase;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class GrimACPhase extends SubMode<Phase> {
    private final SliderSetting motion;
    private final ButtonSetting blink;
    private final ButtonSetting onlyMurderMystery;

    public GrimACPhase(String name, @NotNull Phase parent) {
        super(name, parent);
        this.registerSetting(motion = new SliderSetting("Motion", 3.9, 1, 10, 0.1));
        this.registerSetting(blink = new ButtonSetting("Blink", false));
        this.registerSetting(onlyMurderMystery = new ButtonSetting("Only murder mystery", false));
    }

    @SubscribeEvent
    public void onWorldChange(WorldChangeEvent event) {
        if (!onlyMurderMystery.isToggled() || isMurderMystery())
            mc.thePlayer.motionY = motion.getInput();
        if (blink.isToggled())
            ModuleManager.blink.enable();
    }

    @SubscribeEvent
    public void onAABB(BlockAABBEvent event) {
        if ((!onlyMurderMystery.isToggled() || isMurderMystery()) && mc.thePlayer.ticksExisted < 10)
            event.setBoundingBox(null);
    }

    @Override
    public void onUpdate() {
        if (mc.thePlayer.ticksExisted == 10 && blink.isToggled())
            ModuleManager.blink.disable();
    }

    @Override
    public void onDisable() {
        blink.disable();
    }

    private static boolean isMurderMystery() {
        try {
            return Utils.getSidebarLines().get(0).contains("MURDER");
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
    }
}
