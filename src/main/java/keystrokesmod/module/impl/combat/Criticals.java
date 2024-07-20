package keystrokesmod.module.impl.combat;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.utility.Utils;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class Criticals extends Module {
    private final ModeSetting mode;
    private final SliderSetting fallTimer;
    private long lastTimer = -1;
    public static final String[] MODES = {"Alan34", "NoGround", "Timer"};
    private int ticksSinceVelocity = Integer.MAX_VALUE;

    public Criticals() {
        super("Criticals", category.combat);
        this.registerSetting(new DescriptionSetting("Makes you get a critical hit every time you attack."));
        this.registerSetting(mode = new ModeSetting("Mode", MODES, 0));
        this.registerSetting(fallTimer = new SliderSetting("Fall timer", 0.6, 0.2, 1.0, 0.1, new ModeOnly(mode, 2)));
    }

    @Override
    public void onEnable() {
        ticksSinceVelocity = Integer.MAX_VALUE;
    }

    @Override
    public void onUpdate() {
        if (ticksSinceVelocity < Integer.MAX_VALUE) ticksSinceVelocity++;
    }

    @SubscribeEvent
    public void onPacketReceive(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S12PacketEntityVelocity) {
            if (((S12PacketEntityVelocity) event.getPacket()).getEntityID() == mc.thePlayer.getEntityId()) {
                ticksSinceVelocity = 0;
            }
        }
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if (mode.getInput() != 2) return;
        if (mc.thePlayer.onGround || mc.thePlayer.fallDistance < 0.05) return;
        if (lastTimer != -1) return;

        Utils.getTimer().timerSpeed = (float) fallTimer.getInput();
        lastTimer = System.currentTimeMillis();
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        switch ((int) mode.getInput()) {
            case 0:  // Alan34
                if (ticksSinceVelocity <= 80 && mc.thePlayer.fallDistance < 1.8) {
                    event.setOnGround(false);
                }
                break;
            case 1:  // NoGround
                if (KillAura.target != null) {
                    event.setOnGround(false);
                }
                break;
            case 2:  // Timer
                if (lastTimer != -1 && (mc.thePlayer.onGround || System.currentTimeMillis() - lastTimer > 2000)) {
                    Utils.resetTimer();
                    lastTimer = -1;
                }
                break;
        }
    }

    @Override
    public String getInfo() {
        return MODES[(int) mode.getInput()];
    }
}