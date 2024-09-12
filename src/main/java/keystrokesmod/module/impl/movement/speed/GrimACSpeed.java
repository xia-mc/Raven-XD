package keystrokesmod.module.impl.movement.speed;

import keystrokesmod.module.impl.movement.Speed;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.util.AxisAlignedBB;
import org.jetbrains.annotations.NotNull;

public class GrimACSpeed extends SubMode<Speed> {
    private final SliderSetting amount;
    private final ButtonSetting autoJump;

    public GrimACSpeed(String name, @NotNull Speed parent) {
        super(name, parent);
        this.registerSetting(new DescriptionSetting("Only works on 1.9+"));
        this.registerSetting(amount = new SliderSetting("Amount", 3, 0, 10, 1));
        this.registerSetting(autoJump = new ButtonSetting("Auto jump", true));
    }

    @Override
    public void onUpdate() {
        if (parent.noAction() || !MoveUtil.isMoving()) {
            return;
        }
        if (mc.thePlayer.onGround && autoJump.isToggled() && !Utils.jumpDown()) {
            mc.thePlayer.jump();
        }

        final AxisAlignedBB playerBox = mc.thePlayer.getEntityBoundingBox().expand(1.0, 1.0, 1.0);
        int c = 0;
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityLivingBase) && !(entity instanceof EntityBoat) && !(entity instanceof EntityMinecart) && !(entity instanceof EntityFishHook) || entity instanceof EntityArmorStand || entity.getEntityId() == mc.thePlayer.getEntityId() || !playerBox.intersectsWith(entity.getEntityBoundingBox()) || entity.getEntityId() == -8 || entity.getEntityId() == -1337)
                continue;
            ++c;
        }
        if (c > 0 && MoveUtil.isMoving()) {
            double strafeOffset = Math.min(c, amount.getInput()) * 0.04;
            MoveUtil.moveFlying(strafeOffset);
        }
    }
}
