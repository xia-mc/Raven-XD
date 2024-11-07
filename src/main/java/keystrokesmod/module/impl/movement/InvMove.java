package keystrokesmod.module.impl.movement;

import keystrokesmod.clickgui.ClickGui;
import keystrokesmod.event.MoveInputEvent;
import keystrokesmod.event.PreUpdateEvent;
import keystrokesmod.event.SendPacketEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.PacketUtils;
import keystrokesmod.utility.Utils;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.network.play.client.C0EPacketClickWindow;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Keyboard;

import static keystrokesmod.module.ModuleManager.*;

public class InvMove extends Module {
    public static final String[] MODES = {"Normal", "Blink", "LegitInv", "Hypixel", "None"};
    private final ModeSetting mode;
    private final ButtonSetting container;
    private final ButtonSetting inventory;
    private final ButtonSetting noOpenPacket;
    private final ButtonSetting allowSprint;
    private final ButtonSetting allowSneak;
    private final ButtonSetting chestNameCheck;
    private final ButtonSetting targetNearbyCheck;
    private final ButtonSetting clickGui;

    private boolean blinking = false;

    private boolean clicked = false;

    public InvMove() {
        super("InvMove", category.movement);
        this.registerSetting(new DescriptionSetting("Allow you move in inventory."));
        this.registerSetting(mode = new ModeSetting("Mode", MODES, 0));
        this.registerSetting(container = new ButtonSetting("Container", true));
        this.registerSetting(inventory = new ButtonSetting("Inventory", true));
        this.registerSetting(clickGui = new ButtonSetting("Click gui", true));
        this.registerSetting(new DescriptionSetting("Advanced"));
        this.registerSetting(noOpenPacket = new ButtonSetting("No open packet", false));
        this.registerSetting(allowSprint = new ButtonSetting("Allow sprint", true));
        this.registerSetting(allowSneak = new ButtonSetting("Allow sneak", false));
        this.registerSetting(chestNameCheck = new ButtonSetting("Chest name check", true));
        this.registerSetting(targetNearbyCheck = new ButtonSetting("Target nearby check", true));
    }

    @SubscribeEvent
    public void onMoveInput(MoveInputEvent event) {
        if (mode.getInput() == 3 && canInvMove() && !(mc.currentScreen instanceof ClickGui))
            event.setJump(false);
    }

    @SubscribeEvent
    public void onPreUpdate(PreUpdateEvent event) {
        if (canInvMove()) {
            if (mc.currentScreen instanceof ClickGui && clickGui.isToggled()) {
                doInvMove();
                return;
            }
            switch ((int) mode.getInput()) {
                case 1:
                    if (!blinking) {
                        blink.enable();
                    }
                    blinking = true;
                    break;
                case 2:
                    if (!(mc.currentScreen instanceof GuiInventory) || clicked) {
                        noInvMove();
                        return;
                    }
                    break;
                case 3:
                    MoveUtil.stop();
                    break;
                case 4:
                    return;
            }

            doInvMove();
        } else {
            switch ((int) mode.getInput()) {
                case 1:
                    if (blinking && blink.isEnabled()) {
                        blink.disable();
                    }
                    blinking = false;
                    break;
                case 2:
                    if (mc.currentScreen instanceof GuiContainer)
                        noInvMove();
                    clicked = false;
                    break;
            }
        }
    }

    private boolean canInvMove() {
        if (!nameCheck() || !targetNearbyCheck() || scaffold.isEnabled())
            return false;
        if (clickGui.isToggled())
            return mc.currentScreen instanceof ClickGui;
        if (inventory.isToggled())
            return mc.currentScreen instanceof GuiInventory;
        if (container.isToggled())
            return mc.currentScreen instanceof GuiContainer;
        if (inventory.isToggled() && mc.currentScreen instanceof GuiInventory) return true;
        if (container.isToggled() && mc.currentScreen instanceof GuiContainer) return true;
        return false;
    }

    @SubscribeEvent
    public void onSendPacket(SendPacketEvent event) {
        if (noOpenPacket.isToggled() && event.getPacket() instanceof C0BPacketEntityAction) {
            if (((C0BPacketEntityAction) event.getPacket()).getAction() == C0BPacketEntityAction.Action.OPEN_INVENTORY) {
                event.setCanceled(true);
            }
        }

        if ((int) mode.getInput() != 2) return;

        if (event.getPacket() instanceof C0BPacketEntityAction) {
            C0BPacketEntityAction packet = (C0BPacketEntityAction) event.getPacket();

            if (packet.getAction() == C0BPacketEntityAction.Action.OPEN_INVENTORY) {
                clicked = false;
                event.setCanceled(true);
            }
        } else if (event.getPacket() instanceof C0EPacketClickWindow) {
            if (!clicked && !noOpenPacket.isToggled()) {
                PacketUtils.sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.OPEN_INVENTORY));
            }
            clicked = true;
        }
    }

    private void doInvMove() {
        if (allowSprint.isToggled()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindSprint.getKeyCode()));

            // If sprint is enabled, set the sprint key to true
            if (sprint.isEnabled()) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
            }
        }
        if (allowSneak.isToggled()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode()));
        }
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()));
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode()));
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode()));
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode()));
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), Utils.jumpDown());
    }

    private void noInvMove() {
        if (allowSprint.isToggled()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), false);
        }
        if (allowSneak.isToggled()) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), false);
        }
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindRight.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindLeft.getKeyCode(), false);
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);
    }

    private boolean nameCheck() {
        if (!chestNameCheck.isToggled()) return true;
        if (!(mc.thePlayer.openContainer instanceof ContainerChest)) return true;

        return ((ContainerChest) mc.thePlayer.openContainer).getLowerChestInventory().getName().equals("Chest");
    }

    private boolean targetNearbyCheck() {
        if (!targetNearbyCheck.isToggled()) return true;

        return !Utils.isTargetNearby();
    }

    @Override
    public void onDisable() {
        if (blinking && blink.isEnabled()) {
            blink.disable();
        }
        blinking = false;

        if (mc.currentScreen instanceof GuiInventory && !clicked) {
            PacketUtils.sendPacketNoEvent(new C0BPacketEntityAction(mc.thePlayer, C0BPacketEntityAction.Action.OPEN_INVENTORY));
        }
        clicked = false;
    }

    @Override
    public void onEnable() {
        clicked = mc.currentScreen instanceof GuiInventory;
    }

    @Override
    public String getInfo() {
        return MODES[(int) mode.getInput()];
    }
}
