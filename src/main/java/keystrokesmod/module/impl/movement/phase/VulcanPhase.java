package keystrokesmod.module.impl.movement.phase;

import keystrokesmod.event.*;
import keystrokesmod.module.impl.client.Notifications;
import keystrokesmod.module.impl.movement.Phase;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class VulcanPhase extends SubMode<Phase> {
    private final ButtonSetting fast = new ButtonSetting("Fast", false);
    private final ButtonSetting cancelVelocity = new ButtonSetting("Cancel velocity", true);

    private boolean teleport = false;

    private boolean enable = true;
    private int timer1 = 0;
    private boolean flag = true;
    private boolean yMoving = false;

    public VulcanPhase(String name, Phase parent) {
        super(name, parent);
        this.registerSetting(fast, cancelVelocity);
    }
    @Override
    public void onEnable() {
        flag = true;
        timer1 = 0;
        teleport = false;
        enable = true;
        if( mc.thePlayer.onGround ){
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - 1, mc.thePlayer.posZ);
            MoveUtil.stop();
        } else{
            Notifications.sendNotification(Notifications.NotificationTypes.INFO, "You must me on the ground to do this");
            parent.toggle();
        }
        yMoving = false;
    }

    /**
     * Checks if the player is inside a block
     *
     * @return inside block
     */
    public boolean insideBlock() {
        if (mc.thePlayer.ticksExisted < 5) {
            return false;
        }

        final EntityPlayerSP player = mc.thePlayer;
        final WorldClient world = mc.theWorld;
        final AxisAlignedBB bb = player.getEntityBoundingBox();
        for (int x = MathHelper.floor_double(bb.minX); x < MathHelper.floor_double(bb.maxX) + 1; ++x) {
            for (int y = MathHelper.floor_double(bb.minY); y < MathHelper.floor_double(bb.maxY) + 1; ++y) {
                for (int z = MathHelper.floor_double(bb.minZ); z < MathHelper.floor_double(bb.maxZ) + 1; ++z) {
                    final Block block = world.getBlockState(new BlockPos(x, y, z)).getBlock();
                    final AxisAlignedBB boundingBox;
                    if (block != null && !(block instanceof BlockAir) && (boundingBox = block.getCollisionBoundingBox(world, new BlockPos(x, y, z), world.getBlockState(new BlockPos(x, y, z)))) != null && player.getEntityBoundingBox().intersectsWith(boundingBox)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        mc.thePlayer.cameraYaw = 0.1F;
        if( timer1 > 25) {

            if(insideBlock()) {
                event.setOnGround(false);
            }


        }

        if(insideBlock()){
            timer1++;
        }


        if((insideBlock() && !enable && flag)) {
            flag = false;
            Notifications.sendNotification(Notifications.NotificationTypes.INFO, "Phased");
        }
    }

    @SubscribeEvent
    public void onBlockAABB(BlockAABBEvent event) {
        if (insideBlock()) {
            if (!yMoving && event.getBlockPos().getY() < mc.thePlayer.posY) return;

            event.setBoundingBox(null);

            // Sets The Bounding Box To The Players Y Position.
            if (!(event.getBlock() instanceof BlockAir) && !mc.gameSettings.keyBindSneak.isKeyDown()) {
                final double x = event.getBlockPos().getX(), y = event.getBlockPos().getY(), z = event.getBlockPos().getZ();

                if (y < mc.thePlayer.posY) {
                    event.setBoundingBox(AxisAlignedBB.fromBounds(-15, -1, -15, 15, 1, 15).offset(x, y, z));
                }
            }
        } else if (!teleport){


            if (event.getBlock() instanceof BlockAir && !mc.thePlayer.isSneaking()) {
                final double x = event.getBlockPos().getX(), y = event.getBlockPos().getY(), z = event.getBlockPos().getZ();

                if (y < mc.thePlayer.posY) {
                    event.setBoundingBox(AxisAlignedBB.fromBounds(-15, -1, -15, 15, 1, 15).offset(x, y, z));
                }
            }
        } else if (!insideBlock()){
            Notifications.sendNotification(Notifications.NotificationTypes.INFO, "Disabled due to not being in a block");
            parent.toggle();
        }
    }

    @SubscribeEvent
    public void onStrafe(PrePlayerInputEvent event) {
        if(mc.gameSettings.keyBindJump.isKeyDown() && mc.thePlayer.hurtTime>0){
            mc.thePlayer.motionY=.99;
            yMoving = true;
        } else if (mc.gameSettings.keyBindSneak.isKeyDown()){
            mc.thePlayer.motionY=-.4;
            yMoving = true;
        } else if (!mc.gameSettings.keyBindJump.isKeyDown() && insideBlock() && timer1 > 25){
            yMoving = false;
        }
        if(fast.isToggled() && insideBlock()) {
            if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                event.setSpeed(((.0765*(1+(mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier()))) +.306));
            } else {
                event.setSpeed(.306);
            }
        }

        if (mc.thePlayer.onGround && enable && teleport){

            mc.thePlayer.jump();
            teleport = false;
            enable = false;

        }
        if (mc.thePlayer.onGround && !teleport &&!enable) {

            if (mc.thePlayer.ticksExisted % 2 == 1|| !(mc.thePlayer.moveForward ==0 )) {

                event.setForward(1);
            } else {
                MoveUtil.strafe(0);
                event.setForward(-1);
            }
        }
    }

    @SubscribeEvent
    public void onReceivePacket(@NotNull ReceivePacketEvent event) {
        Packet<?> packet = event.getPacket();

        if (packet instanceof S08PacketPlayerPosLook) {
            teleport = true;
        } else if (packet instanceof S12PacketEntityVelocity) {
            if (cancelVelocity.isToggled() && insideBlock())
                event.setCanceled(true);
        }

    }

    @SubscribeEvent
    public void onPushOutOfBlock(@NotNull PushOutOfBlockEvent event) {
        event.setCanceled(true);
    }
}
