package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.combat.KillAura;
import keystrokesmod.module.impl.render.targetvisual.ITargetVisual;
import keystrokesmod.module.impl.render.targetvisual.targethud.RavenTargetHUD;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeValue;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.Nullable;

public class TargetHUD extends Module {
    public static int posX = 70;
    public static int posY = 30;
    private static ModeValue mode;
    private final ButtonSetting onlyKillAura;

    public static int current$minX;
    public static int current$maxX;
    public static int current$minY;
    public static int current$maxY;
    private static @Nullable EntityLivingBase target = null;

    public TargetHUD() {
        super("TargetHUD", category.render);
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new RavenTargetHUD("Raven", this))
        );
        this.registerSetting(onlyKillAura = new ButtonSetting("Only killAura", true));
    }

    @Override
    public void onEnable() {
        mode.enable();
    }

    public void onDisable() {
        mode.disable();

        target = null;
    }

    @Override
    public void onUpdate() {
        if (!Utils.nullCheck()) {
            target = null;
            return;
        }

        if (KillAura.target != null)
            target = KillAura.target;

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
    public void onRender(TickEvent.RenderTickEvent event) {
        if (target != null)
            ((ITargetVisual) mode.getSubModeValues().get((int) mode.getInput())).render(target);
    }

    public static void renderExample() {
        ((ITargetVisual) mode.getSubModeValues().get((int) mode.getInput())).render(mc.thePlayer);
    }
}
