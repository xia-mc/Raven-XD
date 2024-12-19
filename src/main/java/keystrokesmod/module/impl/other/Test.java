package keystrokesmod.module.impl.other;

import keystrokesmod.module.Module;
import keystrokesmod.utility.PacketUtils;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;

public class Test extends Module {
    public Test() {
        super("Test", category.experimental);
    }

    @Override
    public void onEnable() throws Throwable {
        PacketUtils.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.mainInventory[4]));
    }
}
