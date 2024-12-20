package keystrokesmod.module.impl.movement;

import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.movement.speed.*;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeValue;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class Speed extends Module {
    private final ModeValue mode;
    private final ButtonSetting liquidDisable;
    private final ButtonSetting sneakDisable;
    private final ButtonSetting invMoveDisable;
    private final SliderSetting tempDisableOnFlag;
    private final ButtonSetting stopMotion;
    public int offGroundTicks = 0;
    private int disableTicks = 0;

    public Speed() {
        super("Speed", Module.category.movement);
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new LegitSpeed("Legit", this))
                .add(new HypixelSpeed("Hypixel", this))
                .add(new VanillaSpeed("Vanilla", this))
                .add(new BlocksMCSpeed("BlocksMC", this))
                .add(new VulcanSpeed("Vulcan", this))
                .add(new GrimACSpeed("GrimAC", this))
                .add(new StrafeSpeed("Strafe", this))
        );
        this.registerSetting(liquidDisable = new ButtonSetting("Disable in liquid", true));
        this.registerSetting(sneakDisable = new ButtonSetting("Disable while sneaking", true));
        this.registerSetting(invMoveDisable = new ButtonSetting("Disable while InvMove", false));
        this.registerSetting(tempDisableOnFlag = new SliderSetting("Temp disable on flag", 0, 3, 5, 0.1, "s"));
        this.registerSetting(stopMotion = new ButtonSetting("Stop motion", false));
    }

    @Override
    public String getInfo() {
        return mode.getSubModeValues().get((int) mode.getInput()).getInfo();
    }

    @Override
    public void onEnable() {
        mode.enable();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer.onGround) {
            offGroundTicks = 0;
        } else {
            offGroundTicks++;
        }

        if (disableTicks > 0)
            disableTicks--;
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            disableTicks += (int) (tempDisableOnFlag.getInput() * 20);
        }
    }

    public boolean noAction() {
        return !Utils.nullCheck()
                || ((mc.thePlayer.isInWater() || mc.thePlayer.isInLava()) && liquidDisable.isToggled())
                || (mc.thePlayer.isSneaking() && sneakDisable.isToggled())
                || (ModuleManager.invMove.isEnabled() && ModuleManager.invMove.canInvMove() && invMoveDisable.isToggled())
                || disableTicks > 0;
    }

    @Override
    public void onDisable() {
        mode.disable();

        if (stopMotion.isToggled()) {
            MoveUtil.stop();
        }
        Utils.resetTimer();
        disableTicks = 0;
    }
}
