package keystrokesmod.module.impl.player;

import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.event.RotationEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.aim.AimSimulator;
import keystrokesmod.utility.BlockUtils;
import keystrokesmod.utility.RotationUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class AutoWeb extends Module {
    private final SliderSetting aimSpeed;
    private final SliderSetting minDistance;
    private final ButtonSetting silentAim;
    private final ButtonSetting switchToItem;

    private float lastPitch = -1;

    public AutoWeb() {
        super("AutoWeb", category.player, "auto place webs your opponent when in a radius");
        this.registerSetting(aimSpeed = new SliderSetting("Aim speed", 5, 5, 10, 0.1));
        this.registerSetting(minDistance = new SliderSetting("Min distance", 5, 1, 10, 0.5));
        this.registerSetting(silentAim = new ButtonSetting("Silent aim", true));
        this.registerSetting(switchToItem = new ButtonSetting("Switch to item", true));
    }


    @SubscribeEvent
    public void onPreUpdate(@NotNull PreUpdateEvent event) {
        if (inPosition()) {
            MovingObjectPosition rayCast = RotationUtils.rayCast(mc.playerController.getBlockReachDistance(), RotationHandler.getRotationYaw(), RotationHandler.getRotationPitch());
            if (rayCast != null && rayCast.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && rayCast.getBlockPos().equals(new BlockPos(mc.thePlayer))
                    && holdItem(switchToItem.isToggled())) {
                sendPlace();
            }
        }
    }

    @SubscribeEvent
    public void onRotation(RotationEvent event) {
        if (inPosition()) {
            if (lastPitch == -1) {
                lastPitch = event.getPitch();
            }

            lastPitch = AimSimulator.rotMove(90, lastPitch, (float) aimSpeed.getInput());
            if (silentAim.isToggled())
                event.setPitch(lastPitch);
            else
                mc.thePlayer.rotationPitch = lastPitch;
        }
    }

    @Override
    public void onEnable() {
        lastPitch = -1;
    }

    private boolean inPosition() {
        return !mc.thePlayer.capabilities.isFlying && !mc.thePlayer.capabilities.isCreativeMode
                && !Utils.inLiquid() && Utils.isTargetNearby(minDistance.getInput()) && BlockUtils.getBlock(new BlockPos(mc.thePlayer)) != Blocks.web;
    }

    private boolean holdItem(boolean setSlot) {
        if (this.containsItem(SlotHandler.getHeldItem())) {
            return true;
        } else {
            for (int i = 0; i < InventoryPlayer.getHotbarSize(); ++i) {
                if (this.containsItem(mc.thePlayer.inventory.mainInventory[i]) && setSlot) {
                    SlotHandler.setCurrentSlot(i);
                    return true;
                }
            }

            return false;
        }
    }

    private boolean containsItem(ItemStack itemStack) {
        if (itemStack == null) return false;

        return ((ItemBlock) itemStack.getItem()).getBlock() == Blocks.web;
    }

    private void sendPlace() {
        mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
    }
}
