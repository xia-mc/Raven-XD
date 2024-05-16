package keystrokesmod.module.impl.combat;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;

public class WTap extends Module {
    private final SliderSetting chance;
    private final ButtonSetting playersOnly;
    private final HashMap<Integer, Long> targets = new HashMap<>();
    public static boolean stopSprint = false;
    public WTap() {
        super("WTap", category.combat);
        this.registerSetting(chance = new SliderSetting("Chance", 100, 0, 100, 1, "%"));
        this.registerSetting(playersOnly = new ButtonSetting("Players only", true));
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if (!Utils.nullCheck() || event.entityPlayer != mc.thePlayer || !mc.thePlayer.isSprinting()) {
            return;
        }
        if (playersOnly.isToggled()) {
            if (!(event.target instanceof EntityPlayer)) {
                return;
            }
            final EntityPlayer entityPlayer = (EntityPlayer)event.target;
            if (entityPlayer.maxHurtTime == 0 || entityPlayer.hurtTime > 3) {
                return;
            }
        }
        else if (!(event.target instanceof EntityLivingBase)) {
            return;
        }
        if (((EntityLivingBase)event.target).deathTime != 0) {
            return;
        }
        final long currentTimeMillis = System.currentTimeMillis();
        final Long n = this.targets.get(event.target.getEntityId());
        if (n != null && Utils.getDifference(n, currentTimeMillis) <= 200L) {
            return;
        }
        if (chance.getInput() != 100.0D) {
            double ch = Math.random();
            if (ch >= chance.getInput() / 100.0D) {
                return;
            }
        }
        this.targets.put(event.target.getEntityId(), currentTimeMillis);
        stopSprint = true;
    }

    public void onDisable() {
        stopSprint = false;
        this.targets.clear();
    }
}
