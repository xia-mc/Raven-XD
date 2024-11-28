package keystrokesmod.module.impl.world;

import keystrokesmod.mixins.impl.client.KeyBindingAccessor;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.other.SlotHandler;
import keystrokesmod.module.impl.render.FreeLook;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.MoveUtil;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.render.RenderUtils;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

public class LegitScaffold extends Module {
    private final SliderSetting minDelay = new SliderSetting("Min delay", 100, 0, 500, 1, "ms");
    private final SliderSetting maxDelay = new SliderSetting("Max delay", 200, 0, 500, 1, "ms");
    private final ButtonSetting pitchCheck = new ButtonSetting("Pitch check", true);
    private final SliderSetting pitch = new SliderSetting("Pitch", 45, 0, 90, 5, pitchCheck::isToggled);
    private final ButtonSetting onlySPressed = new ButtonSetting("Only S pressed", false);
    private final ButtonSetting onlySneak = new ButtonSetting("Only sneak", false);
    private final ButtonSetting showBlockCount = new ButtonSetting("Show block count", false);

    private long lastSneakTime = -1;

    public LegitScaffold() {
        super("Legit scaffold", category.world);
        this.registerSetting(minDelay, maxDelay, pitchCheck, pitch, onlySPressed, onlySneak, showBlockCount);
    }

    @Override
    public void guiUpdate() {
        Utils.correctValue(minDelay, maxDelay);
    }

    @Override
    public void onDisable() {
        lastSneakTime = -1;
        setSneak(Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode()));
    }

    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event) {
        if (!Utils.nullCheck() || mc.currentScreen != null) return;

        if ((onlySPressed.isToggled() && !mc.gameSettings.keyBindBack.isKeyDown())
                || (pitchCheck.isToggled() && (FreeLook.viewData != null ? FreeLook.viewData.rotationPitch : mc.thePlayer.rotationPitch) < pitch.getInput())
                || (onlySneak.isToggled() && !Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode()))
        ) {
            setSneak(Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode()));
            return;
        }

        ItemStack item = SlotHandler.getHeldItem();
        if (showBlockCount.isToggled()) {
            if (item != null && item.getItem() instanceof ItemBlock) {
                RenderUtils.drawText(String.valueOf(item.stackSize));
            } else {
                RenderUtils.drawText("0");
            }
        }

        final long currentTime = System.currentTimeMillis();
        if ((Utils.overAir() || Utils.onEdge()) && !(!MoveUtil.isMoving() && Utils.jumpDown())) {
            setSneak(true);
            lastSneakTime = currentTime;
        } else if (lastSneakTime != -1
                && currentTime - lastSneakTime > Math.random() * (maxDelay.getInput() - minDelay.getInput()) + minDelay.getInput()) {
            setSneak(false);
            lastSneakTime = -1;
        }
    }

    private void setSneak(boolean sneak) {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindSneak.getKeyCode(), sneak);
        ((KeyBindingAccessor) mc.gameSettings.keyBindSneak).setPressed(sneak);
    }
}
