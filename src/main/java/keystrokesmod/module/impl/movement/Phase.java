package keystrokesmod.module.impl.movement;

import keystrokesmod.event.BlockAABBEvent;
import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.mixins.impl.client.PlayerControllerMPAccessor;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.BlockPos;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import static keystrokesmod.module.ModuleManager.blink;

public class Phase extends Module {
    private final ModeSetting mode;
    private final ButtonSetting autoBlink;
    private final ButtonSetting cancelS08;
    private final ButtonSetting waitingBreakBlock;
    private final SliderSetting autoDisable;
    private final ButtonSetting exceptGround;

    private int phaseTime;

    // watchdog auto phase
    private boolean phase;

    private boolean currentHittingBlock;
    private boolean lastHittingBlock;

    public Phase() {
        super("Phase", category.movement);
        this.registerSetting(new DescriptionSetting("Lets you go through solid blocks."));
        this.registerSetting(mode = new ModeSetting("Mode", new String[]{"Normal", "Watchdog Auto Phase"}, 0));
        this.registerSetting(autoBlink = new ButtonSetting("Blink", true));
        this.registerSetting(cancelS08 = new ButtonSetting("Cancel S06", false));
        this.registerSetting(waitingBreakBlock = new ButtonSetting("waiting break block", false));
        this.registerSetting(autoDisable = new SliderSetting("Auto disable", 6, 1, 20, 1, "ticks"));
        this.registerSetting(exceptGround = new ButtonSetting("Except ground", false));
    }

    @Override
    public void onEnable() {
        phaseTime = 0;
        phase = false;
        currentHittingBlock = lastHittingBlock = false;
    }

    @Override
    public void onDisable() {
        if (autoBlink.isToggled())
            blink.disable();

        phaseTime = 0;
        phase = false;
    }

    @Override
    public void onUpdate() {
        currentHittingBlock = ((PlayerControllerMPAccessor) mc.playerController).isHittingBlock();
        lastHittingBlock = currentHittingBlock;
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (this.phase) {
            this.phaseTime++;
        } else {
            this.phaseTime = 0;
        }

        if (phaseTime > autoDisable.getInput()) {
            disable();
            return;
        }

        if (waitingBreakBlock.isToggled() && !(lastHittingBlock && !currentHittingBlock)) {
            return;
        }

        switch ((int) mode.getInput()) {
            case 0:
                if (autoBlink.isToggled()) blink.enable();
                phase = true;
                break;
            case 1:
                if (this.phase && autoBlink.isToggled()) blink.enable();
                break;
        }
    }

    @SubscribeEvent
    public void onBlockAABB(BlockAABBEvent event) {
        if (this.phase) {
            if (exceptGround.isToggled() && event.getBlockPos().equals(new BlockPos(mc.thePlayer).down()))
                return;
            event.setBoundingBox(null);
        }
    }

    @SubscribeEvent
    public void onWorldChange(@NotNull WorldEvent.Load event) {
        this.phase = false;
        this.phaseTime = 0;
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (cancelS08.isToggled()) {
            if (event.getPacket() instanceof S08PacketPlayerPosLook) {
                event.setCanceled(true);
            }
        }

        if ((int) mode.getInput() == 1) {
            if (event.getPacket() instanceof S02PacketChat) {
                S02PacketChat packet = (S02PacketChat) event.getPacket();
                String chat = packet.getChatComponent().getUnformattedText();

                if (chat.contains(" 2 ") && chat.contains("game")) {
                    this.phase = true;
                } else if (chat.contains("FIGHT") && chat.contains("Cages")) {
                    this.phase = false;
                    disable();
                }
            }
        }
    }
}
