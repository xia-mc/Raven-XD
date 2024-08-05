package keystrokesmod.module.impl.render;

import keystrokesmod.event.*;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.utility.RotationUtils;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;

import javax.annotation.Nullable;

public class FreeLook extends Module {
    private final ButtonSetting onlyIfPressed = new ButtonSetting("Only if pressed", true);

    public static @Nullable ViewData viewData = null;

    public FreeLook() {
        super("FreeLook", category.render);
        this.registerSetting(onlyIfPressed);
    }

    @Override
    public void onDisable() {
        if (viewData != null) {
            mc.gameSettings.thirdPersonView = viewData.thirdPersonView;
            mc.thePlayer.rotationYaw = viewData.rotationYaw;
            mc.thePlayer.rotationPitch = viewData.rotationPitch;
        }
        viewData = null;
    }

    public static void call() {
        if (ModuleManager.freeLook.isEnabled() && FreeLook.viewData != null) {
            mc.objectMouseOver = RotationUtils.rayCast(
                    mc.playerController.getBlockReachDistance(),
                    FreeLook.viewData.rotationYaw,
                    FreeLook.viewData.rotationPitch
            );
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPreMotion(RotationEvent event) {
        try {
            if (onlyIfPressed.isToggled() && !Keyboard.isKeyDown(this.getKeycode())) {
                disable();
                return;
            }
        } catch (IndexOutOfBoundsException ignored) {
        }

        if (viewData != null){
            call();
            if (!event.isSet()) {
                event.setYaw(viewData.rotationYaw);
                event.setPitch(viewData.rotationPitch);
            }
        } else {
            viewData = new ViewData(
                    mc.gameSettings.thirdPersonView,
                    event.getYaw(),
                    event.getPitch()
            );
            mc.gameSettings.thirdPersonView = 1;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPreInput(@NotNull PrePlayerInputEvent event) {
        if (viewData != null) {
            event.setYaw(viewData.rotationYaw);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onJump(JumpEvent event) {
        if (viewData != null) {
            event.setYaw(viewData.rotationYaw);
        }
    }

    public static class ViewData {
        public final int thirdPersonView;
        public final float rotationYaw;
        public final float rotationPitch;

        public ViewData(int thirdPersonView, float rotationYaw, float rotationPitch) {
            this.thirdPersonView = thirdPersonView;
            this.rotationYaw = rotationYaw;
            this.rotationPitch = rotationPitch;
        }
    }
}
