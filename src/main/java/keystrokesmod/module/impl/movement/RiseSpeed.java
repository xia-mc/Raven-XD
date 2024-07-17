////
//// Source code recreated from a .class file by IntelliJ IDEA
//// (powered by FernFlower decompiler)
////
//
//package keystrokesmod.module.impl.movement;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//
//import keystrokesmod.event.MoveInputEvent;
//import keystrokesmod.event.PreMotionEvent;
//import keystrokesmod.module.Module;
//import keystrokesmod.module.setting.impl.ButtonSetting;
//import keystrokesmod.module.setting.impl.ModeSetting;
//import keystrokesmod.module.setting.impl.SliderSetting;
//import keystrokesmod.utility.BlockUtils;
//import net.minecraft.block.BlockAir;
//import net.minecraft.init.Blocks;
//import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
//import org.jetbrains.annotations.NotNull;
//

///**
// * Skidded from Rise
// * <p>
// * Counter-confused by xia__mc
// * @see hackclient.rise.nb (rise 6.1.30)
// */
//public class RiseSpeed extends Module {
//    private final ModeSetting wP = new ModeSetting("Mode", new String[]{"Ground Strafe", "Autism"}, 0);
//    public final ButtonSetting wQ = new ButtonSetting("Fast Fall", false);
//    public final SliderSetting wR = new SliderSetting("Ticks to Glide", 29, 1, 29, 1);
//    private float wS = 0.0F;
//    private boolean wT;
//    private boolean vo;
//    private static float wU = 0.0F;
//    private static final float wV = 8.0F;
//
//    public RiseSpeed() {
//        super("RiseSpeed", category.experimental);
//    }
//
//    @SubscribeEvent
//    public void onMoveInput(@NotNull MoveInputEvent event) {
//        event.setJump(false);
//    }
//
//    @SubscribeEvent
//    public void onPreMotion(PreMotionEvent event) {
//        if (BlockUtils.getBlock(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.motionY, 0.0) != Blocks.air) {
//            this.wT = false;
//        }
//
//        if (mc.thePlayer.bIM <= 10 || abf.b(2.0, true)) {
//            this.wT = true;
//        }
//
//        if ((Boolean)this.wQ.mO() && !this.wT) {
//            var1x.setPosY(var1x.getPosY() + 5.0E-12);
//        }
//
//    }
//
//    @co
//    public final cm<dm> xa = (var1x) -> {
//        if (abd.isMoving() && mc.thePlayer.fr) {
//            abd.strafe(abd.lI());
//            mc.thePlayer.jump();
//        }
//
//        if (mc.thePlayer.fr) {
//            this.wS = 1.0F;
//        }
//
//        int[] var4 = new int[]{10, 11, 13, 14, 16, 17, 19, 20, 22, 23, 25, 26, 28, 29};
//        if (!(Wy.avm.w(mc.thePlayer.ow().ab(0.0, -0.25, 0.0)).aZ() instanceof BlockAir)) {
//            int[] var5 = var4;
//            int var6 = var4.length;
//
//            for (int var7 = 0; var7 < var6; ++var7) {
//                int var8 = var5[var7];
//                if (mc.thePlayer.tk == var8 && var8 <= 9 + ((Number) this.wR.mO()).intValue()) {
//                    mc.thePlayer.bHn = 0.0;
//                    abd.strafe(abd.lI() * (double) this.wS);
//                    this.wS *= 0.98F;
//                }
//            }
//        }
//
//        if ((Boolean)this.wQ.mO() && !this.wT && mc.thePlayer.bKf == 0) {
//            ArrayList var9 = new ArrayList(Arrays.asList(0.33310120140062277, 0.24796918219826297, 0.14960980209333172, 0.05321760771444281, -0.02624674495067964, -0.3191218156544406, -0.3161693874618279, -0.3882460072689227, -0.4588810960546281));
//            if (mc.thePlayer.tk < var9.size() - 1 && mc.thePlayer.tk > 1 && mc.thePlayer.bHZ > mc.thePlayer.tk && mc.thePlayer.bIg > 10) {
//            }
//
//            if (mc.thePlayer.tk == 1) {
//                abd.strafe();
//            }
//
//            if (abf.k(0.0, mc.thePlayer.bHn, 0.0) != Blocks.cbf && mc.thePlayer.tk > 2) {
//                abd.strafe();
//            }
//        }
//
//    };
//
//    public nb(String var1, jv var2) {
//        super(var1, var2);
//    }
//
//    public void onEnable() {
//        if (mc.thePlayer.fr) {
//            mc.thePlayer.jump();
//        }
//
//        this.wT = true;
//    }
//
//    public void onDisable() {
//        mc.thePlayer.azj = false;
//    }
//
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
//}
