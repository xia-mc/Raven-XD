package keystrokesmod.module.impl.world;

import keystrokesmod.event.PreMotionEvent;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;


public class AutoWeapon extends Module {
    private final SliderSetting hoverDelay;
    private final ButtonSetting swap;
    private final ButtonSetting ignoreTeammates;
    private final ButtonSetting onlyWhenHoldingDown;
    private int previousSlot = -1;
    private int ticksHovered;
    private Entity currentEntity;

    public AutoWeapon() {
        super("AutoWeapon", category.world);
        this.registerSetting(hoverDelay = new SliderSetting("Hover delay", 0.0, 0.0, 20.0, 1.0));
        this.registerSetting(swap = new ButtonSetting("Swap to previous slot", true));
        this.registerSetting(onlyWhenHoldingDown = new ButtonSetting("Only when holding lmb", true));
        this.registerSetting(ignoreTeammates = new ButtonSetting("Ignore teammates", true));
        this.registerSetting(new DescriptionSetting("Configure your weapons in the Settings tab."));
    }

    public void onDisable() {
        resetVariables();
    }

    public void setSlot(final int currentItem) {
        if (currentItem == -1) {
            return;
        }
        SlotHandler.setCurrentSlot(currentItem);
    }

    @SubscribeEvent
    public void onPreMotion(PreMotionEvent e) {
        if (!Utils.nullCheck() || !mc.inGameHasFocus || mc.currentScreen != null) {
            resetSlot();
            resetVariables();
            return;
        }
        Entity hoveredEntity = mc.objectMouseOver != null ? mc.objectMouseOver.entityHit : null;
        if (!(hoveredEntity instanceof EntityLivingBase)
                || (hoveredEntity instanceof EntityPlayer && AntiBot.isBot(hoveredEntity))
                || (hoveredEntity instanceof EntityPlayer && Utils.isFriended((EntityPlayer) hoveredEntity))
                || (ignoreTeammates.isToggled() && Utils.isTeamMate(hoveredEntity) || (onlyWhenHoldingDown.isToggled() && !Mouse.isButtonDown(0)))
        ) {
            resetSlot();
            resetVariables();
            return;
        }
        ticksHovered = hoveredEntity.equals(currentEntity) ? ticksHovered + 1 : 0;
        currentEntity = hoveredEntity;

        if (hoverDelay.getInput() == 0 || ticksHovered > hoverDelay.getInput()) {
            int slot = Utils.getWeapon();
            if (slot != -1) {
                if (previousSlot == -1) {
                    previousSlot = SlotHandler.getCurrentSlot();
                }
                setSlot(slot);
            }
        }
    }

    private void resetVariables() {
        ticksHovered = 0;
        resetSlot();
        previousSlot = -1;
    }

    private void resetSlot() {
        if (previousSlot == -1 || !swap.isToggled()) {
            return;
        }
        setSlot(previousSlot);
        previousSlot = -1;
    }
}
