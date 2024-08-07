package keystrokesmod.module.impl.fun;

import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.impl.other.anticheats.utils.world.PlayerRotation;
import keystrokesmod.module.impl.world.AntiBot;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.script.classes.Vec3;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.RotationUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

public class BlockOut extends Module {
    private final SliderSetting range;
    private final ButtonSetting aim;
    private final ButtonSetting ignoreTeammates;

    public BlockOut() {
        super("Block-Out", category.fun);
        this.registerSetting(range = new SliderSetting("Range", 6, 3, 6, 0.1));
        this.registerSetting(aim = new ButtonSetting("Aim", true));
        this.registerSetting(ignoreTeammates = new ButtonSetting("Ignore teammates", true));
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        if (!Utils.nullCheck()) return;

        mc.theWorld.playerEntities.parallelStream()
                .filter(p -> p != mc.thePlayer)
                .filter(p -> p instanceof AbstractClientPlayer)
                .map(p -> (AbstractClientPlayer) p)
                .filter(p -> p.getDistanceSqToEntity(mc.thePlayer) <= range.getInput() * range.getInput())
                .filter(p -> !AntiBot.isBot(p))
                .filter(p -> !(ignoreTeammates.isToggled() && Utils.isTeamMate(p)))
                .min(Comparator.comparingDouble(p -> mc.thePlayer.getDistanceSqToEntity(p)))
                .ifPresent(target -> {
                    Set<BlockPos> blocks = BlockUtils.getSurroundBlocks(target);
                    for (BlockPos block : blocks) {
                        if (!BlockUtils.replaceable(block)) continue;

                        Optional<Triple<BlockPos, EnumFacing, Vec3>> placeSide = RotationUtils.getPlaceSide(block);
                        if (!placeSide.isPresent()) continue;

                        if (aim.isToggled()) {
                            RotationHandler.setRotationYaw(PlayerRotation.getYaw(placeSide.get().getRight()));
                            RotationHandler.setRotationPitch(PlayerRotation.getPitch(placeSide.get().getRight()));
                        }

                        if (mc.playerController.onPlayerRightClick(
                                mc.thePlayer, mc.theWorld,
                                SlotHandler.getHeldItem(),
                                placeSide.get().getLeft(), placeSide.get().getMiddle(), placeSide.get().getRight().toVec3()
                        ))
                            return;
                    }
                });
    }
}
