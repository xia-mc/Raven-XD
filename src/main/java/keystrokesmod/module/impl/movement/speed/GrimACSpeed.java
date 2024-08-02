package keystrokesmod.module.impl.movement.speed;

import keystrokesmod.module.impl.movement.Speed;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import org.jetbrains.annotations.NotNull;

public class GrimACSpeed extends SubMode<Speed> {
    private final SliderSetting amount;
    private final ButtonSetting autoJump;

    public GrimACSpeed(String name, @NotNull Speed parent) {
        super(name, parent);
        this.registerSetting(new DescriptionSetting("Only works on 1.9+"));
        this.registerSetting(amount = new SliderSetting("Amount", 4, 0, 10, 1));
        this.registerSetting(autoJump = new ButtonSetting("Auto jump", true));
    }

    @Override
    public void onUpdate() {
        if (parent.noAction() || !MoveUtil.isMoving()) return;
        if (mc.thePlayer.onGround && autoJump.isToggled()) {
            mc.thePlayer.jump();
        }

        int collisions = 0;
        AxisAlignedBB grimPlayerBox = mc.thePlayer.getEntityBoundingBox().expand(1.0, 1.0, 1.0);
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (canCauseSpeed(entity) && (grimPlayerBox.intersectsWith(entity.getEntityBoundingBox()))) {
                collisions += 1;
            }
        }
        double yaw = Math.toRadians(MoveYaw());
        double boost = amount.getInput() / 100 * collisions;
        mc.thePlayer.addVelocity(-Math.sin(yaw) * boost, 0.0, Math.cos(yaw) * boost);
    }

    private boolean canCauseSpeed(Entity entity) {
        return entity != mc.thePlayer && entity instanceof EntityLivingBase;
    }

    public static double MoveYaw(){
        return  (MoveUtil.direction() * 180f / Math.PI);
    }
}
