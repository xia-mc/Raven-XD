package keystrokesmod.mixins.impl.client;

import keystrokesmod.event.ClickEvent;
import keystrokesmod.event.PreTickEvent;
import keystrokesmod.event.RightClickEvent;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.client.Notifications;
import keystrokesmod.module.impl.combat.HitBox;
import keystrokesmod.module.impl.combat.Reach;
import keystrokesmod.module.impl.exploit.ExploitFixer;
import keystrokesmod.module.impl.render.Animations;
import keystrokesmod.module.impl.render.FreeLook;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class, priority = 1001)
public abstract class MixinMinecraft {

    @Shadow public GameSettings gameSettings;

    @Shadow public EntityPlayerSP thePlayer;

    @Shadow protected abstract void clickMouse();

    @Shadow public MovingObjectPosition objectMouseOver;

    @Inject(method = "runTick", at = @At("HEAD"))
    private void runTickPre(CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new PreTickEvent());
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;onStoppedUsingItem(Lnet/minecraft/entity/player/EntityPlayer;)V",
            shift = At.Shift.BY, by = 2
    ))
    private void onRunTick$usingWhileDigging(CallbackInfo ci) {
        if (ModuleManager.animations != null && ModuleManager.animations.isEnabled() && Animations.swingWhileDigging.isToggled()
                && this.gameSettings.keyBindAttack.isKeyDown()) {
            if (this.objectMouseOver != null && this.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                this.thePlayer.swingItem();
            }
        }
    }

    @Inject(method = "clickMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;swingItem()V"), cancellable = true)
    private void beforeSwingByClick(CallbackInfo ci) {
        ClickEvent event = new ClickEvent();
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled())
            ci.cancel();
    }

    /**
     * @author xia__mc
     * @reason to fix freelook do impossible action
     */
    @Inject(method = "rightClickMouse", at = @At("HEAD"), cancellable = true)
    private void onRightClickMouse(CallbackInfo ci) {
        RightClickEvent event = new RightClickEvent();
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled())
            ci.cancel();
    }

    @Inject(method = "crashed", at = @At("HEAD"), cancellable = true)
    private void onCrashed(CrashReport crashReport, CallbackInfo ci) {
        try {
            if (ExploitFixer.onCrash(crashReport)) {
                ci.cancel();
            }
        } catch (Throwable ignored) {
        }
    }
}
