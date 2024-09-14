package keystrokesmod.module.impl.player.antivoid;

import keystrokesmod.module.impl.player.AntiVoid;
import keystrokesmod.module.impl.player.blink.NormalBlink;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import org.jetbrains.annotations.NotNull;

public class BacktrackAntiVoid extends SubMode<AntiVoid> {
    private final NormalBlink blink = new NormalBlink("Blink", this);
    private final SliderSetting distance;
    private final ButtonSetting includeMotion;
    private final ButtonSetting clearXZMotion;

    private Vec3 fallbackPosition = null;
    private Vec3 fallbackMotion = null;

    public BacktrackAntiVoid(String name, @NotNull AntiVoid parent) {
        super(name, parent);
        this.registerSetting(blink.getSettings());
        this.registerSetting(distance = new SliderSetting("Distance", 5, 0, 10, 1));
        this.registerSetting(includeMotion = new ButtonSetting("Include motion", false));
        this.registerSetting(clearXZMotion = new ButtonSetting("Clear xz motion", true));
    }

    @Override
    public void onUpdate() throws Throwable {
        if (fallbackPosition == null) {
            if (predVoid() && MoveUtil.isMoving()) {
                fallbackPosition = new Vec3(mc.thePlayer);
                fallbackMotion = new Vec3(mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ);
                blink.enable();
            }
        } else {
            if (Utils.overVoid() && !mc.thePlayer.onGround) {
                if (mc.thePlayer.fallDistance > distance.getInput()) {
                    mc.thePlayer.setPosition(fallbackPosition.x, fallbackPosition.y, fallbackPosition.z);
                    if (includeMotion.isToggled()) {
                        mc.thePlayer.motionX = fallbackMotion.x;
                        mc.thePlayer.motionY = fallbackMotion.y;
                        mc.thePlayer.motionZ = fallbackMotion.z;
                    }
                    if (clearXZMotion.isToggled()) {
                        MoveUtil.stop();
                    }
                    fallbackPosition = null;
                    fallbackMotion = null;
                    synchronized (blink.blinkedPackets) {
                        for (Packet<?> p : blink.blinkedPackets) {
                            if (!(p instanceof C03PacketPlayer)) {
                                PacketUtils.sendPacketNoEvent(p);
                            }
                        }
                        blink.blinkedPackets.clear();
                    }
                    blink.disable();
                }
            } else if (!predVoid()) {
                blink.disable();
                fallbackPosition = null;
                fallbackMotion = null;
            }
        }
    }

    private static boolean predVoid() {
        return Utils.overVoid(mc.thePlayer.posX + mc.thePlayer.motionX, mc.thePlayer.posY + mc.thePlayer.motionY, mc.thePlayer.posZ + mc.thePlayer.motionZ);
    }

    @Override
    public void onDisable() throws Throwable {
        blink.disable();
        fallbackPosition = null;
    }
}
