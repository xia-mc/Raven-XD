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
import keystrokesmod.utility.ContainerUtils;
import keystrokesmod.utility.Utils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ProjectileAimBot extends IAutoClicker {
    private final ModeValue clickMode;
    private final SliderSetting maxTargetLookFovDiff;

    private int fromSlot = -1;

    public ProjectileAimBot() {
        super("ProjectileAimBot", category.combat);
        this.registerSetting(clickMode = new ModeValue("Click mode", this)
                .add(new LowCPSAutoClicker("Normal", this, false, true))
                .add(new NormalAutoClicker("NormalFast", this, false, true))
                .add(new RecordAutoClicker("Record", this, false, true))
        );
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
        if (KillAura.target != null && !ModuleManager.killAura.isAttack() && Utils.inFov((float) maxTargetLookFovDiff.getInput(), KillAura.target)) {
            int slot = ContainerUtils.getMostProjectiles(-1);
            if (slot == -1) return;
            fromSlot = SlotHandler.getCurrentSlot();
            SlotHandler.setCurrentSlot(slot);


        } else if (fromSlot != -1) {
            SlotHandler.setCurrentSlot(fromSlot);
            fromSlot = -1;
        }
    }

    @Override
    public boolean click() {
        if (ContainerUtils.isProjectiles(SlotHandler.getHeldItem())) {
            Utils.sendClick(1, true);
            return true;
        } else {
            return false;
        }
    }
}
