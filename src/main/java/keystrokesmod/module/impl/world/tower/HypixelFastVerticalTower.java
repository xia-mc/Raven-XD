package keystrokesmod.module.impl.world.tower;

import keystrokesmod.event.MoveEvent;
import keystrokesmod.module.impl.world.Tower;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Reflection;
import net.minecraft.block.BlockAir;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class HypixelFastVerticalTower extends SubMode<Tower> {
    private boolean towering;
    private int towerTicks;

    public HypixelFastVerticalTower(String name, @NotNull Tower parent) {
        super(name, parent);
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) throws IllegalAccessException {
        boolean airUnder = negativeExpand(0.239);

        if (MoveUtil.isMoving() && MoveUtil.speed() > 0.1 && !mc.thePlayer.isPotionActive(Potion.jump)) {
            double towerSpeed = isGoingDiagonally(0.1) ? 0.22 : 0.29888888;
            if (!mc.thePlayer.onGround) {
                if (this.towering) {
                    if (this.towerTicks == 2) {
                        event.setY(Math.floor(mc.thePlayer.posY + 1.0) - mc.thePlayer.posY);
                    } else if (this.towerTicks == 3) {
                        if (parent.canTower() && !airUnder) {
                            event.setY(mc.thePlayer.motionY = 0.4198499917984009);
                            MoveUtil.strafe((float) towerSpeed - this.randomAmount());
                            this.towerTicks = 0;
                        } else {
                            this.towering = false;
                        }
                    }
                }
            } else {
                this.towering = parent.canTower() && !airUnder;
                if (this.towering) {
                    this.towerTicks = 0;
                    Reflection.jumpTicks.set(mc.thePlayer, 0);
                    if (event.getY() > 0.0) {
                        event.setY(mc.thePlayer.motionY = 0.4198479950428009);
                        MoveUtil.strafe((float) towerSpeed - this.randomAmount());
                    }
                }
            }

            ++this.towerTicks;
        }
    }

    public static boolean isGoingDiagonally(double amount) {
        return Math.abs(mc.thePlayer.motionX) > amount && Math.abs(mc.thePlayer.motionZ) > amount;
    }

    public static boolean negativeExpand(double negativeExpandValue) {
        return mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX + negativeExpandValue, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ + negativeExpandValue)).getBlock() instanceof BlockAir && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX - negativeExpandValue, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ - negativeExpandValue)).getBlock() instanceof BlockAir && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX - negativeExpandValue, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)).getBlock() instanceof BlockAir && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX + negativeExpandValue, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ)).getBlock() instanceof BlockAir && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ + negativeExpandValue)).getBlock() instanceof BlockAir && mc.theWorld.getBlockState(new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0, mc.thePlayer.posZ - negativeExpandValue)).getBlock() instanceof BlockAir;
    }

    private double randomAmount() {
        return 8.0E-4 + Math.random() * 0.008;
    }
}
