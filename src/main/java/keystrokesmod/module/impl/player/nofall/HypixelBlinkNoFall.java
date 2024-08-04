package keystrokesmod.module.impl.player.nofall;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.module.impl.player.NoFall;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.BlockUtils;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import static keystrokesmod.module.ModuleManager.blink;
import static keystrokesmod.module.ModuleManager.scaffold;

public class HypixelBlinkNoFall extends SubMode<NoFall> {
    private final SliderSetting minFallDistance;

    private boolean blinked = false;
    private boolean prevOnGround = false;
    private double fallDistance = 0;

    public HypixelBlinkNoFall(String name, @NotNull NoFall parent) {
        super(name, parent);
        this.registerSetting(minFallDistance = new SliderSetting("Minimum fall distance", 3.0, 0.0, 8.0, 0.1));
    }

    @Override
    public void onDisable() {
        if (blinked) {
            blink.disable();
            blinked = false;
        }
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer.onGround || parent.noAction()) {
            if (blinked) {
                blink.disable();
                blinked = false;
            }

            this.prevOnGround = true;
        } else if (this.prevOnGround) {
            if (shouldBlink()) {
                blink.enable();
                blinked = true;
            }

            prevOnGround = false;
        } else if (BlockUtils.isBlockUnder() && blink.isEnabled() && (this.fallDistance - mc.thePlayer.motionY) >= minFallDistance.getInput()) {
            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
            this.fallDistance = 0.0F;
        }
    }

    private boolean shouldBlink() {
        return !mc.thePlayer.onGround && !BlockUtils.isBlockUnder((int) Math.floor(minFallDistance.getInput())) && BlockUtils.isBlockUnder() && !scaffold.isEnabled();
    }
}
