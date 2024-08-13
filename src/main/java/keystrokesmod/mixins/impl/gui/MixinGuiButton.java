package keystrokesmod.mixins.impl.gui;

import keystrokesmod.module.ModuleManager;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.font.IFont;
import keystrokesmod.utility.render.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;


@Mixin(GuiButton.class)
public abstract class MixinGuiButton extends Gui {

    @Shadow public boolean visible;

    @Shadow @Final protected static ResourceLocation buttonTextures;

    @Shadow protected boolean hovered;

    @Shadow public int xPosition;

    @Shadow public int yPosition;

    @Shadow public int width;

    @Shadow public int height;

    @Shadow protected abstract int getHoverState(boolean p_getHoverState_1_);

    @Shadow protected abstract void mouseDragged(Minecraft p_mouseDragged_1_, int p_mouseDragged_2_, int p_mouseDragged_3_);

    @Shadow public boolean enabled;

    @Shadow public String displayString;

    @Unique
    private int ravenXD$hoverValue;

    @Inject(method = "drawButton", at = @At("HEAD"), cancellable = true)
    public void onDrawButton(Minecraft minecraft, int x, int y, CallbackInfo ci) {
        if (!ModuleManager.clientTheme.isEnabled() || !ModuleManager.clientTheme.button.isToggled())
            return;

        if (this.visible) {
            IFont font = FontManager.tenacity20;
            this.hovered = x >= this.xPosition && y >= this.yPosition && x < this.xPosition + this.width && y < this.yPosition + this.height;

            if (hovered)
                ravenXD$hoverValue = Math.min(ravenXD$hoverValue + 70, 200);
            else
                ravenXD$hoverValue = Math.max(ravenXD$hoverValue - 70, 102);

            Color rectColor = new Color(35, 37, 43, ravenXD$hoverValue);
            rectColor = raven_XD$interpolateColorC(rectColor, ColorUtils.brighter(rectColor, 0.4f), -1);
            RenderUtils.drawBloomShadow(xPosition - 3, yPosition - 3, width + 6, height + 6, 12, new Color(0, 0, 0, 50), false);
            RRectUtils.drawRoundOutline(xPosition, this.yPosition, width, height, (float)3.5, 0.0015f, rectColor, new Color(30, 30, 30, 100));
            RRectUtils.drawRoundOutline(xPosition, this.yPosition, width, height, (float)3.5, 0.0015f,  new Color(0, 0, 0, 5) , new Color(0, 0, 0, 5));
            RRectUtils.drawRoundOutline(xPosition, yPosition, width, height, (float)3.5, 0.0015f,  new Color(0, 0, 0, 50) , new Color(200, 200, 200, 60));

            font.drawCenteredString(displayString, this.xPosition + this.width / 2.0f, this.yPosition + height / 2f - font.height() / 2f, -1);
        }

        ci.cancel();
    }

    @Unique
    @Contract("_, _, _ -> new")
    private static @NotNull Color raven_XD$interpolateColorC(final @NotNull Color color1, final @NotNull Color color2, float amount) {
        amount = Math.min(1.0f, Math.max(0.0f, amount));
        return new Color(ColorUtils.interpolateInt(color1.getRed(), color2.getRed(), amount), ColorUtils.interpolateInt(color1.getGreen(), color2.getGreen(), amount), ColorUtils.interpolateInt(color1.getBlue(), color2.getBlue(), amount), ColorUtils.interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }
}
