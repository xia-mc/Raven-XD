package keystrokesmod.module.impl.combat;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Reflection;
import keystrokesmod.utility.RotationUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFishingRod;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

public class RodAimbot extends Module {
    private final SliderSetting fov;
    private final SliderSetting predicatedTicks;
    private final SliderSetting distance;
    private final ButtonSetting aimInvis;
    private final ButtonSetting ignoreTeammates;
    public boolean rotate;
    private boolean rightClick;
    private EntityPlayer target;
    private int lookTicks;

    public RodAimbot() {
        super("RodAimbot", Module.category.combat, 0);
        this.registerSetting(fov = new SliderSetting("FOV", 180, 30, 360, 4));
        this.registerSetting(predicatedTicks = new SliderSetting("Predicted ticks", 5.0, 0.0, 20.0, 1.0));
        this.registerSetting(distance = new SliderSetting("Distance", 6, 3, 30, 0.5));
        this.registerSetting(aimInvis = new ButtonSetting("Aim invis", false));
        this.registerSetting(ignoreTeammates = new ButtonSetting("Ignore teammates", false));
    }

    public void onDisable() {
        rotate = false;
        rightClick = false;
        target = null;
        lookTicks = 0;
    }

    @SubscribeEvent
    public void onMouse(final MouseEvent mouseEvent) {
        if (mouseEvent.button != 1 || !mouseEvent.buttonstate || !Utils.nullCheck() || mc.currentScreen != null) {
            return;
        }
        if (mc.thePlayer.getCurrentEquippedItem() == null || !(mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemFishingRod) || mc.thePlayer.fishEntity != null) {
            return;
        }
        target = getTarget();
        if (target == null) {
            return;
        }
        mouseEvent.setCanceled(true);
        rightClick = true;
        rotate = true;
        lookTicks = 1;
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (!Utils.nullCheck()) {
            return;
        }
        if (rightClick || rotate) {
            if (mc.thePlayer.getCurrentEquippedItem() == null || !(mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemFishingRod)) {
                return;
            }
            float[] rotations = RotationUtils.getRotationsPredicated(target, (int) predicatedTicks.getInput());
            event.setYaw(rotations[0]);
            event.setPitch(rotations[1]);
            if (lookTicks > 0) {
                lookTicks--;
            } else {
                if (!rightClick && rotate) {
                    rotate = false;
                }
                if (rightClick) {
                    Reflection.rightClick();
                    rightClick = false;
                }
            }
        }
    }

    private @Nullable EntityPlayer getTarget() {
        EntityPlayer closestTarget = null;
        double closestDistance = distance.getInput() * distance.getInput();
        for (final EntityPlayer entityPlayer : mc.theWorld.playerEntities) {
            if (entityPlayer != mc.thePlayer) {
                if (entityPlayer.deathTime != 0) {
                    continue;
                }
                if (!aimInvis.isToggled() && entityPlayer.isInvisible()) {
                    continue;
                }
                double distanceSq = mc.thePlayer.getDistanceSqToEntity(entityPlayer);
                if (distanceSq > closestDistance) {
                    continue;
                }
                if (Utils.isFriended(entityPlayer)) {
                    continue;
                }
                float n = (float) fov.getInput();
                if (n != 360.0f && !Utils.inFov(n, entityPlayer)) {
                    continue;
                }
                if (AntiBot.isBot(entityPlayer)) {
                    continue;
                }
                if (ignoreTeammates.isToggled() && Utils.isTeamMate(entityPlayer)) {
                    continue;
                }
                closestTarget = entityPlayer;
                closestDistance = distanceSq;
            }
        }
        return closestTarget;
    }
}