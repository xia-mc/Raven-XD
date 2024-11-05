package keystrokesmod.mixins.impl.client;

import keystrokesmod.event.ClickEvent;
import keystrokesmod.event.PreTickEvent;
import keystrokesmod.event.RightClickEvent;
import keystrokesmod.event.WorldChangeEvent;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.combat.HitBox;
import keystrokesmod.module.impl.combat.Reach;
import keystrokesmod.module.impl.exploit.ExploitFixer;
import keystrokesmod.module.impl.render.Animations;
import keystrokesmod.module.impl.render.FreeLook;
import keystrokesmod.module.impl.render.Watermark;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.render.BackgroundUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.io.InputStream;

import static keystrokesmod.Raven.mc;

@Mixin(value = Minecraft.class, priority = 1001)
public abstract class MixinMinecraft {

    @Unique private @Nullable WorldClient raven_XD$lastWorld = null;

    @Inject(method = "runTick", at = @At("HEAD"))
    private void runTickPre(CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new PreTickEvent());

        if (raven_XD$lastWorld != mc.theWorld && Utils.nullCheck()) {
            MinecraftForge.EVENT_BUS.post(new WorldChangeEvent());
        }

        this.raven_XD$lastWorld = mc.theWorld;
    }

    @Inject(method = "runTick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;onStoppedUsingItem(Lnet/minecraft/entity/player/EntityPlayer;)V",
            shift = At.Shift.BY, by = 2
    ))
    private void onRunTick$usingWhileDigging(CallbackInfo ci) {
        if (ModuleManager.animations != null && ModuleManager.animations.isEnabled() && Animations.swingWhileDigging.isToggled()
                && mc.gameSettings.keyBindAttack.isKeyDown()) {
            if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                mc.thePlayer.swingItem();
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
     * @reason to fix reach and hitBox won't work with autoClicker
     */
    @Inject(method = "clickMouse", at = @At("HEAD"))
    private void onLeftClickMouse(CallbackInfo ci) {
        FreeLook.call();
        Reach.call();
        HitBox.call();
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

    @Inject(method = "createDisplay", at = @At(value = "RETURN"))
    private void onSetTitle(@NotNull CallbackInfo ci) {
        Display.setTitle("Opai " + Watermark.VERSION);
    }



    @Redirect(method = "drawSplashScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/DefaultResourcePack;getInputStream(Lnet/minecraft/util/ResourceLocation;)Ljava/io/InputStream;"))
    private InputStream modifyConstant(@NotNull DefaultResourcePack instance, ResourceLocation location) throws IOException {
        return instance.getInputStream(BackgroundUtils.getLogoPng());
    }
}
