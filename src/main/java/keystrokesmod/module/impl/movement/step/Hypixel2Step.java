package keystrokesmod.module.impl.movement.step;

import keystrokesmod.event.PostPlayerInputEvent;
import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.impl.movement.Step;
import keystrokesmod.module.impl.other.anticheats.utils.phys.Vec2;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.BlockUtils;
import net.minecraft.block.BlockAir;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class Hypixel2Step extends SubMode<Step> {
    private boolean step;

    public Hypixel2Step(String name, Step parent) {
        super(name, parent);
    }


    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer.isCollidedHorizontally && mc.thePlayer.onGround) {
            step = true;
        }

        if (shouldStep()) {
            mc.thePlayer.stepHeight = 1.0F;
        } else {
            mc.thePlayer.stepHeight = 0.6F;
        }

    }

    public boolean shouldStep() {
        if (!mc.thePlayer.onGround || !mc.thePlayer.isCollidedHorizontally) {
            return false;
        }

        Vec2[] steps = {new Vec2(0, 1), new Vec2(1, 0), new Vec2(0, -1), new Vec2(-1, 0)};

        for (Vec2 step : steps) {
            if (!(BlockUtils.blockRelativeToPlayer(step.x, 0, step.y) instanceof BlockAir) && BlockUtils.blockRelativeToPlayer(step.x, 1, step.y) instanceof BlockAir) {
                return true;
            }
        }

        return false;
    }

    @SubscribeEvent
    public void onSendPacket(@NotNull SendPacketEvent event) {
        final double[] values = new double[] {0.42, 0.75, 1.0};

        if (event.getPacket() instanceof C03PacketPlayer.C04PacketPlayerPosition && shouldStep()) {
            for (double value : values) {
                event.setPacket(new C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX - value, mc.thePlayer.posY + value, mc.thePlayer.posZ + value, false));
            }
        }

    }

    @SubscribeEvent
    public void onPostPlayerInput(PostPlayerInputEvent event) {
        if (step) {
            mc.thePlayer.jump();
            step = false;
        }
    }
}