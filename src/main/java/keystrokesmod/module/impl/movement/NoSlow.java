package keystrokesmod.module.impl.movement;

import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.movement.noslow.*;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeValue;
import keystrokesmod.utility.ContainerUtils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemSword;

import java.util.function.Supplier;

public class NoSlow extends Module {
    private static ModeValue mode = null;

    private static final Supplier<Boolean> nonCustomMode = () -> mode != null && !(mode.getSelected() instanceof CustomNoSlow);

    private static final ButtonSetting sword = new ButtonSetting("Sword", true, nonCustomMode);
    private static final ButtonSetting bow = new ButtonSetting("Bow", true, nonCustomMode);
    private static final ButtonSetting rest = new ButtonSetting("Rest", true, nonCustomMode);

    public NoSlow() {
        super("NoSlow", category.movement);
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new VanillaNoSlow("Vanilla", this))
                .add(new HypixelNoSlow("Hypixel", this))
                .add(new NCPNoSlow("NCP", this))
                .add(new IntaveNoSlow("Intave", this))
                .add(new OldIntaveNoSlow("Old Intave", this))
                .add(new OldGrimNoSlow("Old Grim", this))
                .add(new CustomNoSlow("Custom", this))
        );
        this.registerSetting(sword, bow, rest);
    }

    @Override
    public void onEnable() {
        mode.enable();
    }

    @Override
    public void onDisable() {
        mode.disable();
    }

    @Override
    public String getInfo() {
        return mode.getSubModeValues().get((int) mode.getInput()).getPrettyName();
    }

    public static float getForwardSlowed() {
        if (!mc.thePlayer.isUsingItem()) return 1;
        if (!ModuleManager.noSlow.isEnabled()) return 0.2f;
        if (nonCustomMode.get()) {
            assert SlotHandler.getHeldItem() != null;
            Item item = SlotHandler.getHeldItem().getItem();
            if (item instanceof ItemSword && !sword.isToggled())
                return 0.2f;
            if (item instanceof ItemBow && !bow.isToggled())
                return 0.2f;
            if (ContainerUtils.isRest(item) && !rest.isToggled())
                return 0.2f;
        }
        return ((INoSlow) mode.getSubModeValues().get((int) mode.getInput())).getSlowdown();
    }

    public static float getStrafeSlowed() {
        if (!mc.thePlayer.isUsingItem()) return 1;
        if (!ModuleManager.noSlow.isEnabled()) return 0.2f;
        if (nonCustomMode.get()) {
            assert SlotHandler.getHeldItem() != null;
            Item item = SlotHandler.getHeldItem().getItem();
            if (item instanceof ItemSword && !sword.isToggled())
                return 0.2f;
            if (item instanceof ItemBow && !bow.isToggled())
                return 0.2f;
            if (ContainerUtils.isRest(item) && !rest.isToggled())
                return 0.2f;
        }
        return ((INoSlow) mode.getSubModeValues().get((int) mode.getInput())).getStrafeSlowdown();
    }
}
