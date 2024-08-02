package keystrokesmod.module.impl.world;

import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.event.RotationEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.other.RotationHandler;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.AimSimulator;
import keystrokesmod.utility.RotationUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

public class WaterBucket extends Module {
    private final SliderSetting aimSpeed;
    private final SliderSetting minDistance;
    private final ButtonSetting silentAim;
    private final ButtonSetting switchToItem;

    private float lastPitch = -1;

    public WaterBucket() {
        super("Water bucket", category.world, 0);
        this.registerSetting(aimSpeed = new SliderSetting("Aim speed", 5, 5, 10, 0.1));
        this.registerSetting(minDistance = new SliderSetting("Min distance", 3, 0, 20, 0.5));
        this.registerSetting(silentAim = new ButtonSetting("Silent aim", true));
        this.registerSetting(switchToItem = new ButtonSetting("Switch to item", true));
    }

    @SubscribeEvent
    public void onPreUpdate(@NotNull PreUpdateEvent event) {
        if (inPosition()) {
            MovingObjectPosition rayCast = RotationUtils.rayCast(mc.playerController.getBlockReachDistance(), RotationHandler.getRotationYaw(), RotationHandler.getRotationPitch());
            if (rayCast != null && rayCast.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && holdWaterBucket(switchToItem.isToggled())) {
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
                && !mc.thePlayer.onGround && !mc.thePlayer.isInWater() && mc.thePlayer.fallDistance >= minDistance.getInput() && !Utils.overVoid();
    }

    private boolean holdWaterBucket(boolean setSlot) {
        if (this.containsWater(SlotHandler.getHeldItem())) {
            return true;
        } else {
            for (int i = 0; i < InventoryPlayer.getHotbarSize(); ++i) {
                if (this.containsWater(mc.thePlayer.inventory.mainInventory[i]) && setSlot) {
                    SlotHandler.setCurrentSlot(i);
                    return true;
                }
            }

            return false;
        }
    }

    private boolean containsWater(ItemStack itemStack) {
        return itemStack != null && itemStack.getItem() == Items.water_bucket;
    }

    private void sendPlace() {
        mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
    }
}
