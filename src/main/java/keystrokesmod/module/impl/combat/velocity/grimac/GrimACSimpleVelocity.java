package keystrokesmod.module.impl.combat.velocity.grimac;

import keystrokesmod.event.PostVelocityEvent;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.mixins.impl.entity.EntityPlayerSPAccessor;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.combat.velocity.GrimACVelocity;
import keystrokesmod.module.impl.exploit.viaversionfix.ViaVersionFixHelper;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class GrimACSimpleVelocity extends SubMode<GrimACVelocity> {
    private final SliderSetting reduceCountEveryTime;
    private final SliderSetting reduceTimes;
    private final ButtonSetting onlyWhileMoving;
    private final ButtonSetting notWhileEating;
    private final ButtonSetting debug;

    private int unReduceTimes = 0;

    public GrimACSimpleVelocity(String name, @NotNull GrimACVelocity parent) {
        super(name, parent);
        this.registerSetting(reduceCountEveryTime = new SliderSetting("Reduce count every time", 4, 1, 10, 1));
        this.registerSetting(reduceTimes = new SliderSetting("Reduce times", 1, 1, 5, 1));
        this.registerSetting(onlyWhileMoving = new ButtonSetting("Only while moving", false));
        this.registerSetting(notWhileEating = new ButtonSetting("Not while eating", false));
        this.registerSetting(debug = new ButtonSetting("Debug", false));
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        if (ModuleManager.blink.isEnabled()) return;
        if (unReduceTimes > 0 && mc.thePlayer.hurtTime > 0
                && !(onlyWhileMoving.isToggled() && !MoveUtil.isMoving())
                && !(notWhileEating.isToggled() && mc.thePlayer.isUsingItem() && SlotHandler.getHeldItem() != null && SlotHandler.getHeldItem().getItem() instanceof ItemFood)
                && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY
                && mc.objectMouseOver.entityHit instanceof EntityPlayer) {
            if (!((EntityPlayerSPAccessor) mc.thePlayer).isServerSprint()) {
                PacketUtils.sendPacket(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));
                mc.thePlayer.setSprinting(true);
                ((EntityPlayerSPAccessor) mc.thePlayer).setServerSprint(true);
            }

            doReduce();
            if (debug.isToggled())
                Utils.sendMessage(String.format("%d Reduced %.3f %.3f", (int) reduceTimes.getInput() - unReduceTimes,  mc.thePlayer.motionX, mc.thePlayer.motionZ));
            unReduceTimes--;
        } else {
            unReduceTimes = 0;
        }
    }

    private void doReduce() {
        for (int i = 0; i < (int) reduceCountEveryTime.getInput(); i++) {
            parent.sendAttack(mc.objectMouseOver.entityHit);

            mc.thePlayer.motionX *= 0.6;
            mc.thePlayer.motionZ *= 0.6;
        }
    }

    @SubscribeEvent
    public void onPostVelocity(PostVelocityEvent event) {
        unReduceTimes = (int) reduceTimes.getInput();
    }
}
