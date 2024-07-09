package keystrokesmod.module.impl.other;

import io.netty.buffer.Unpooled;
import keystrokesmod.Raven;
import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;


public class ClientSpoofer extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", SpoofMode.getNames(), 0);
    public final ButtonSetting cancelForgePacket = new ButtonSetting("Cancel Forge packet", false);
    private static SpoofMode currentSpoof = SpoofMode.FORGE;

    public ClientSpoofer() {
        super("ClientSpoofer", category.other);
        this.registerSetting(mode, cancelForgePacket);
    }
    public void onDisable() {
        currentSpoof = SpoofMode.FORGE;
    }
    @Override
    public void onUpdate() {
        currentSpoof = SpoofMode.values()[(int) mode.getInput()];
        if(currentSpoof == SpoofMode.FORGE) {
            cancelForgePacket.disable();
        } else {
            Raven.getModuleManager().getModule("ModSpoofer").enable();
        }
    }

    @SubscribeEvent
    public void onSendPacket(@NotNull SendPacketEvent event) {
        if (event.getPacket() instanceof FMLProxyPacket && cancelForgePacket.isToggled()) {
            event.setCanceled(true);
        }
    }


    public static BrandInfo getBrandName() {
        return new BrandInfo(currentSpoof.brand, currentSpoof.channel);
    }


    public static class BrandInfo {
        public final String brand;
        public final String channel;

        public BrandInfo(String brand, String channel) {
            this.brand = brand;
            this.channel = channel;
        }
    }

    enum SpoofMode {
        FORGE("Forge", "FML,Forge", "MC|Brand"),
        VANILLA("Vanilla", "vanilla", "MC|Brand"),
        LUNAR("Lunar", "lunarclient:v2.16.0-2426", "MC|Brand"),
        CHEATBREAKER("Cheatbreaker", "CB", "MC|Brand"),
        GEYSER("Geyser", "Geyser", "MC|Brand"),
        LABYMOD("LabyMod", "LMC", "MC|Brand"),;

        public final String name;
        public final String brand;
        public final String channel;

        SpoofMode(String name, String brand, String channel) {
            this.name = name;
            this.brand = brand;
            this.channel = channel;
        }

        public static String @NotNull [] getNames() {
            return java.util.Arrays.stream(values()).map(spoofMode -> spoofMode.name).toArray(String[]::new);
        }
    }
}