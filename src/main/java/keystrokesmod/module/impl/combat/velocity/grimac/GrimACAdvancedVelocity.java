package keystrokesmod.module.impl.combat.velocity.grimac;

import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.event.PreVelocityEvent;
import keystrokesmod.mixins.impl.entity.EntityPlayerSPAccessor;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.combat.KillAura;
import keystrokesmod.module.impl.combat.velocity.GrimACVelocity;
import keystrokesmod.module.impl.exploit.viaversionfix.ViaVersionFixHelper;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemFood;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import javax.vecmath.Vector2d;

public class GrimACAdvancedVelocity extends SubMode<GrimACVelocity> {
    private final ButtonSetting notWhileEating;
    private final ButtonSetting debug;
    private final ButtonSetting test;

    public boolean velocityInput;
    public float velocityYaw;
    private boolean attacked;
    private double reduceXZ;
    private int skipTicks = 0;

    public GrimACAdvancedVelocity(String name, @NotNull GrimACVelocity parent) {
        super(name, parent);
        this.registerSetting(notWhileEating = new ButtonSetting("Not while eating", false));
        this.registerSetting(debug = new ButtonSetting("Debug", false));
        this.registerSetting(test = new ButtonSetting("Test", false));
    }

    @Override
    public void onDisable() throws Throwable {
        skipTicks = 0;
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.@NotNull LivingUpdateEvent event) {
        if (event.entityLiving != mc.thePlayer) return;
        if (ViaVersionFixHelper.is122() || test.isToggled()) {
            if (velocityInput) {
                if (attacked) {
                    mc.thePlayer.motionX *= reduceXZ;
                    mc.thePlayer.motionZ *= reduceXZ;
                    attacked = false;
                }
                if (mc.thePlayer.hurtTime == 0) {
                    velocityInput = false;
                }
            }
        } else {
            if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.onGround) {
                mc.thePlayer.addVelocity(-1.3E-10, -1.3E-10, -1.3E-10);
                mc.thePlayer.setSprinting(false);
            }
        }
    }

    @SubscribeEvent
    public void onPreVelocity(PreVelocityEvent event) {
        if (notWhileEating.isToggled() && mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem() instanceof ItemFood && mc.thePlayer.isUsingItem())
            return;

        double x = event.getMotionX() / 8000D;
        double z = event.getMotionZ() / 8000D;
        float yaw = (float) Math.toDegrees(Math.atan2(z, x)) - 90;
        double speed = Math.sqrt(x * x + z * z);
        float yawDiff = Math.abs(((((yaw - mc.thePlayer.rotationYaw) % 360F) + 540F) % 360F) - 180F);
        double horizontalStrength = new Vector2d(event.getMotionX(), event.getMotionZ()).length();
        if (horizontalStrength <= 1000) return;
        velocityInput = true;
        Entity entity = null;
        reduceXZ = 0;

        Entity target = KillAura.target;
        if (target != null && !ModuleManager.killAura.noAimToEntity()) {
            entity = KillAura.target;
        }

        boolean state = ((EntityPlayerSPAccessor) mc.thePlayer).isServerSprint();

        if (entity != null) {
            if (!state) {
                skipTicks = 1;
                mc.getNetHandler().getNetworkManager().sendPacket(new C03PacketPlayer(mc.thePlayer.onGround));
                mc.getNetHandler().getNetworkManager().sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
            }
            int count = 12;
            for (int i = 1; i <= count; i++) {
                parent.sendAttack(entity);
            }
            if (!state) {
                mc.getNetHandler().getNetworkManager().sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
            }
            attacked = true;
            reduceXZ = 0.07776;
            velocityYaw = yaw;
            if (debug.isToggled()) {
                Utils.sendMessage("Yaw: " + Math.round(velocityYaw * 100) / 100f + ", Diff: " + Math.round(yawDiff * 100) / 100f + ", Speed: " + Math.round(speed * 100) / 100f);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPreUpdate(PreUpdateEvent event) {
        if (skipTicks > 0) {
            skipTicks--;
            event.setCanceled(true);
        }
    }
}
