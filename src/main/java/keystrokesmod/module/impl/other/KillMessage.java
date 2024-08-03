package keystrokesmod.module.impl.other;

import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.utility.PacketUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.network.play.server.S13PacketDestroyEntities;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class KillMessage extends Module {
    public static String killMessage = "This is a custom killMessage";

    private final ModeSetting mode;

    private EntityPlayer lastAttack = null;
    private long lastAttackTime = -1;

    public KillMessage() {
        super("KillMessage", category.other);
        this.registerSetting(mode = new ModeSetting("Mode", new String[]{"yby02", "Custom"}, 0));
    }

    @SubscribeEvent
    public void onAttack(@NotNull AttackEntityEvent event) {
        if (event.target instanceof EntityPlayer) {
            lastAttack = (EntityPlayer) event.target;
            lastAttackTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onUpdate() {
        if (System.currentTimeMillis() - lastAttackTime > 20)
            lastAttack = null;
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (lastAttack != null && event.getPacket() instanceof S13PacketDestroyEntities) {
            S13PacketDestroyEntities packet = (S13PacketDestroyEntities) event.getPacket();
            for (int id : packet.getEntityIDs()) {
                if (id == lastAttack.getEntityId()) {
                    PacketUtils.sendPacket(new C01PacketChatMessage(getKillMessage()));
                }
            }
        }
    }

    private String getKillMessage() {
        switch ((int) mode.getInput()) {
            case 0:
                return "你们好，我叫Esound。你已经被02开发的Neverlose击杀";
            case 1:
                return killMessage;
        }
        return "";
    }
}
