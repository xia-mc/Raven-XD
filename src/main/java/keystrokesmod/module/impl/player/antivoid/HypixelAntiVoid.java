package keystrokesmod.module.impl.player.antivoid;

import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.module.impl.player.AntiVoid;
import keystrokesmod.module.impl.player.blink.NormalBlink;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import lombok.Getter;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static keystrokesmod.module.ModuleManager.*;

public class HypixelAntiVoid extends SubMode<AntiVoid> {
    @Getter
    private static @Nullable HypixelAntiVoid instance = null;
    public final NormalBlink blink = new NormalBlink("Blink", this);
    private final SliderSetting distance;
    private final ButtonSetting toggleScaffold;
    private Vec3 position, motion;
    private boolean wasVoid, setBack;
    private int overVoidTicks;
    private boolean disabledForLongJump = false;

    public HypixelAntiVoid(String name, @NotNull AntiVoid parent) {
        super(name, parent);
        this.registerSetting(distance = new SliderSetting("Distance", 5, 0, 10, 1));
        this.registerSetting(toggleScaffold = new ButtonSetting("Toggle scaffold", false));
        instance = this;
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer.capabilities.allowFlying) return;
        if (mc.thePlayer.ticksExisted <= 50) return;

        if (disabledForLongJump && mc.thePlayer.onGround)
            disabledForLongJump = false;

        if (longJump.isEnabled())
            disabledForLongJump = true;
        if (scaffold.isEnabled() || fly.isEnabled() || disabledForLongJump) {
            blink.disable();
            return;
        }

        boolean overVoid = !mc.thePlayer.onGround && Utils.overVoid();

        if (overVoid) {
            overVoidTicks++;
        } else if (mc.thePlayer.onGround) {
            overVoidTicks = 0;
        }

        if (overVoid && position != null && motion != null && overVoidTicks < 30 + distance.getInput() * 20) {
            if (!setBack) {
                wasVoid = true;

                blink.enable();

                if (mc.thePlayer.fallDistance > distance.getInput() || setBack) {
                    PacketUtils.sendPacketNoEvent(new C03PacketPlayer.C04PacketPlayerPosition(position.xCoord, position.yCoord - 0.1 - Math.random(), position.zCoord, false));
                    if (this.toggleScaffold.isToggled()) {
                        scaffold.enable();
                    }

                    blink.blinkedPackets.clear();

                    mc.thePlayer.fallDistance = 0;

                    setBack = true;
                }
            } else {
                blink.disable();
            }
        } else {
            setBack = false;

            if (wasVoid) {
                blink.disable();
                wasVoid = false;
            }

            motion = new Vec3(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ);
            position = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        }
    }
}
