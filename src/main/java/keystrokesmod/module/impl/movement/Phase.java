package keystrokesmod.module.impl.movement;

import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.movement.phase.VulcanPhase;
import keystrokesmod.module.impl.movement.phase.WatchdogAutoPhase;
import keystrokesmod.module.setting.impl.*;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class Phase extends Module {
    private final ModeValue mode;
    private final ButtonSetting cancelS08;

    public Phase() {
        super("Phase", category.movement);
        this.registerSetting(new DescriptionSetting("Lets you go through solid blocks."));
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new LiteralSubMode("Normal", this))
                .add(new WatchdogAutoPhase("Watchdog Auto Phase", this))
                .add(new VulcanPhase("Vulcan", this))
        );
        this.registerSetting(cancelS08 = new ButtonSetting("Cancel S08", false));
    }

    @Override
    public void onEnable() {
        mode.enable();
    }

    @Override
    public void onDisable() {
        mode.disable();
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook && cancelS08.isToggled())
            event.setCanceled(true);
    }
}
