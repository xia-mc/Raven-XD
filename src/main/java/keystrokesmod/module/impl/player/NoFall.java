package keystrokesmod.module.impl.player;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.*;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static keystrokesmod.module.ModuleManager.blink;
import static keystrokesmod.module.ModuleManager.scaffold;

public class NoFall extends Module {
    public final ModeSetting mode;
    private final SliderSetting minFallDistance;
    private final ButtonSetting ignoreVoid;
    private final String[] modes = new String[]{"Spoof", "Extra", "NoGround", "Blink", "Matrix", "Vulcan"};

    // for blink noFall
    private boolean blinked = false;
    private boolean prevOnGround = false;
    private double fallDistance = 0;

    // for vulcan noFall
    public static final List<Integer> maxModCount = new ArrayList<>(Arrays.asList(3, 2, 2));
    private int currentModCount = 0;
    private int hasModCount = 0;

    public NoFall() {
        super("NoFall", category.player);
        this.registerSetting(mode = new ModeSetting("Mode", modes, 0));
        this.registerSetting(minFallDistance = new SliderSetting("Minimum fall distance", 3.0, 0.0, 8.0, 0.1));
        this.registerSetting(ignoreVoid = new ButtonSetting("Ignore void", true));
    }

    @Override
    public void onEnable() {
        this.fallDistance = mc.thePlayer.fallDistance;
    }

    @Override
    public void onDisable() {
        if (blinked) {
            blink.disable();
            blinked = false;
        }
        Utils.resetTimer();
    }

    public void onUpdate() {
        if (mode.getInput() == 5 && mc.thePlayer.onGround && currentModCount > 0) {
            hasModCount++;
            currentModCount = 0;
        }
    }

    @SubscribeEvent
    public void onPacketSend(@NotNull SendPacketEvent event) {
        if (mode.getInput() == 5) {
            if (event.getPacket() instanceof C03PacketPlayer && mc.thePlayer.fallDistance > minFallDistance.getInput()) {
                Utils.sendMessage(String.format("currentMod: %s  hasMod: %s", currentModCount, hasModCount));
                if (currentModCount > maxModCount.get(hasModCount % maxModCount.size())) {
                    return;
                }

                try {
                    Reflection.C03PacketPlayerOnGround.set(event.getPacket(), true);
                    mc.thePlayer.fallDistance = 0;
                    mc.thePlayer.setVelocity(0, 0, 0);
                    currentModCount++;
                } catch (IllegalAccessException ignored) {
                }
            }
        }
    }


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPreMotion(PreMotionEvent e) {
        if (ignoreVoid.isToggled() && isVoid()) {
            return;
        }
        if ((double) mc.thePlayer.fallDistance > minFallDistance.getInput() || minFallDistance.getInput() == 0) {
            if ((int) mode.getInput() == 0) {
                e.setOnGround(true);
            }
        }
    }

    @SubscribeEvent
    public void onPreMotionEvent(PreMotionEvent event) {
        Utils.resetTimer();
        if (mc.thePlayer.onGround) {
            this.fallDistance = 0.0;
        } else if (mc.thePlayer.motionY < 0.0) {
            this.fallDistance -= mc.thePlayer.motionY;
        }

        if (mc.thePlayer.capabilities.allowFlying) return;
        if (ignoreVoid.isToggled() && isVoid()) {
            if (blinked) {
                blink.disable();
                blinked = false;
            }
            return;
        }
        switch ((int) mode.getInput()) {
            case 1:
                float extra$fallDistance = 0;
                try {
                    extra$fallDistance = Reflection.EntityFallDistance.getFloat(mc.thePlayer);
                } catch (Exception exception) {
                    Utils.sendMessage("&cFailed to get fall distance.");
                }
                if (extra$fallDistance > minFallDistance.getInput()) {
                    Utils.getTimer().timerSpeed = (float) 0.5;
                    mc.getNetHandler().addToSendQueue(new C03PacketPlayer(true));
                    try {
                        Reflection.EntityFallDistance.setFloat(mc.thePlayer, 0);
                    } catch (Exception exception) {
                        Utils.sendMessage("&cFailed to set fall distance to 0.");
                    }
                }
                break;
            case 2:
                event.setOnGround(false);
                break;
            case 3:
                if (mc.thePlayer.onGround) {
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
                break;
            case 4:
                if (BlockUtils.isBlockUnder()) {
                    if (fallDistance > 2) {
                        MoveUtil.strafe(0.19);
                    }

                    if (fallDistance > 3 && MoveUtil.speed() < 0.2) {
                        event.setOnGround(true);
                        fallDistance = 0;
                    }
                }
                break;
        }
    }

    @Override
    public String getInfo() {
        return modes[(int) mode.getInput()];
    }

    private boolean isVoid() {
        return Utils.overVoid(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
    }

    private boolean shouldBlink() {
        return !mc.thePlayer.onGround && !BlockUtils.isBlockUnder((int) Math.floor(minFallDistance.getInput())) && BlockUtils.isBlockUnder() && !scaffold.isEnabled();
    }
}
