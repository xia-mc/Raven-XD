package keystrokesmod.module.impl.movement.fly;

import keystrokesmod.event.ScaffoldPlaceEvent;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.movement.Fly;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SubMode;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

import static keystrokesmod.module.ModuleManager.scaffold;

public class SpoofFly extends SubMode<Fly> {
    private final ButtonSetting keep;

    private static final Set<BlockPos> hiddenPos = new HashSet<>();

    public SpoofFly(String name, @NotNull Fly parent) {
        super(name, parent);
        this.registerSetting(keep = new ButtonSetting("Keep", true));

        FMLCommonHandler.instance().bus().register(new Object() {
            @SubscribeEvent
            public void onWorldChange(@NotNull EntityJoinWorldEvent event) {
                if (event.entity == mc.thePlayer) {
                    hiddenPos.clear();
                }
            }
        });
    }

    public static boolean isHidden(BlockPos pos) {
        return hiddenPos.contains(pos);
    }

    public static boolean hideRotation() {
        return ModuleManager.fly.isEnabled() && ModuleManager.fly.mode.getSubModeValues().get((int) ModuleManager.fly.mode.getInput()) instanceof SpoofFly;
    }

    @Override
    public void onEnable() {
        scaffold.setHidden(true);
        scaffold.enable();
    }

    @Override
    public void onDisable() {
        scaffold.disable();
        scaffold.setHidden(false);

        if (!keep.isToggled()) {
            hiddenPos.clear();
        }
    }

    @Override
    public void onUpdate() {
        scaffold.setHidden(true);
        scaffold.enable();
    }

    @SubscribeEvent
    public void onPlace(@NotNull ScaffoldPlaceEvent event) {
        MovingObjectPosition hitResult = event.getHitResult();
        hiddenPos.add(hitResult.getBlockPos().offset(hitResult.sideHit));
    }
}
