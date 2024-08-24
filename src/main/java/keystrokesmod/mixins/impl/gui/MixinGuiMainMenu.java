package keystrokesmod.mixins.impl.gui;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import keystrokesmod.Raven;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.render.BackgroundUtils;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Mixin(value = GuiMainMenu.class, priority = 1983)
public abstract class MixinGuiMainMenu extends GuiScreen {
    @Unique
    private static final int LOGO_COLOR = new Color(255, 255, 255, 200).getRGB();

    @Shadow private int field_92022_t;

    @Shadow protected abstract boolean func_183501_a();

    @Shadow private GuiScreen field_183503_M;

    private static final List<String> SPLASH_TEXTS = Arrays.asList(
            "Raven... but funny?",
            "beep boop beep",
            "Made with... something by xia__mc!",
            "System.out.println(\"Hello world!\");!",
            "Make sure to thank the contributors!",
            "Sub to xia__mc & qloha on YT!",
            "i <3 java"
    );
    private String splashText;

    // Add variables for animation
    private long startTime;
    private static final float ANIMATION_DURATION = 500.0f; // 0.5 second

    @Inject(method = "drawScreen", at = @At("HEAD"), cancellable = true)
    public void onDrawScreen(int p_drawScreen_1_, int p_drawScreen_2_, float p_drawScreen_3_, CallbackInfo ci) {
        if (!ModuleManager.clientTheme.isEnabled() || !ModuleManager.clientTheme.mainMenu.isToggled())
            return;

        BackgroundUtils.renderBackground(this);

        FontManager.tenacity80.drawCenteredString("Raven XD", width / 2.0, height * 0.2, LOGO_COLOR);

        // Calculate the scale factor for the throbbing effect
        long currentTime = System.currentTimeMillis();
        float elapsedTime = (currentTime - startTime) % ANIMATION_DURATION;
        float scale = 1.0f + 0.1f * (float)Math.sin((elapsedTime / ANIMATION_DURATION) * 2 * Math.PI);

        // Apply the scale factor to the splash text
        GlStateManager.pushMatrix();
        GlStateManager.translate(width / 2.0 + 20, height * 0.275, 0); // Move text 20 pixels to the right
        GlStateManager.scale(scale, scale, 1.0f);
        FontManager.tenacity20.drawCenteredString(splashText, 0, 0, LOGO_COLOR);
        GlStateManager.popMatrix();

        List<String> branding = Lists.reverse(FMLCommonHandler.instance().getBrandings(true));

        for(int breadline = 0; breadline < branding.size(); ++breadline) {
            String brd = branding.get(breadline);
            if (!Strings.isNullOrEmpty(brd)) {
                this.drawString(this.fontRendererObj, brd, 2, this.height - (10 + breadline * (this.fontRendererObj.FONT_HEIGHT + 1)), 16777215);
            }
        }

        ForgeHooksClient.renderMainMenu((GuiMainMenu) (Object) this, this.fontRendererObj, this.width, this.height);
        String s1 = "Copyright Mojang AB. Do not distribute!";
        this.drawString(this.fontRendererObj, s1, this.width - this.fontRendererObj.getStringWidth(s1) - 2, this.height - 10, -1);
        String s2 = Raven.moduleCounter + " modules and " + Raven.settingCounter + " settings loaded!";
        this.drawString(this.fontRendererObj, s2, this.width - this.fontRendererObj.getStringWidth(s2) - 2, 2, -1);

        super.drawScreen(p_drawScreen_1_, p_drawScreen_2_, p_drawScreen_3_);
        if (this.func_183501_a()) {
            this.field_183503_M.drawScreen(p_drawScreen_1_, p_drawScreen_2_, p_drawScreen_3_);
        }

        ci.cancel();
    }

    @Inject(method = "initGui", at = @At("HEAD"), cancellable = true)
    public void onInitGui(CallbackInfo ci) {
        if (!ModuleManager.clientTheme.isEnabled() || !ModuleManager.clientTheme.mainMenu.isToggled())
            return;
        Random random = new Random();
        splashText = SPLASH_TEXTS.get(random.nextInt(SPLASH_TEXTS.size()));

        // Initialize the animation start time
        startTime = System.currentTimeMillis();

        int j = this.height / 4 + 48;
        this.buttonList.add(new GuiButton(1, this.width / 2 - 103, j, 200, 18, "SinglePlayer"));
        this.buttonList.add(new GuiButton(2, this.width / 2 - 103, j + 24, 200, 18, "MultiPlayer"));
        this.buttonList.add(new GuiButton(6, this.width / 2 - 103, j + 48, 200, 18, "Mods"));
        this.buttonList.add(new GuiButton(0, this.width / 2 - 103, j + 72 + 12, 98, 18, "Options"));
        this.buttonList.add(new GuiButton(4, this.width / 2 - 1, j + 72 + 12, 98, 18, "Quit"));

        this.mc.setConnectedToRealms(false);
        ci.cancel();
    }
}
