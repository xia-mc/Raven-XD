package keystrokesmod.module.impl.movement.noslow;

import keystrokesmod.event.*;
import keystrokesmod.module.impl.movement.NoSlow;
import keystrokesmod.module.impl.movement.Sprint;
import keystrokesmod.module.impl.movement.noslow.customnoslow.SimpleCustomNoSlow;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.ModeValue;
import keystrokesmod.utility.ContainerUtils;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import net.minecraft.item.ItemBow;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomNoSlow extends INoSlow {
    private final ButtonSetting onlyWhileMove;
    private final ButtonSetting startSlow;
    public final ModeSetting switchMode;
    public final ModeSetting slotMode;
    private final ModeSetting mode;
    private final ModeValue sword;
    private final ModeValue bow;
    private final ModeValue rest;

    private int usingTicks = 0;

    public CustomNoSlow(String name, @NotNull NoSlow parent) {
        super(name, parent);
        this.registerSetting(onlyWhileMove = new ButtonSetting("Only while move", false));
        this.registerSetting(startSlow = new ButtonSetting("Start slow", false));
        this.registerSetting(switchMode = new ModeSetting("Switch mode", new String[]{"PreAttack", "PostAttack", "PrePosition", "PostPosition"}, 0));
        this.registerSetting(slotMode = new ModeSetting("Slot mode", new String[]{"PreAttack", "PostAttack", "PrePosition", "PostPosition"}, 0));
        this.registerSetting(mode = new ModeSetting("Sprint mode", new String[]{"Legit", "LegitMotion", "AllDirection", "AllDirectionMotion"}, 0));
        this.registerSetting(new DescriptionSetting("Sword"));
        this.registerSetting(sword = new ModeValue("Sword", this, () -> false).add(new SimpleCustomNoSlow("Sword", this)));
        this.registerSetting(new DescriptionSetting("Bow"));
        this.registerSetting(bow = new ModeValue("Bow", this, () -> false).add(new SimpleCustomNoSlow("Bow", this)));
        this.registerSetting(new DescriptionSetting("Rest"));
        this.registerSetting(rest = new ModeValue("Rest", this, () -> false).add(new SimpleCustomNoSlow("Rest", this)));
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPreUpdate(PreUpdateEvent event) {
        if (noAction()) return;
        SimpleCustomNoSlow noSlow = getNoSlow();
        if (noSlow != null)
            noSlow.onPreUpdate(event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPostUpdate(PostUpdateEvent event) {
        if (noAction()) return;
        SimpleCustomNoSlow noSlow = getNoSlow();
        if (noSlow != null)
            noSlow.onPostUpdate(event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer.isUsingItem()) {
            usingTicks++;

            if (mode.getInput() == 1 || mode.getInput() == 3)
                event.setSprinting(false);
            if (mode.getInput() == 2 || mode.getInput() == 3)
                Sprint.omni = true;
        } else {
            if (usingTicks > 0) {
                Sprint.omni = false;
            }

            usingTicks = 0;
        }

        if (noAction()) return;
        SimpleCustomNoSlow noSlow = getNoSlow();
        if (noSlow != null)
            noSlow.onPreMotion(event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPostMotion(PostMotionEvent event) {
        if (noAction()) return;
        SimpleCustomNoSlow noSlow = getNoSlow();
        if (noSlow != null)
            noSlow.onPostMotion(event);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSendPacket(SendPacketEvent event) {
        if (noAction()) return;
        SimpleCustomNoSlow noSlow = getNoSlow();
        if (noSlow != null)
            noSlow.onSendPacket(event);
    }

    private boolean noAction() {
        return onlyWhileMove.isToggled() && !MoveUtil.isMoving();
    }

    @Override
    public void onEnable() {
        sword.enable();
        bow.enable();
        rest.enable();
    }

    @Override
    public void onDisable() {
        sword.disable();
        bow.disable();
        rest.disable();

        if (usingTicks > 0) {
            Sprint.omni = false;
        }
    }

    private @Nullable SimpleCustomNoSlow getNoSlow() {
        if (mc.thePlayer.isUsingItem() && SlotHandler.getHeldItem() != null) {
            if (Utils.holdingSword())
                return (SimpleCustomNoSlow) sword.getSubModeValues().get(0);
            else if (SlotHandler.getHeldItem().getItem() instanceof ItemBow)
                return (SimpleCustomNoSlow) bow.getSubModeValues().get(0);
            else if (ContainerUtils.isRest(SlotHandler.getHeldItem().getItem()))
                return (SimpleCustomNoSlow) rest.getSubModeValues().get(0);
        }
        return null;
    }

    @Override
    public float getSlowdown() {
        if (startSlow.isToggled() && usingTicks <= 1) return 0.2f;

        SimpleCustomNoSlow noSlow = getNoSlow();
        if (noSlow != null)
            return noSlow.getForwardSlowed();
        return 0.2f;
    }

    @Override
    public float getStrafeSlowdown() {
        if (startSlow.isToggled() && usingTicks <= 1) return 0.2f;

        SimpleCustomNoSlow noSlow = getNoSlow();
        if (noSlow != null)
            return noSlow.getStrafeSlowed();
        return 0.2f;
    }
}
