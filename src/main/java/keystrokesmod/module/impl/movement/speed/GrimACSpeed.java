package keystrokesmod.module.impl.movement.speed;

import keystrokesmod.event.MoveInputEvent;
import keystrokesmod.module.impl.combat.KillAura;
import keystrokesmod.module.impl.movement.Speed;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class GrimACSpeed extends SubMode<Speed> {
    private final SliderSetting amount;
    private final ButtonSetting autoJump;

    private boolean forceStrafe = false;

    public GrimACSpeed(String name, @NotNull Speed parent) {
        super(name, parent);
        this.registerSetting(new DescriptionSetting("Only works on 1.9+"));
        this.registerSetting(amount = new SliderSetting("Amount", 3, 0, 10, 1));
        this.registerSetting(autoJump = new ButtonSetting("Auto jump", true));
    }

    @Override
    public void onUpdate() {
        if (parent.noAction() || !MoveUtil.isMoving()) {
            forceStrafe = false;
            return;
        }
        if (mc.thePlayer.onGround && autoJump.isToggled()) {
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
            double yaw = MoveUtil.direction();
            double mx = -Math.sin(Math.toRadians(yaw));
            double mz = Math.cos(Math.toRadians(yaw));
            mc.thePlayer.addVelocity(mx * strafeOffset, 0.0, mz * strafeOffset);
            if (c < 4 && KillAura.target != null) {
                forceStrafe = true;
                return;
            }
            forceStrafe = false;
            return;
        }
        forceStrafe = false;
    }

    @SubscribeEvent
    public void onMoveInput(MoveInputEvent event) {
        if (forceStrafe)
            event.setStrafe(-1);
    }

    @Override
    public void onEnable() throws Throwable {
        forceStrafe = false;
    }
}
