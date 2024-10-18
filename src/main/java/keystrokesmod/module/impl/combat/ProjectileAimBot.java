package keystrokesmod.module.impl.combat;

import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.combat.autoclicker.IAutoClicker;
import keystrokesmod.module.impl.combat.autoclicker.LowCPSAutoClicker;
import keystrokesmod.module.impl.combat.autoclicker.NormalAutoClicker;
import keystrokesmod.module.impl.combat.autoclicker.RecordAutoClicker;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.ModeValue;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.ContainerUtils;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ProjectileAimBot extends IAutoClicker {
    private final ModeValue clickMode;
    private final SliderSetting maxTargetLookFovDiff;
    private final SliderSetting minDistance;

    private int fromSlot = -1;
    private boolean targeted = false;

    public ProjectileAimBot() {
        super("ProjectileAimBot", category.combat);
        this.registerSetting(clickMode = new ModeValue("Click mode", this)
                .add(new LowCPSAutoClicker("Normal", this, false, true))
                .add(new NormalAutoClicker("NormalFast", this, false, true))
                .add(new RecordAutoClicker("Record", this, false, true))
        );
        this.registerSetting(minDistance = new SliderSetting("Min distance", 3.5, 1, 10, 0.1));
        this.registerSetting(maxTargetLookFovDiff = new SliderSetting("MaxTargetLookFovDiff", 180, 20, 180, 1));

    }

    @Override
    public void onEnable() {
        clickMode.enable();
    }

    @Override
    public void onDisable() {
        clickMode.disable();
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        Vec3 eyePos = Utils.getEyePos();
        if (KillAura.target != null && !ModuleManager.killAura.isAttack()
                && Utils.inFov((float) maxTargetLookFovDiff.getInput(), KillAura.target)
                && Utils.getEyePos(KillAura.target).distanceTo(eyePos) >= minDistance.getInput()) {
            int slot = ContainerUtils.getMostProjectiles(-1);
            if (slot == -1) return;
            if (slot >= 9)
                slot -= 36;

            if (slot < 0 || slot > 9) return;
            if (fromSlot == -1)
                fromSlot = SlotHandler.getCurrentSlot();
            SlotHandler.setCurrentSlot(slot);
            targeted = true;
        }
    }

    @Override
    public boolean click() {
        if (ContainerUtils.isProjectiles(SlotHandler.getHeldItem()) && targeted) {
            Utils.sendClick(1, true);
            targeted = false;
            Utils.sendClick(1, false);
            return true;
        } else {
            if (fromSlot != -1) {
                SlotHandler.setCurrentSlot(fromSlot);
                fromSlot = -1;
            }
            return false;
        }
    }
}
