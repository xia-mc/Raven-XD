package keystrokesmod.module.impl.movement.longjump;

import keystrokesmod.module.impl.client.Notifications;
import keystrokesmod.module.impl.movement.LongJump;
import keystrokesmod.module.impl.movement.longjump.hypixelfireball.NormalHypixelFireballLongJump;
import keystrokesmod.module.impl.movement.longjump.hypixelfireball.SameYHypixelFireballLongJump;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.*;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class HypixelFireballLongJump extends SubMode<LongJump> {
    public final SliderSetting speed;
    private final ModeValue mode;
    public final ButtonSetting autoDisable;

    private int lastSlot = -1;

    public HypixelFireballLongJump(String name, @NotNull LongJump parent) {
        super(name, parent);
        this.registerSetting(speed = new SliderSetting("Speed", 1.5, 0, 2, 0.01));
        this.registerSetting(mode = new ModeValue("Sub-Mode", this)
                .add(new NormalHypixelFireballLongJump("Normal", this))
                .add(new SameYHypixelFireballLongJump("SameY", this))
        );
        this.registerSetting(autoDisable = new ButtonSetting("Auto disable", true));
    }

    @Override
    public void onDisable() {
        mode.disable();
        if (lastSlot != -1) {
            SlotHandler.setCurrentSlot(lastSlot);
        }
    }

    @Override
    public void onEnable() {
        if (getFireball() == -1) {
            Notifications.sendNotification(Notifications.NotificationTypes.INFO, "Could not find Fireball");
            parent.disable();
        } else {
            lastSlot = SlotHandler.getCurrentSlot();
        }
        mode.enable();
    }

    public int getFireball() {
        int a = -1;
        for (int i = 0; i < 9; ++i) {
            final ItemStack getStackInSlot = mc.thePlayer.inventory.getStackInSlot(i);
            if (getStackInSlot != null && getStackInSlot.getItem() == Items.fire_charge) {
                a = i;
                break;
            }
        }
        return a;
    }
}
