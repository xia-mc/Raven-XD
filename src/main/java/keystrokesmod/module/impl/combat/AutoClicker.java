package keystrokesmod.module.impl.combat;

import keystrokesmod.event.ClickEvent;
import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.mixins.impl.client.MinecraftAccessor;
import keystrokesmod.mixins.impl.client.PlayerControllerMPAccessor;
import keystrokesmod.module.impl.combat.autoclicker.DragClickAutoClicker;
import keystrokesmod.module.impl.combat.autoclicker.IAutoClicker;
import keystrokesmod.module.impl.combat.autoclicker.NormalAutoClicker;
import keystrokesmod.module.impl.combat.autoclicker.RecordAutoClicker;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.ModeValue;
import keystrokesmod.utility.CoolDown;
import keystrokesmod.utility.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoClicker extends IAutoClicker {
    private final ModeValue mode;
    private final ButtonSetting breakBlocks;
    private final ButtonSetting jitter;
    private final ButtonSetting inventoryFill;
    private final ModeSetting clickSound;

    private final CoolDown coolDown = new CoolDown(100);
    private double directionX, directionY;

    private boolean inventoryClick = false;

    public AutoClicker() {
        super("AutoClicker", category.combat);
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new NormalAutoClicker("Normal", this, true, false))
                .add(new DragClickAutoClicker("Drag Click", this, true, false))
                .add(new RecordAutoClicker("Record", this, true, false))
                .setDefaultValue("Normal")
        );
        this.registerSetting(breakBlocks = new ButtonSetting("Break blocks", true));
        this.registerSetting(jitter = new ButtonSetting("Jitter", false));
        this.registerSetting(inventoryFill = new ButtonSetting("Inventory fill", false));
        this.registerSetting(clickSound = new ModeSetting("Click sound", new String[]{"None", "Standard", "Double", "Alan"}, 0));
    }

    @Override
    public void onEnable() {
        mode.enable();
    }

    @Override
    public void onDisable() {
        mode.disable();
    }

    @SubscribeEvent
    public void onClick(ClickEvent event) {
        coolDown.start();

        directionX = (Math.random() - 0.5) * 4;
        directionY = (Math.random() - 0.5) * 4;

        if (clickSound.getInput() != 0) {
            mc.thePlayer.playSound(
                    "keystrokesmod:click." + clickSound.getOptions()[(int) clickSound.getInput()].toLowerCase()
                    , 1, 1
            );
        }
    }

    @Override
    public void onUpdate() {
        ((MinecraftAccessor) mc).setLeftClickCounter(-1);

        if (!coolDown.hasFinished() && this.jitter.isToggled()) {
            mc.thePlayer.rotationYaw += (float) (((Math.random() - 0.5) * 400 / Minecraft.getDebugFPS()) * directionX);
            mc.thePlayer.rotationPitch += (float) (((Math.random() - 0.5) * 400 / Minecraft.getDebugFPS()) * directionY) * mc.gameSettings.mouseSensitivity * 2;
        }
    }

    @Override
    public boolean click() {
        if (mc.currentScreen == null && HitSelect.canAttack()) {
            if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                if (breakBlocks.isToggled()) {
                    return false;
                } else {
                    ((PlayerControllerMPAccessor) mc.playerController).setCurBlockDamageMP(0);
                }
            }

            Utils.sendClick(0, true);
            return true;
        } else if (inventoryFill.isToggled() && mc.currentScreen instanceof GuiContainer) {
            inventoryClick = true;
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent event) {
        if (mc.currentScreen instanceof GuiContainer) {
            if (inventoryClick) {
                Utils.inventoryClick(mc.currentScreen);
            }
        }
        inventoryClick = false;
    }
}
