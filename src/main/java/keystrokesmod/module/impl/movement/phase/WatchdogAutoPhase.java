package keystrokesmod.module.impl.movement.phase;

import keystrokesmod.event.BlockAABBEvent;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.event.WorldChangeEvent;
import keystrokesmod.module.impl.movement.Phase;
import keystrokesmod.module.impl.player.blink.NormalBlink;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.CoolDown;
import keystrokesmod.utility.Utils;
import net.minecraft.block.BlockGlass;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class WatchdogAutoPhase extends SubMode<Phase> {
    private final CoolDown stopWatch = new CoolDown(4000);
    private final NormalBlink blink = new NormalBlink("Blink", this);
    private boolean phase;

    public WatchdogAutoPhase(String name, Phase parent) {
        super(name, parent);
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        if (phase && !stopWatch.hasFinished())
            blink.enable();
    }

    @SubscribeEvent
    public void onBlockAABBEvent(BlockAABBEvent event) {
        if (phase && event.getBlock() instanceof BlockGlass) event.setBoundingBox(null);
    }

    @SubscribeEvent
    public void onWorldChange(WorldChangeEvent event) {
        onDisable();
    }

    @SubscribeEvent
    public void onPacketReceiveEvent(@NotNull ReceivePacketEvent event) {
        Packet<?> packet = event.getPacket();

        if (packet instanceof S02PacketChat) {
            S02PacketChat s02PacketChat = ((S02PacketChat) packet);
            String chat = s02PacketChat.getChatComponent().getUnformattedText();

            switch (chat) {
                case "Cages opened! FIGHT!":
                case "§r§r§r                               §r§f§lSkyWars Duel§r":
                case "§r§eCages opened! §r§cFIGHT!§r":
                    onDisable();
                    break;

                case "The game starts in 3 seconds!":
                case "§r§e§r§eThe game starts in §r§a§r§c3§r§e seconds!§r§e§r":
                case "§r§eCages open in: §r§c3 §r§eseconds!§r":
                    if (Utils.isSkyWars()) {
                        phase = true;
                        stopWatch.start();
                    }
                    break;
            }
        }
    }

    @Override
    public void onDisable() {
        phase = false;
        blink.disable();
    }
}