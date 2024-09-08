package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.combat.KillAura;
import keystrokesmod.module.impl.render.targetvisual.ITargetVisual;
import keystrokesmod.module.impl.render.targetvisual.targetesp.JelloTargetESP;
import keystrokesmod.module.impl.render.targetvisual.targetesp.RavenTargetESP;
import keystrokesmod.module.impl.render.targetvisual.targetesp.VapeTargetESP;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeValue;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

public class TargetESP extends Module {
    private final ModeValue mode;
    private final ButtonSetting onlyKillAura;

    private static @Nullable EntityLivingBase target = null;
    private long lastTargetTime = -1;

    public TargetESP() {
        super("TargetESP", category.render);
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new RavenTargetESP("Raven", this))
                .add(new JelloTargetESP("Jello", this))
                .add(new VapeTargetESP("Vape", this))
        );
        this.registerSetting(onlyKillAura = new ButtonSetting("Only killAura", true));
    }

    @Override
    public void onEnable() {
        mode.enable();
    }

    @Override
    public void onDisable() {
        mode.disable();

        target = null;
        lastTargetTime = -1;
    }

    @Override
    public void onUpdate() {
        if (!Utils.nullCheck()) {
            target = null;
            return;
        }


        if (KillAura.target != null) {
            target = KillAura.target;
            lastTargetTime = System.currentTimeMillis();
        }

        if (target != null && lastTargetTime != -1 && (target.isDead || System.currentTimeMillis() - lastTargetTime > 5000 || target.getDistanceSqToEntity(mc.thePlayer) > 20)) {
            target = null;
            lastTargetTime = -1;
        }

        if (onlyKillAura.isToggled()) return;

        // manual target
        if (target != null) {
            if (!Utils.inFov(180, target) || target.getDistanceSqToEntity(mc.thePlayer) > 36) {
                target = null;
            }
        }
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if (onlyKillAura.isToggled()) return;

        if (event.target instanceof EntityLivingBase) {
            target = (EntityLivingBase) event.target;
        }
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if (target != null)
            ((ITargetVisual) mode.getSubModeValues().get((int) mode.getInput())).render(target);
    }
}
