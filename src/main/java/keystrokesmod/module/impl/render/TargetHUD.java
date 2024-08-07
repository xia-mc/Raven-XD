package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.impl.combat.KillAura;
import keystrokesmod.module.impl.render.targetvisual.ITargetVisual;
import keystrokesmod.module.impl.render.targetvisual.targethud.ExhibitionTargetHUD;
import keystrokesmod.module.impl.render.targetvisual.targethud.RavenTargetHUD;
import keystrokesmod.module.impl.render.targetvisual.targethud.TestTargetHUD;
import keystrokesmod.module.impl.render.targetvisual.targethud.WurstTargetHUD;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeValue;
import keystrokesmod.utility.Utils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MovingObjectPosition;
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
    private long lastKillAuraTime = -1;

    public TargetHUD() {
        super("TargetHUD", category.render);
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new RavenTargetHUD("Raven", this))
                .add(new ExhibitionTargetHUD("Exhibition", this))
                .add(new WurstTargetHUD("Wurst", this))
                .add(new TestTargetHUD("Test", this))
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
        lastKillAuraTime = -1;
    }

    @Override
    public void onUpdate() {
        if (!Utils.nullCheck()) {
            target = null;
            return;
        }

        if (KillAura.target != null) {
            target = KillAura.target;
            lastKillAuraTime = System.currentTimeMillis();
        }

        if (lastKillAuraTime != -1 && System.currentTimeMillis() - lastKillAuraTime > 1000) {
            target = null;
            lastKillAuraTime = -1;
        }


        if (onlyKillAura.isToggled()) return;

        // manual target
        if (target != null) {
            if (!Utils.inFov(180, target) || target.getDistanceSqToEntity(mc.thePlayer) > 36) {
                target = null;
            }
        } else {
            if (mc.objectMouseOver != null
                    && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY
                    && mc.objectMouseOver.entityHit instanceof EntityLivingBase) {
                target = (EntityLivingBase) mc.objectMouseOver.entityHit;
                lastKillAuraTime = System.currentTimeMillis();
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
        if (mc.currentScreen != null) {
            return;
        }
        render(target);
    }

    private static void render(EntityLivingBase target) {
        if (target != null) {
            final ScaledResolution scaledResolution = new ScaledResolution(mc);
            final int n2 = 8;
            final int n3 = mc.fontRendererObj.getStringWidth(target.getDisplayName().getFormattedText()) + n2;
            final int n4 = scaledResolution.getScaledWidth() / 2 - n3 / 2 + posX;
            final int n5 = scaledResolution.getScaledHeight() / 2 + 15 + posY;
            current$minX = n4 - n2;
            current$minY = n5 - n2;
            current$maxX = n4 + n3;
            current$maxY = n5 + (mc.fontRendererObj.FONT_HEIGHT + 5) - 6 + n2;

            ((ITargetVisual) mode.getSubModeValues().get((int) mode.getInput())).render(target);
        }
    }

    public static void renderExample() {
        render(mc.thePlayer);
    }
}
