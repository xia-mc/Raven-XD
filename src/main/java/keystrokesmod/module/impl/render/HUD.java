package keystrokesmod.module.impl.render;

import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.player.ChestStealer;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.font.IFont;
import keystrokesmod.utility.render.RenderUtils;
import keystrokesmod.utility.Theme;
import keystrokesmod.utility.Utils;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class HUD extends Module {
    public static ModeSetting theme;
    public static ModeSetting font;
    public static ButtonSetting dropShadow;
    private final ButtonSetting background;
    private final ButtonSetting sidebar;
    public static ButtonSetting alphabeticalSort;
    private static ButtonSetting alignRight;
    public static ButtonSetting lowercase;
    public static ButtonSetting showInfo;
    private static final ButtonSetting combat = new ButtonSetting("Combat", true);
    private static final ButtonSetting movement = new ButtonSetting("Movement", true);
    private static final ButtonSetting player = new ButtonSetting("Player", true);
    private static final ButtonSetting world = new ButtonSetting("World", true);
    private static final ButtonSetting render = new ButtonSetting("Render", true);
    private static final ButtonSetting minigames = new ButtonSetting("Minigames", true);
    private static final ButtonSetting fun = new ButtonSetting("Fun", true);
    private static final ButtonSetting other = new ButtonSetting("Other", true);
    private static final ButtonSetting client = new ButtonSetting("Client", true);
    private static final ButtonSetting scripts = new ButtonSetting("Scripts", true);
    private static final ButtonSetting exploit = new ButtonSetting("Exploit", true);
    private static final ButtonSetting experimental = new ButtonSetting("Experimental", true);
    public static int hudX = 5;
    public static int hudY = 70;
    private boolean isAlphabeticalSort;
    private boolean canShowInfo;

    public HUD() {
        super("HUD", Module.category.render);
        this.registerSetting(new DescriptionSetting("Right click bind to hide modules."));
        this.registerSetting(theme = new ModeSetting("Theme", Theme.themes, 0));
        this.registerSetting(font = new ModeSetting("Font", new String[]{"Minecraft", "Product Sans", "Regular", "Tenacity"}, 0));
        this.registerSetting(new ButtonSetting("Edit position", () -> {
            final EditScreen screen = new EditScreen();
            FMLCommonHandler.instance().bus().register(screen);
            mc.displayGuiScreen(screen);
        }));
        this.registerSetting(alignRight = new ButtonSetting("Align right", false));
        this.registerSetting(alphabeticalSort = new ButtonSetting("Alphabetical sort", false));
        this.registerSetting(dropShadow = new ButtonSetting("Drop shadow", true));
        this.registerSetting(background = new ButtonSetting("Background", false));
        this.registerSetting(sidebar = new ButtonSetting("Sidebar", false));
        this.registerSetting(lowercase = new ButtonSetting("Lowercase", false));
        this.registerSetting(showInfo = new ButtonSetting("Show module info", true));

        this.registerSetting(new DescriptionSetting("Categories"));
        this.registerSetting(combat, movement, player, world, render, minigames, fun, other, client, scripts, exploit, experimental);
    }

    public void onEnable() {
        ModuleManager.sort();
    }

    public void guiButtonToggled(ButtonSetting b) {
        if (b == alphabeticalSort || b == showInfo) {
            ModuleManager.sort();
        }
    }

    @SubscribeEvent
    public void onRenderTick(@NotNull RenderTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END || !Utils.nullCheck()) {
            return;
        }
        if (isAlphabeticalSort != alphabeticalSort.isToggled()) {
            isAlphabeticalSort = alphabeticalSort.isToggled();
            ModuleManager.sort();
        }
        if (canShowInfo != showInfo.isToggled()) {
            canShowInfo = showInfo.isToggled();
            ModuleManager.sort();
        }
        if (mc.currentScreen != null && !(mc.currentScreen instanceof GuiChest && ChestStealer.noChestRender()) && !(mc.currentScreen instanceof GuiChat) || mc.gameSettings.showDebugInfo) {
            return;
        }
        int n = hudY;
        double n2 = 0.0;
        try {
            List<String> texts = getDrawTexts();

            for (String text : texts) {
                int e = Theme.getGradient((int) theme.getInput(), n2);
                if (theme.getInput() == 0) {
                    n2 -= 120;
                } else {
                    n2 -= 12;
                }
                double n3 = hudX;
                double width = getFontRenderer().width(text);
                if (alignRight.isToggled()) {
                    n3 -= width;
                }
                if (background.isToggled()) {
                    RenderUtils.drawRect(n3 - 1, n - 1, n3 + width, n + Math.round(getFontRenderer().height() + 1), new Color(0, 0, 0, 100).getRGB());
                }
                if (sidebar.isToggled()) {
                    RenderUtils.drawRect(alignRight.isToggled() ? n3 + width : n3 - 2, n - 1, alignRight.isToggled() ? n3 + width + 1 : n3 - 1, n + Math.round(getFontRenderer().height() + 1), new Color(255, 255, 255, 200).getRGB());
                }
                getFontRenderer().drawString(text, n3, n, e, dropShadow.isToggled());
                n += Math.round(getFontRenderer().height() + 2);
            }
        }
        catch (Exception exception) {
            Utils.sendMessage("&cAn error occurred rendering HUD. check your logs");
            Utils.sendDebugMessage(Arrays.toString(exception.getStackTrace()));
            Utils.log.error(exception);
        }
    }

    @NotNull
    private List<String> getDrawTexts() {
        List<Module> modules = ModuleManager.organizedModules;
        List<String> texts = new ArrayList<>(modules.size());

        for (Module module : modules) {
            if (isIgnored(module)) continue;

            String text = module.getPrettyName();
            if (showInfo.isToggled() && !module.getPrettyInfo().isEmpty()) {
                text += " §7" + module.getPrettyInfo();
            }
            if (lowercase.isToggled()) {
                text = text.toLowerCase();
            }
            texts.add(text);
        }
        return texts;
    }

    public static double getLongestModule(IFont fr) {
        double length = 0;

        for (Module module : ModuleManager.organizedModules) {
            if (module.isEnabled()) {
                String moduleName = module.getPrettyName();
                if (showInfo.isToggled() && !module.getInfo().isEmpty()) {
                    moduleName += " §7" + module.getInfo();
                }
                if (lowercase.isToggled()) {
                    moduleName = moduleName.toLowerCase();
                }
                if (fr.width(moduleName) > length) {
                    length = fr.width(moduleName);
                }
            }
        }
        return length;
    }

    static class EditScreen extends GuiScreen {
        final String example = "This is an-Example-HUD";
        GuiButtonExt resetPosition;
        boolean hoverHUD = false;
        boolean hoverTargetHUD = false;
        boolean hoverWatermark = false;
        int miX = 0;
        int miY = 0;
        double maX = 0;
        double maY = 0;
        int curHudX = 5;
        int curHudY = 70;
        int laX = 0;
        int laY = 0;
        int lmX = 0;
        int lmY = 0;
        double clickMinX = 0;

        public void initGui() {
            super.initGui();
            this.buttonList.add(this.resetPosition = new GuiButtonExt(1, this.width - 90, 5, 85, 20, "Reset position"));
            this.curHudX = HUD.hudX;
            this.curHudY = HUD.hudY;
        }

        @Override
        public void onGuiClosed() {
            FMLCommonHandler.instance().bus().unregister(this);
        }

        public void drawScreen(int mX, int mY, float pt) {
            drawRect(0, 0, this.width, this.height, -1308622848);
            int miX = this.curHudX;
            int miY = this.curHudY;
            int maX = miX + 50;
            int maY = miY + 32;
            double[] clickPos = this.d(getFontRenderer(), this.example);
            this.miX = miX;
            this.miY = miY;
            if (clickPos == null) {
                this.maX = maX;
                this.maY = maY;
                this.clickMinX = miX;
            }
            else {
                this.maX = clickPos[0];
                this.maY = clickPos[1];
                this.clickMinX = clickPos[2];
            }
            HUD.hudX = miX;
            HUD.hudY = miY;
            ScaledResolution res = new ScaledResolution(this.mc);
            int x = res.getScaledWidth() / 2 - 84;
            int y = res.getScaledHeight() / 2 - 20;
            RenderUtils.dct("Edit the HUD position by dragging.", '-', x, y, 2L, 0L, true, getFontRenderer());

            try {
                this.handleInput();
            } catch (IOException ignored) {
            }

            super.drawScreen(mX, mY, pt);
        }

        @SubscribeEvent
        public void onRenderTick(RenderTickEvent event) {
            TargetHUD.renderExample();
            ModuleManager.watermark.render();
        }

        private double @Nullable [] d(IFont fr, String t) {
            if (empty()) {
                double x = this.miX;
                double y = this.miY;
                String[] var5 = t.split("-");

                for (String s : var5) {
                    if (HUD.alignRight.isToggled()) {
                        x += getFontRenderer().width(var5[0]) - getFontRenderer().width(s);
                    }
                    fr.drawString(s, (float) x, (float) y, Color.white.getRGB(), HUD.dropShadow.isToggled());
                    y += Math.round(fr.height() + 2);
                }
            }
            else {
                double longestModule = getLongestModule(getFontRenderer());
                double n = this.miY;
                double n2 = 0.0;
                for (Module module : ModuleManager.organizedModules) {
                    if (isIgnored(module)) continue;

                    String moduleName = module.getPrettyName();
                    if (showInfo.isToggled() && !module.getInfo().isEmpty()) {
                        moduleName += " §7" + module.getInfo();
                    }
                    if (lowercase.isToggled()) {
                        moduleName = moduleName.toLowerCase();
                    }
                    int e = Theme.getGradient((int) theme.getInput(), n2);
                    if (theme.getInput() == 0) {
                        n2 -= 120;
                    }
                    else {
                        n2 -= 12;
                    }
                    double n3 = this.miX;
                    if (alignRight.isToggled()) {
                        n3 -= getFontRenderer().width(moduleName);
                    }
                    getFontRenderer().drawString(moduleName, n3, (float) n, e, dropShadow.isToggled());
                    n += Math.round(getFontRenderer().height() + 2);
                }
                return new double[]{this.miX + longestModule, n, this.miX - longestModule};
            }
            return null;
        }

        protected void mouseClickMove(int mX, int mY, int b, long t) {
            super.mouseClickMove(mX, mY, b, t);
            if (b == 0) {
                if (this.hoverHUD) {
                    this.curHudX = this.laX + (mX - this.lmX);
                    this.curHudY = this.laY + (mY - this.lmY);
                } else if (this.hoverTargetHUD) {
                    TargetHUD.posX = this.laX + (mX - this.lmX);
                    TargetHUD.posY = this.laY + (mY - this.lmY);
                } else if (this.hoverWatermark) {
                    Watermark.posX = this.laX + (mX - this.lmX);
                    Watermark.posY = this.laY + (mY - this.lmY);
                } else if (mX > this.clickMinX && mX < this.maX && mY > this.miY && mY < this.maY) {
                    this.hoverHUD = true;
                    this.lmX = mX;
                    this.lmY = mY;
                    this.laX = this.curHudX;
                    this.laY = this.curHudY;
                } else if (mX > TargetHUD.current$minX && mX < TargetHUD.current$maxX && mY > TargetHUD.current$minY && mY < TargetHUD.current$maxY) {
                    this.hoverTargetHUD = true;
                    this.lmX = mX;
                    this.lmY = mY;
                    this.laX = TargetHUD.posX;
                    this.laY = TargetHUD.posY;
                } else if (mX > Watermark.current$minX && mX < Watermark.current$maxX && mY > Watermark.current$minY && mY < Watermark.current$maxY) {
                    this.hoverWatermark = true;
                    this.lmX = mX;
                    this.lmY = mY;
                    this.laX = Watermark.posX;
                    this.laY = Watermark.posY;
                }

            }
        }

        protected void mouseReleased(int mX, int mY, int s) {
            super.mouseReleased(mX, mY, s);
            if (s == 0) {
                this.hoverHUD = false;
                this.hoverTargetHUD = false;
                this.hoverWatermark = false;
            }

        }

        public void actionPerformed(GuiButton b) {
            if (b == this.resetPosition) {
                this.curHudX = HUD.hudX = 5;
                this.curHudY = HUD.hudY = 70;
                TargetHUD.posX = 70;
                TargetHUD.posY = 30;
                Watermark.posX = 5;
                Watermark.posY = 5;
            }

        }

        public boolean doesGuiPauseGame() {
            return false;
        }

        private boolean empty() {
            for (Module module : ModuleManager.organizedModules) {
                if (module.isEnabled() && !module.getName().equals("HUD")) {
                    if (module.isHidden()) {
                        continue;
                    }
                    if (module == ModuleManager.commandLine) {
                        continue;
                    }
                    return false;
                }
            }
            return true;
        }
    }

    private static boolean isIgnored(@NotNull Module module) {
        if (!module.isEnabled() || module.getName().equals("HUD"))
            return true;
        if (module instanceof SubMode)
            return true;

        if (module.moduleCategory() == category.combat && !combat.isToggled()) return true;
        if (module.moduleCategory() == category.movement && !movement.isToggled()) return true;
        if (module.moduleCategory() == category.player && !player.isToggled()) return true;
        if (module.moduleCategory() == category.world && !world.isToggled()) return true;
        if (module.moduleCategory() == category.render && !render.isToggled()) return true;
        if (module.moduleCategory() == category.minigames && !minigames.isToggled()) return true;
        if (module.moduleCategory() == category.fun && !fun.isToggled()) return true;
        if (module.moduleCategory() == category.other && !other.isToggled()) return true;
        if (module.moduleCategory() == category.client && !client.isToggled()) return true;
        if (module.moduleCategory() == category.scripts && !scripts.isToggled()) return true;
        if (module.moduleCategory() == category.exploit && !exploit.isToggled()) return true;
        if (module.moduleCategory() == category.experimental && !experimental.isToggled()) return true;

        if (module.isHidden()) {
            return true;
        }
        return module == ModuleManager.commandLine;
    }

    public static IFont getFontRenderer() {
        switch ((int) font.getInput()) {
            default:
            case 0:
                return FontManager.getMinecraft();
            case 1:
                return FontManager.productSans20;
            case 2:
                return FontManager.regular22;
            case 3:
                return FontManager.tenacity20;
        }
    }
}
