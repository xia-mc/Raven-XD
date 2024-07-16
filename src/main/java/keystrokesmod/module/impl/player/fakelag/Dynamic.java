package keystrokesmod.module.impl.player.fakelag;

import keystrokesmod.module.impl.player.FakeLag;
import keystrokesmod.module.setting.impl.SubModeValue;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.Utils;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;

import static keystrokesmod.module.ModuleManager.blink;

public class Dynamic extends SubModeValue {
    private AbstractClientPlayer target = null;

    public Dynamic(String name) {
        super(name);
    }
    @Override
    public void onEnable() {
        FakeLag.lastDisableTime = -1;
        FakeLag.lastHurt = false;
        FakeLag.lastStartBlinkTime = -1;
        FakeLag.packetQueue.clear();
        FakeLag.vec3 = null;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderTick(TickEvent.RenderTickEvent ev) {
        if (!Utils.nullCheck()) {
            FakeLag.sendPacket(false);
            FakeLag.lastDisableTime = System.currentTimeMillis();
            FakeLag.lastStartBlinkTime = -1;
            return;
        }
        if (System.currentTimeMillis() - FakeLag.lastDisableTime <= FakeLag.dynamicStopOnHurtTime.getInput()) {
            blink.disable();
        }

        if (blink.isEnabled()) {
            if (System.currentTimeMillis() - FakeLag.lastStartBlinkTime > (long) FakeLag.delayInt) {
                if (FakeLag.debug.isToggled()) Utils.sendMessage("&3" + FakeLag.class.getSimpleName() + "&7: &r" + "stop lag: time out.");
                FakeLag.lastStartBlinkTime = System.currentTimeMillis();
                blink.disable();
            } else if (!FakeLag.lastHurt && mc.thePlayer.hurtTime > 0 && FakeLag.dynamicStopOnHurt.isToggled()) {
                if (FakeLag.debug.isToggled()) Utils.sendMessage("&3" + FakeLag.class.getSimpleName() + "&7: &r" + "stop lag: hurt.");
                FakeLag.lastDisableTime = System.currentTimeMillis();
                blink.disable();
            }
        }

        if (target != null) {
            double distance = new Vec3(mc.thePlayer).distanceTo(target);
            if (blink.isEnabled() && distance < FakeLag.dynamicStopRange.getInput()) {
                if (FakeLag.debug.isToggled()) Utils.sendMessage("&3" + FakeLag.class.getSimpleName() + "&7: &r" + "stop lag: too low range.");
                blink.disable();
            } else if (!blink.isEnabled() && distance > FakeLag.dynamicStopRange.getInput()
                    && new Vec3(mc.thePlayer).distanceTo(target) < FakeLag.dynamicStartRange.getInput()) {
                if (FakeLag.debug.isToggled()) Utils.sendMessage("&3" + FakeLag.class.getSimpleName() + "&7: &r" +  "start lag: in range.");
                FakeLag.lastStartBlinkTime = System.currentTimeMillis();
                blink.enable();
            } else if (blink.isEnabled() && distance > FakeLag.dynamicStartRange.getInput()) {
                if (FakeLag.debug.isToggled()) Utils.sendMessage("&3" + FakeLag.class.getSimpleName() + "&7: &r" + "stop lag: out of range.");
                blink.disable();
            } else if (distance > FakeLag.dynamicMaxTargetRange.getInput()) {
                if (FakeLag.debug.isToggled()) Utils.sendMessage("&3" + FakeLag.class.getSimpleName() + "&7: &r" + String.format("release target: %s", target.getName()));
                target = null;
                blink.disable();
            }
        } else blink.disable();

        FakeLag.lastHurt = mc.thePlayer.hurtTime > 0;
    }
    @SubscribeEvent
    public void onAttack(@NotNull AttackEntityEvent e) {
        if (e.target instanceof AbstractClientPlayer) {
            if (FakeLag.dynamicIgnoreTeammates.isToggled() && Utils.isTeamMate(e.target)) return;
            target = (AbstractClientPlayer) e.target;
        }
    }
}