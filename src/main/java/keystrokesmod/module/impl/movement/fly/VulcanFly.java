package keystrokesmod.module.impl.movement.fly;

import keystrokesmod.event.BlockAABBEvent;
import keystrokesmod.event.ReceivePacketEvent;
import keystrokesmod.module.impl.movement.Fly;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class VulcanFly extends SubMode<Fly> {
    private final ButtonSetting timer;
    private final SliderSetting timerSpeed;

    private boolean shouldTimer = false;

    public VulcanFly(String name, @NotNull Fly parent) {
        super(name, parent);
        this.registerSetting(timer = new ButtonSetting("Timer", false));
        this.registerSetting(timerSpeed = new SliderSetting("Timer speed", 2, 1.5, 5, 0.5, "x"));
    }

    @Override
    public void onDisable() {
        Utils.resetTimer();
    }

    @SubscribeEvent
    public void onBlockAABB(@NotNull BlockAABBEvent event) {
        if (BlockUtils.replaceable(event.getBlockPos())) {
            final double x = event.getBlockPos().getX(), y = event.getBlockPos().getY(), z = event.getBlockPos().getZ();

            if (y < mc.thePlayer.posY) {
                event.setBoundingBox(AxisAlignedBB.fromBounds(-15, -1, -15, 15, 1, 15).offset(x, y, z));
            }
        }
    }

    @Override
    public void onUpdate() {
        if (shouldTimer && timer.isToggled())
            Utils.getTimer().timerSpeed = (float) (timerSpeed.getInput() - Math.random() / 1000);
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        if (event.getPacket() instanceof S08PacketPlayerPosLook) {
            event.setCanceled(true);
            shouldTimer = true;
        }
    }

    @Override
    public void onEnable() {
        shouldTimer = false;
    }
}
