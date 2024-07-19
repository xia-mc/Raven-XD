package keystrokesmod.module.impl.combat;

import keystrokesmod.event.ClickEvent;
import keystrokesmod.mixins.impl.client.MinecraftAccessor;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.combat.autoclicker.DragClickAutoClicker;
import keystrokesmod.module.impl.combat.autoclicker.IAutoClicker;
import keystrokesmod.module.impl.combat.autoclicker.NormalAutoClicker;
import keystrokesmod.module.impl.combat.autoclicker.RecordAutoClicker;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.ModeValue;
import keystrokesmod.utility.CoolDown;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoClicker extends IAutoClicker {
    private final ModeValue mode;
    private final ButtonSetting jitter;
    private final ButtonSetting inventoryFill;
    private final ModeSetting clickSound;

    private final CoolDown coolDown = new CoolDown(100);
    private double directionX, directionY;

    public AutoClicker() {
        super("AutoClicker", category.combat);
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new NormalAutoClicker("Normal", this, true))
                .add(new DragClickAutoClicker("Drag Click", this, true))
                .add(new RecordAutoClicker("Record", this, true))
                .setDefaultValue("Normal")
        );
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

        if (!coolDown.hasFinished() && this.jitter.isToggled() && mc.gameSettings.keyBindUseItem.isKeyDown()) {
            mc.thePlayer.rotationYaw += (float) (((Math.random() - 0.5) * 400 / Minecraft.getDebugFPS()) * directionX);
            mc.thePlayer.rotationPitch += (float) (((Math.random() - 0.5) * 400 / Minecraft.getDebugFPS()) * directionY) * mc.gameSettings.mouseSensitivity * 2;
        }
    }

    @Override
    public boolean isInventoryFill() {
        return inventoryFill.isToggled();
    }
}
