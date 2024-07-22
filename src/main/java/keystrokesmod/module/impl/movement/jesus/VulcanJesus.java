package keystrokesmod.module.impl.movement.jesus;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.module.impl.movement.Jesus;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import net.minecraft.potion.Potion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class VulcanJesus extends SubMode<Jesus> {
    private boolean isFirstTimeInWater = true;
    private int waterTicks = 0;
    private double posY = 50;

    public VulcanJesus(String name, @NotNull Jesus parent) {
        super(name, parent);
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (!mc.gameSettings.keyBindJump.isPressed() && mc.thePlayer.isInWater() && isFirstTimeInWater && !(mc.thePlayer.posY % 1 == 0 || (mc.thePlayer.posY * 2) % 1 == 0) ) {
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - .85, mc.thePlayer.posZ);
            isFirstTimeInWater = false;

        }

        if (mc.thePlayer.isInWater() && !mc.gameSettings.keyBindJump.isKeyDown() && !(mc.thePlayer.posY % 1 == 0 || (mc.thePlayer.posY * 2) % 1 == 0)) {
            if (MoveUtil.isMoving()) MoveUtil.strafe(MoveUtil.getAllowedHorizontalDistance() - Math.random() / 1000);
            mc.thePlayer.motionY = 0;
        }
        if (isFirstTimeInWater){
            posY = mc.thePlayer.posY-.85;
        }

        if (mc.thePlayer.isInWater()) {
            waterTicks = 0;
        }

        if(waterTicks<20){
            MoveUtil.strafe();
        }
        waterTicks++;

        boolean jump = mc.gameSettings.keyBindJump.isKeyDown() || mc.gameSettings.keyBindJump.isPressed();

        if (jump && mc.thePlayer.isInWater() && !mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindJump.isPressed()){
            mc.thePlayer.setPosition(mc.thePlayer.posX, posY, mc.thePlayer.posZ);
        }

        if (mc.thePlayer.isPotionActive(Potion.moveSpeed) && mc.thePlayer.isInWater() && !mc.gameSettings.keyBindJump.isKeyDown()) {
            if (MoveUtil.isMoving()) MoveUtil.strafe((.05*(1+(mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier())) +.34 - Math.random() / 1000));
        }

        if(!mc.gameSettings.keyBindJump.isKeyDown() && mc.thePlayer.isInWater()  && !(mc.thePlayer.posY % 1 == 0 || (mc.thePlayer.posY * 2) % 1 == 0)){
            mc.thePlayer.setPosition(mc.thePlayer.posX, posY, mc.thePlayer.posZ);
        }
        if(mc.gameSettings.keyBindJump.isKeyDown() && mc.thePlayer.isInWater()){
            mc.thePlayer.motionY = .6;
            MoveUtil.strafe(.1);

        }

        if(mc.thePlayer.isInWater() && !mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindJump.isPressed()  && !(mc.thePlayer.posY % 1 == 0 || (mc.thePlayer.posY * 2) % 1 == 0)){
            mc.thePlayer.setPosition(mc.thePlayer.posX, posY-.2, mc.thePlayer.posZ);
        }

        if (!mc.thePlayer.isInWater() && !isFirstTimeInWater  && !(mc.thePlayer.posY % 1 == 0 || (mc.thePlayer.posY * 2) % 1 == 0))  {
            isFirstTimeInWater = true;
        }
    }
}
