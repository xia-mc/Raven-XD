package keystrokesmod.module.impl.player;

import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.PacketUtils;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FakePotion extends Module {
    private final ButtonSetting speed;
    private final SliderSetting speedLevel;
    private final ButtonSetting jump;
    private final SliderSetting jumpLevel;
    private final ButtonSetting delayTransaction;

    private final Queue<Packet<?>> delayedPackets = new ConcurrentLinkedQueue<>();

    public FakePotion() {
        super("FakePotion", category.player);
        this.registerSetting(speed = new ButtonSetting("Speed", false));
        this.registerSetting(speedLevel = new SliderSetting("Speed level", 1, 1, 5, 1, speed::isToggled));
        this.registerSetting(jump = new ButtonSetting("Jump boost", false));
        this.registerSetting(jumpLevel = new SliderSetting("Jump boost level", 1, 1, 5, 1, jump::isToggled));
        this.registerSetting(delayTransaction = new ButtonSetting("Delay transaction", false));
    }

    @Override
    public void onUpdate() {
        if (speed.isToggled()) {
            mc.thePlayer.addPotionEffect(
                    new PotionEffect(Potion.moveSpeed.getId(), 999999, (int) speedLevel.getInput() - 1, false, false)
            );
        }
        if (jump.isToggled()) {
            mc.thePlayer.addPotionEffect(
                    new PotionEffect(Potion.jump.getId(), 999999, (int) jumpLevel.getInput() - 1, false, false)
            );
        }
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent event) {
        if (delayTransaction.isToggled() && event.getPacket() instanceof C0FPacketConfirmTransaction) {
            event.setCanceled(true);
            delayedPackets.add(event.getPacket());
        }
    }

    @Override
    public void onDisable() {
        mc.thePlayer.removePotionEffect(Potion.moveSpeed.getId());
        mc.thePlayer.removePotionEffect(Potion.jump.getId());

        if (!delayedPackets.isEmpty()) {
            for (Packet<?> p : delayedPackets) {
                PacketUtils.sendPacket(p);
            }
        }
        delayedPackets.clear();
    }
}
