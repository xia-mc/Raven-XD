package keystrokesmod.module.impl.fun;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.SliderSetting;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Vec3;

public class FlameTrail extends Module {
    public SliderSetting a;

    public FlameTrail() {
        super("Flame Trail", Module.category.fun, 0);
    }

    public void onUpdate() {
        Vec3 vec = mc.thePlayer.getLookVec();
        double x = mc.thePlayer.posX - vec.xCoord * 2.0D;
        double y = mc.thePlayer.posY + ((double) mc.thePlayer.getEyeHeight() - 0.2D);
        double z = mc.thePlayer.posZ - vec.zCoord * 2.0D;
        mc.thePlayer.worldObj.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D, 0);
    }
}
