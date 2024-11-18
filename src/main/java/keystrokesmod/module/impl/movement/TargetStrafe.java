package keystrokesmod.module.impl.movement;

import keystrokesmod.event.JumpEvent;
import keystrokesmod.event.PrePlayerInputEvent;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.combat.KillAura;
import keystrokesmod.module.impl.other.anticheats.utils.world.PlayerRotation;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.MoveUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static keystrokesmod.module.ModuleManager.killAura;
import static keystrokesmod.module.ModuleManager.scaffold;

public class TargetStrafe extends Module {
    private static float yaw;
    private static EntityLivingBase target = null;
    private static boolean active = false;
    private final SliderSetting range = new SliderSetting("Range", 1, 0.2, 6, 0.1);
    private final ButtonSetting speed = new ButtonSetting("Allow speed", true);
    private final ButtonSetting fly = new ButtonSetting("Allow fly", false);
    private final ButtonSetting manual = new ButtonSetting("Allow manual", false);
    private final ButtonSetting strafe = new ButtonSetting("Strafe around", true);
    private boolean left, colliding;

    public TargetStrafe() {
        super("TargetStrafe", category.movement);
        this.registerSetting(range, speed, fly, manual, strafe);
    }

    public static float getMovementYaw() {
        if (active && target != null) return yaw;
        return mc.thePlayer.rotationYaw;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onJump(JumpEvent event) {
        if (target != null && active) {
            event.setYaw(yaw);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onStrafe(PrePlayerInputEvent event) {
        if (target != null && active) {
            event.setYaw(yaw);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPreUpdate(PreUpdateEvent event) {
        //  Disable if scaffold is enabled
        if (scaffold == null || scaffold.isEnabled() || killAura == null || !killAura.isEnabled()) {
            active = false;
            return;
        }

        active = true;

        /*
         * Getting targets and selecting the nearest one
         */
        if ((!speed.isToggled() && ModuleManager.speed.isEnabled())
                || (!fly.isToggled() && ModuleManager.fly.isEnabled())
                || (!manual.isToggled() && !ModuleManager.speed.isEnabled() && !ModuleManager.fly.isEnabled())) {
            target = null;
            return;
        }

        if (KillAura.target == null) {
            target = null;
            return;
        }

        if (mc.thePlayer.isCollidedHorizontally || !BlockUtils.isBlockUnder(5)) {
            if (!colliding) {
                if (strafe.isToggled())
                    MoveUtil.strafe();
                left = !left;
            }
            colliding = true;
        } else {
            colliding = false;
        }

        target = KillAura.target;

        if (target == null) {
            return;
        }

        float yaw = PlayerRotation.getYaw(new Vec3(target)) + (90 + 45) * (left ? -1 : 1);

        final double range = this.range.getInput() + Math.random() / 100f;
        final double posX = -MathHelper.sin((float) Math.toRadians(yaw)) * range + target.posX;
        final double posZ = MathHelper.cos((float) Math.toRadians(yaw)) * range + target.posZ;

        yaw = PlayerRotation.getYaw(new Vec3(posX, target.posY, posZ));

        TargetStrafe.yaw = yaw;
    }

    @Override
    public void onDisable() {
        active = false;
    }
}