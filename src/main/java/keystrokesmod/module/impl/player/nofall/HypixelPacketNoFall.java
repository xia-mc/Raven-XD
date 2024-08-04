package keystrokesmod.module.impl.player.nofall;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.module.impl.combat.KillAura;
import keystrokesmod.module.impl.player.NoFall;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.Utils;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class HypixelPacketNoFall extends SubMode<NoFall> {
    private final SliderSetting minFallDistance;
    private final ButtonSetting notWhileKillAura;

    private float fallDistance = 0;
    private boolean timed = false;

    public HypixelPacketNoFall(String name, @NotNull NoFall parent) {
        super(name, parent);
        this.registerSetting(minFallDistance = new SliderSetting("Minimum fall distance", 3.0, 0.0, 8.0, 0.1));
        this.registerSetting(notWhileKillAura = new ButtonSetting("Not while killAura", true));
    }

    @Override
    public void onDisable() {
        fallDistance = 0;
        if (timed)
            Utils.resetTimer();
        timed = false;
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer.onGround)
            fallDistance = 0;
        else
            fallDistance += (float) Math.min(mc.thePlayer.motionY, 0);

        if (fallDistance >= minFallDistance.getInput() && !parent.noAction() && !(notWhileKillAura.isToggled() && KillAura.target != null)) {
            Utils.getTimer().timerSpeed = (float) 0.5;
            mc.getNetHandler().addToSendQueue(new C03PacketPlayer(true));
            fallDistance = 0;
            timed = true;
        } else if (timed) {
            Utils.resetTimer();
            timed = false;
        }
    }
}
