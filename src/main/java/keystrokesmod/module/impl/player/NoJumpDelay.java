package keystrokesmod.module.impl.player;

import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.mixins.impl.entity.EntityLivingBaseAccessor;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.setting.impl.ButtonSetting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class NoJumpDelay extends Module {
    private final ButtonSetting notWhileScaffold;

    public NoJumpDelay() {
        super("NoJumpDelay", category.player);
        this.registerSetting(notWhileScaffold = new ButtonSetting("Not while scaffold", false));
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        if (!notWhileScaffold.isToggled() || !ModuleManager.scaffold.isEnabled()) {
            ((EntityLivingBaseAccessor) mc.thePlayer).setJumpTicks(0);
        }
    }
}
