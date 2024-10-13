package keystrokesmod.module.impl.other;

import keystrokesmod.Raven;
import keystrokesmod.event.WorldChangeEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.other.anticheats.PlayerManager;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import lombok.Getter;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

/**
 * @author xia__mc
 * @see <a href="https://github.com/Nova-Committee/CheatDetector">CheatDetector Github</a>
 */
public class Anticheat extends Module {
    @Getter
    private static SliderSetting latency;
    @Getter
    private static SliderSetting threshold;
    @Getter
    private static ButtonSetting disableInLobby;
    @Getter
    private static ButtonSetting checkForSelf;
    @Getter
    private static ButtonSetting checkForTeammates;
    @Getter
    private static SliderSetting vlClearTime;
    @Getter
    private static ButtonSetting noAlertBuffer;
    @Getter
    private static ButtonSetting shouldPing;
    @Getter
    private static ModeSetting pingSound;
    @Getter
    private static ModeSetting autoReport;
    @Getter
    private static ButtonSetting experimentalMode;
    @Getter
    private static ButtonSetting aimCheck;
    @Getter
    private static ButtonSetting combatCheck;
    @Getter
    private static ButtonSetting movementCheck;
    @Getter
    private static ButtonSetting scaffoldingCheck;
    @Getter
    private static ButtonSetting simulationCheck;

    private PlayerManager manager = new PlayerManager();
    public Anticheat() {
        super("Anticheat", category.other);
        this.registerSetting(new DescriptionSetting("Tries to detect cheaters."));
        this.registerSetting(latency = new SliderSetting("Latency compensation", 600.0, 0.0, 1000.0, 1.0, "ms"));
        this.registerSetting(threshold = new SliderSetting("Movement threshold", 1.0, 0.0, 3.0, 0.01, "blocks"));
        this.registerSetting(disableInLobby = new ButtonSetting("Disable in lobby", true));
        this.registerSetting(checkForSelf = new ButtonSetting("Check for self", true));
        this.registerSetting(checkForTeammates = new ButtonSetting("Check for teammates", true));
        this.registerSetting(vlClearTime = new SliderSetting("VL clear time", 6000, -1, 12000, 1, "ticks"));
        this.registerSetting(noAlertBuffer = new ButtonSetting("Remove alert buffer", false));
        this.registerSetting(shouldPing = new ButtonSetting("Should ping", true));
        this.registerSetting(pingSound = new ModeSetting("Ping sound", new String[]{"Note", "Augustus"}, 0, shouldPing::isToggled));
        this.registerSetting(autoReport = new ModeSetting("Auto report", new String[]{"None", "/wdr", "/report"}, 0));
        this.registerSetting(experimentalMode = new ButtonSetting("Experimental mode", false));
        this.registerSetting(aimCheck = new ButtonSetting("Aim checks", true));
        this.registerSetting(combatCheck = new ButtonSetting("Combat checks", true));
        this.registerSetting(movementCheck = new ButtonSetting("Movement checks", true));
        this.registerSetting(scaffoldingCheck = new ButtonSetting("Scaffolding checks", true));
        this.registerSetting(simulationCheck = new ButtonSetting("Simulation checks", false));
    }

    public void onUpdate() {
        manager.update(Raven.mc);
    }

    @SubscribeEvent
    public void onEntityJoin(@NotNull WorldChangeEvent e) {
        manager = null;
        manager = new PlayerManager();
    }

    @Override
    public void onDisable() throws Throwable {
        //noinspection SynchronizeOnNonFinalField
        synchronized (manager) {
            manager.dataMap.values().forEach(trPlayer -> trPlayer.manager.onCustomAction(MinecraftForge.EVENT_BUS::unregister));
            manager = null;
        }
    }

    @Override
    public void onEnable() throws Throwable {
        manager = new PlayerManager();
    }
}
