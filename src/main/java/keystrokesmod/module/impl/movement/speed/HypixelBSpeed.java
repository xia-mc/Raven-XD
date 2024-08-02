//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package keystrokesmod.module.impl.movement.speed;

import keystrokesmod.event.MoveInputEvent;
import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.event.PrePlayerInputEvent;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.module.impl.movement.Speed;
import keystrokesmod.module.impl.movement.Sprint;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.module.setting.utils.ModeOnly;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.rise.RiseSecret;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.BlockUtils;
import net.minecraft.block.BlockAir;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;


/**
 * Skidded from Rise (com.alan.clients.module.impl.movement.speed.WatchdogSpeed)
 * <p>
 * Counter-confused by xia__mc
 * @see hackclient.rise.nb
 * @author Alan34
 */
public class HypixelBSpeed extends SubMode<Speed> {
    private final ModeSetting mode = new ModeSetting("Mode", new String[]{"Ground Strafe", "Autism"}, 0);
    public final ButtonSetting fastFall = new ButtonSetting("Fast Fall", false, new ModeOnly(mode, 1));
    public final SliderSetting ticksToGlide = new SliderSetting("Ticks to Glide", 29, 1, 29, 1, new ModeOnly(mode, 1));
    private float wS = 0.0F;
    private boolean wT;
//    private boolean vo;
//    private static float wU = 0.0F;
//    private static final float wV = 8.0F;
    
    private int offGroundTicks = 0;

    public HypixelBSpeed(String name, Speed parent) {
        super(name, parent);
        this.registerSetting(mode, fastFall, ticksToGlide);
    }
    
    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer.onGround)
            offGroundTicks = 0;
        else
            offGroundTicks++;
    }

    @SubscribeEvent
    public void onMoveInput(@NotNull MoveInputEvent event) {
        event.setJump(false);
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (parent.noAction()) return;

        if (BlockUtils.getBlock(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.motionY, 0.0) != Blocks.air) {
            this.wT = false;
        }

        if (RiseSecret.entityPlayerSP$bIm <= 10 || b(2.0)) {
            this.wT = true;
        }

        if (this.fastFall.isToggled() && !this.wT) {
            event.setPosY(event.getPosY() + 5.0E-12);
        }

    }

    public static boolean b(double var0) {
        AxisAlignedBB var3 = mc.thePlayer.getEntityBoundingBox().offset(0.0, var0 / 2.0, 0.0).expand(0.0, var0 - (double) mc.thePlayer.height, 0.0);
        return !mc.theWorld.getCollidingBoundingBoxes(mc.thePlayer, var3).isEmpty();
    }

    @SubscribeEvent
    public void onStrafe(PrePlayerInputEvent event) {
        if (parent.noAction()) return;

        switch (mode.getOptions()[(int) mode.getInput()]) {
            case "Ground Strafe":
                if (!MoveUtil.isMoving()) {
                    Sprint.omni = false;
                    break;
                }
                Sprint.omni = true;
                if (mc.thePlayer.onGround) {
                    MoveUtil.strafe(MoveUtil.getAllowedHorizontalDistance() - 0.01);
                    mc.thePlayer.jump();
                }

                if (offGroundTicks == 1 || BlockUtils.getBlock(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.motionY, mc.thePlayer.posZ) != Blocks.air && offGroundTicks > 2) {
                    MoveUtil.strafe();
                }
                break;
            case "Autism":
                if (MoveUtil.isMoving() && mc.thePlayer.onGround) {
                    MoveUtil.strafe(MoveUtil.getAllowedHorizontalDistance());
                    mc.thePlayer.jump();
                }

                if (mc.thePlayer.onGround) {
                    this.wS = 1.0F;
                }

                int[] var4 = new int[]{10, 11, 13, 14, 16, 17, 19, 20, 22, 23, 25, 26, 28, 29};
                if (!(BlockUtils.getBlockState(mc.thePlayer.getPosition().add(0.0, -0.25, 0.0)).getBlock() instanceof BlockAir)) {
                    for (int var8 : var4) {
                        if (offGroundTicks == var8 && var8 <= 9 + (int) this.ticksToGlide.getInput()) {
                            mc.thePlayer.motionY = 0.0;
                            MoveUtil.strafe(MoveUtil.getAllowedHorizontalDistance() * (double) this.wS);
                            this.wS *= 0.98F;
                        }
                    }
                }

                break;
        }

        if (this.fastFall.isToggled() && !this.wT && mc.thePlayer.hurtTime == 0) {
//            ArrayList<Double> var9 = new ArrayList<>(Arrays.asList(0.33310120140062277, 0.24796918219826297, 0.14960980209333172, 0.05321760771444281, -0.02624674495067964, -0.3191218156544406, -0.3161693874618279, -0.3882460072689227, -0.4588810960546281));
//            if (offGroundTicks < var9.size() - 1 && offGroundTicks > 1 && mc.thePlayer.bHZ > offGroundTicks && mc.thePlayer.bIg > 10) {
//            }

            if (offGroundTicks == 1) {
                MoveUtil.strafe();
            }

            if (BlockUtils.getBlock(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.motionY, mc.thePlayer.posZ) != Blocks.air && offGroundTicks > 2) {
                MoveUtil.strafe();
            }
        }

    }

    public void onEnable() {
        if (mc.thePlayer.onGround) {
            mc.thePlayer.jump();
        }

        this.wT = true;
    }

    public void onDisable() {
        Sprint.omni = false;
    }

/*
 TODO this is a public static function.. i don't know where do it be used.
 */
//    public static void a(dx var0, double var1, float var3, float var4, float var5) {
//        if (var3 != 0.0F || var4 != 0.0F) {
//            float var6 = var5;
//            boolean var7 = var3 < 0.0F;
//            float var8 = 90.0F * (var3 > 0.0F ? 0.5F : (var7 ? -0.5F : 1.0F));
//            if (var7) {
//                var6 = var5 + 180.0F;
//            }
//
//            if (var4 > 0.0F) {
//                var6 -= var8;
//            } else if (var4 < 0.0F) {
//                var6 += var8;
//            }
//
//            var6 = (var6 + 360.0F) % 360.0F;
//            float var9 = var6 - wU;
//            var9 = (var9 + 180.0F) % 360.0F - 180.0F;
//            if (Math.abs(var9) < 8.0F) {
//                wU = var6;
//            } else {
//                wU += Math.signum(var9) * 8.0F;
//            }
//
//            wU = (wU + 360.0F) % 360.0F;
//            double var10 = StrictMath.cos(Math.toRadians((double)wU + 90.0));
//            double var12 = StrictMath.cos(Math.toRadians((double)wU));
//            var0.setPosX(var10 * var1);
//            var0.setPosZ(var12 * var1);
//        }
//    }
}
