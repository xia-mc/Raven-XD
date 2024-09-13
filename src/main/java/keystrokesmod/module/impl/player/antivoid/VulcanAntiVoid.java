package keystrokesmod.module.impl.player.antivoid;

import keystrokesmod.event.BlockAABBEvent;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.impl.player.AntiVoid;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.block.BlockAir;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class VulcanAntiVoid extends SubMode<AntiVoid> {
    private final SliderSetting distance;

    private boolean fallDistanced = false;

    public VulcanAntiVoid(String name, @NotNull AntiVoid parent) {
        super(name, parent);
        this.registerSetting(distance = new SliderSetting("Distance", 5, 0, 10, 1));
    }

    @SubscribeEvent
    public void onAABB(BlockAABBEvent event) {
        if (!Utils.nullCheck()) return;
        if (mc.thePlayer.fallDistance > distance.getInput())
            fallDistanced = true;
        if (fallDistanced && event.getBlockPos().getY() < mc.thePlayer.posY) {
            if (BlockUtils.getBlock(event.getBlockPos()) instanceof BlockAir) {
                final double x = event.getBlockPos().getX(), y = event.getBlockPos().getY(), z = event.getBlockPos().getZ();
                event.setBoundingBox(AxisAlignedBB.fromBounds(-15, -1, -15, 15, 1, 15).offset(x, y, z));
            } else {
                fallDistanced = false;
            }
        }
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), false);
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
        }
    }
}
