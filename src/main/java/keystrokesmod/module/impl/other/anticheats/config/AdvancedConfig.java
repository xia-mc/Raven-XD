package keystrokesmod.module.impl.other.anticheats.config;

public class AdvancedConfig {
    
    public static boolean blinkCheck = true;
    public static int blinkAlertBuffer = 10;
    public static double blinkMaxDistance = 8;

    public static boolean noSlowACheck = true;
    public static int noSlowAAlertBuffer = 20;
    public static int noSlowAInJumpDisableTick = 4;


    
    public static boolean speedACheck = true;
    public static int speedAAlertBuffer = 30;
    public static int speedAAfterJumpJumpTick = 10;
    public static double speedAAfterJumpSpeed = 7.4;
    public static double speedASprintingSpeed = 5.612;
    public static double speedASilentSpeed = 1.295;
    public static double speedAWalkSpeed = 4.317;


    
    public static boolean speedBCheck = true;
    public static int speedBAlertBuffer = 10;

    public static boolean groundSpoofACheck = true;
    public static int groundSpoofAAlertBuffer = 15;

    
    public static boolean groundSpoofBCheck = true;
    public static int groundSpoofBAlertBuffer = 15;

    
    public static boolean autoBlockACheck = false;
    public static int autoBlockAAlertBuffer = 2;

    
    public static boolean speedCCheck = true;
    public static int speedCAlertBuffer = 10;

    
    public static boolean motionACheck = true;
    public static int motionAAlertBuffer = 10;

    public static boolean reachACheck = true;
    public static int reachAAlertBuffer = 4;
    public static double reachADefaultReach = 3.5;
    public static int reachACheckDelay = 2;

    public static boolean hitBoxACheck = true;
    public static int hitBoxAAlertBuffer = 4;
    public static int hitBoxACheckDelay = 2;

    
    public static boolean flyACheck = true;
    public static int flyAAlertBuffer = 30;
    
    public static boolean flyBCheck = true;
    public static int flyBAlertBuffer = 30;
    public static int flyBMinRepeatTicks = 10;
    
    public static boolean strafeACheck = true;
    public static int strafeAAlertBuffer = 20;
    public static double strafeAMaxDiffToFlag = 0.005;

    public static boolean invalidPitchCheck = true;
    public static int invalidPitchAlertBuffer = 1;

    public static boolean aimACheck = true;
    public static int aimAAlertBuffer = 20;
    public static boolean aimAOnlyOnSwing = false;
    public static boolean aimAOnlyIfTargetIsMoving = true;
    public static boolean aimAOnlyPlayer = true;
    public static double aimAMinDiffYaw = 4;
    public static double aimAMinDeltaYaw = 25;
    public static double aimAMinDiffPitch = 4;
    public static double aimAMinDeltaPitch = 25;
    public static double aimAMaxDistance = 6;

    public static boolean scaffoldACheck = true;
    public static int scaffoldAAlertBuffer = 30;

    public static boolean aimBCheck = true;
    public static int aimBAlertBuffer = 10;
    public static double aimBMinDiffYaw = 1;
    public static double aimBMinDiffPitch = 1;

    public static boolean scaffoldBCheck = true;
    public static int scaffoldBAlertBuffer = 10;

    public static boolean noFallACheck = true;
    public static int noFallAAlertBuffer = 3;

    public static boolean aimCCheck = true;
    public static int aimCAlertBuffer = 1;
    public static double aimCMinDeltaYaw = 25;
    public static double aimCMinDeltaPitch = 25;

    public static short getNoSlowAInJumpDisableTick() {
        return (short) noSlowAInJumpDisableTick;
    }

    public static short getSpeedAAfterJumpJumpTick() {
        return (short) speedAAfterJumpJumpTick;
    }

}
