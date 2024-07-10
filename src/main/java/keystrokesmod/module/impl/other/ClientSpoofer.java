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
    }
    @SubscribeEvent
    public void onSendPacket(@NotNull SendPacketEvent event) {
        if (event.getPacket() instanceof FMLProxyPacket && cancelForgePacket.isToggled()) {
            event.setCanceled(true);
        }
    }

    public static BrandInfo getBrandName() {
        return new BrandInfo(currentSpoof.brand);
    }


    public static class BrandInfo {
        public final String brand;

        public BrandInfo(String brand) {
            this.brand = brand;
        }
    }

    enum SpoofMode {
        FORGE("Forge", "FML,Forge"),
        VANILLA("Vanilla", "vanilla"),
        LUNAR("Lunar", "lunarclient:v2.16.0-2426"),
        CHEATBREAKER("Cheatbreaker", "CB"),
        GEYSER("Geyser", "Geyser"),
        LABYMOD("LabyMod", "LMC"),;

        public final String name;
        public final String brand;

        SpoofMode(String name, String brand) {
            this.name = name;
            this.brand = brand;
        }

        public static String @NotNull [] getNames() {
            return java.util.Arrays.stream(values()).map(spoofMode -> spoofMode.name).toArray(String[]::new);
        }
    }
}