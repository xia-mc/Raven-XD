package keystrokesmod.module.impl.combat.criticals;

import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.combat.Criticals;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LagCriticals extends SubMode<Criticals> {
    private final SliderSetting maxLagTime;
    private final SliderSetting chance;
    private final ButtonSetting stopOnHurt;

    private final Queue<Packet<?>> delayedPackets = new ConcurrentLinkedQueue<>();
    private long startLag = -1;
    private boolean delayed = false;

    public LagCriticals(String name, @NotNull Criticals parent) {
        super(name, parent);
        this.registerSetting(maxLagTime = new SliderSetting("Max lag time", 1000, 100, 2000, 100));
        this.registerSetting(chance = new SliderSetting("Chance", 90, 0, 100, 1, "%"));
        this.registerSetting(stopOnHurt = new ButtonSetting("Stop on hurt", true));
    }

    @Override
    public void onDisable() {
        if (startLag != -1)
            reset();
        startLag = -1;
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent event) {
        if (startLag != -1) {
            if (mc.thePlayer.onGround || delayed
                    || (mc.thePlayer.hurtTime > 0 && stopOnHurt.isToggled())
                    || System.currentTimeMillis() - startLag > maxLagTime.getInput()) {
                reset();
                startLag = -1;
            }
        } else if (mc.thePlayer.motionY < 0 && !mc.thePlayer.onGround && !delayed) {
            if (delayedPackets.isEmpty() && chance.getInput() != 100 && Math.random() * 100 > chance.getInput()) {
                delayed = true;
                return;
            }

            if (Utils.isTargetNearby(ModuleManager.killAura.isEnabled() ? ModuleManager.killAura.attackRange.getInput() : 3)) {
                startLag = System.currentTimeMillis();
                event.setCanceled(true);
                delayedPackets.add(event.getPacket());
            }
        } else if (mc.thePlayer.onGround) {
            delayed = false;
        }
    }

    private void reset() {
        for (Packet<?> p : delayedPackets) {
            PacketUtils.sendPacketNoEvent(p);
        }
        delayedPackets.clear();
    }
}
