package keystrokesmod.module.impl.render;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.combat.KillAura;
import keystrokesmod.module.impl.render.arraylist.ArrayListModule;
import keystrokesmod.module.setting.impl.*;
import keystrokesmod.utility.Theme;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.font.IFont;
import keystrokesmod.utility.interact.moveable.Moveable;
import keystrokesmod.utility.interact.moveable.MoveableManager;
import keystrokesmod.utility.render.RenderUtils;
import keystrokesmod.utility.render.blur.GaussianBlur;
import lombok.Getter;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class ArrayList extends Module implements Moveable {
    private static final int BACKGROUND_COLOR = new Color(20, 20, 20, 150).getRGB();

    private final ModeSetting theme;
    private final SliderSetting size;
    private final ButtonSetting info;
    private final ModeSetting textCase;
    private final ButtonSetting right;
    private final ButtonSetting background;
    private final ButtonSetting blur;
    private final ButtonSetting combat;
    private final ButtonSetting movement;
    private final ButtonSetting player;
    private final ButtonSetting world;
    private final ButtonSetting render;
    private final ButtonSetting minigames;
    private final ButtonSetting fun;
    private final ButtonSetting other;
    private final ButtonSetting client;
    private final ButtonSetting scripts;
    private final ButtonSetting exploit;
    private final ButtonSetting experimental;

    private final Map<@NotNull Module, @NotNull ArrayListModule> mapping = new Object2ObjectOpenHashMap<>();
    @Getter
    private int minX = 10, minY = 10, maxX = 10, maxY = 10;

    public ArrayList() {
        super("ArrayList", category.render);
        this.registerSetting(theme = new ModeSetting("Theme", Theme.themes, 0));
        this.registerSetting(size = new SliderSetting("Size", 1, 0.5, 2, 0.1));
        this.registerSetting(info = new ButtonSetting("Info", false));
        this.registerSetting(textCase = new ModeSetting("Text case", new String[]{"None", "lower", "UPPER"}, 0));
        this.registerSetting(right = new ButtonSetting("Right", false));
        this.registerSetting(background = new ButtonSetting("Background", false));
        this.registerSetting(blur = new ButtonSetting("Blur", false));
        this.registerSetting(new DescriptionSetting("Categories"));
        this.registerSetting(combat = new ButtonSetting("Combat", true));
        this.registerSetting(movement = new ButtonSetting("Movement", true));
        this.registerSetting(player = new ButtonSetting("Player", true));
        this.registerSetting(world = new ButtonSetting("World", true));
        this.registerSetting(render = new ButtonSetting("Render", true));
        this.registerSetting(minigames = new ButtonSetting("Minigames", true));
        this.registerSetting(fun = new ButtonSetting("Fun", true));
        this.registerSetting(other = new ButtonSetting("Other", true));
        this.registerSetting(client = new ButtonSetting("Client", true));
        this.registerSetting(scripts = new ButtonSetting("Scripts", true));
        this.registerSetting(exploit = new ButtonSetting("Exploit", true));
        this.registerSetting(experimental = new ButtonSetting("Experimental", true));
        MoveableManager.register(this);
    }

    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (mc.currentScreen == null)
            render();
    }

    @Override
    public void render() {
        final IFont font = FontManager.getFont(FontManager.Fonts.TENACITY, (int) (20 * size.getInput()));
        final double height = font.height();

        updateArrayList(font);

        final ScaledResolution sr = new ScaledResolution(mc);
        moveX(Utils.limit(minX, 0, sr.getScaledWidth()) - minX);
        moveY(Utils.limit(minY, 0, sr.getScaledHeight()) - minY);

        int nextMinX = minX;
        int nextMaxX = minX;
        int nextMaxY = minY;

        final double shadowExtra = 2 * size.getInput();
        final double lineInterval = shadowExtra * 3;
        final int enableX = right.isToggled() ? maxX : minX;
        for (ArrayListModule module : mapping.values()) {
            if (!module.shouldRender()) continue;
            String text = getText(module);
            double width = font.width(text);

            int posX = module.getPosX(enableX, right.isToggled() ? sr.getScaledWidth() + width + shadowExtra * 2 : -width - shadowExtra * 2);
            if (module.getModule() instanceof KillAura)
                Utils.sendMessage(String.valueOf(posX));
            int posY = module.getPosY(minY, (int) (lineInterval + height));

            if (background.isToggled()) {
                if (right.isToggled()) {
                    RenderUtils.drawBloomShadow(
                            (float) (posX - width - shadowExtra * 2), (float) (posY - shadowExtra),
                            (float) (width + shadowExtra * 2), (float) (height + shadowExtra * 2),
                            (int) shadowExtra, 8, BACKGROUND_COLOR, false
                    );
                } else {
                    RenderUtils.drawBloomShadow(
                            (float) (posX - shadowExtra), (float) (posY - shadowExtra),
                            (float) (width + shadowExtra * 2), (float) (height + shadowExtra * 2),
                            (int) shadowExtra, 8, BACKGROUND_COLOR, false
                    );
                }
            }

            if (blur.isToggled()) {
                GaussianBlur.startBlur();
                if (right.isToggled()) {
                    RenderUtils.drawBloomShadow(
                            (float) (posX - width - shadowExtra * 2), (float) (posY - shadowExtra),
                            (float) (width + shadowExtra * 2), (float) (height + shadowExtra * 2),
                            (int) shadowExtra, 8, -1, false
                    );
                } else {
                    RenderUtils.drawBloomShadow(
                            (float) (posX - shadowExtra), (float) (posY - shadowExtra),
                            (float) (width + shadowExtra * 2), (float) (height + shadowExtra * 2),
                            (int) shadowExtra, 8, -1, false
                    );
                }
                GaussianBlur.endBlur(4, 1);
            }

            if (right.isToggled()) {
                font.drawRightString(text, posX, posY, Theme.getGradient((int) theme.getInput(), posY));
                nextMinX = (int) Math.min(nextMinX, posX - width);
                nextMaxX = posX;
            } else {
                font.drawString(text, posX, posY, Theme.getGradient((int) theme.getInput(), posY));
                nextMinX = posX;
                nextMaxX = (int) Math.max(nextMaxX, posX + width);
            }
            nextMaxY = (int) (posY + height);
        }

        minX = nextMinX;
        maxX = nextMaxX;
        maxY = nextMaxY;
    }

    private @NotNull String getText(@NotNull ArrayListModule module) {
        String name = module.getName();
        Optional<String> info = this.info.isToggled() ? module.getInfo() : Optional.empty();
        StringBuilder text = new StringBuilder(name);
        info.ifPresent(string -> text.append(" §7").append(string));

        String result = text.toString();
        switch ((int) textCase.getInput()) {
            default:
            case 0:
                return result;
            case 1:
                return result.toLowerCase();
            case 2:
                return result.toUpperCase();
        }
    }

    private void updateArrayList(IFont font) {
        final int[] renderingIndex = {0};
        ModuleManager.organizedModules.parallelStream()
                .filter(this::canRender)
                .map(module -> {
                    if (!mapping.containsKey(module)) {
                        return mapping.put(module, new ArrayListModule(module));
                    }
                    return mapping.get(module);
                })
                .filter(Objects::nonNull)
                .sorted((c1, c2) -> Double.compare(font.width(getText(c2)), font.width(getText(c1))))
                .forEachOrdered(module -> {
                    module.onUpdate(renderingIndex[0]);
                    if (module.getModule().isEnabled())
                        renderingIndex[0] += 1;
                });
    }

    private boolean canRender(@NotNull Module module) {
        if (module instanceof SubMode)
            return false;
        if (module.isHidden())
            return false;

        if (module.moduleCategory() == category.combat && !combat.isToggled()) return false;
        if (module.moduleCategory() == category.movement && !movement.isToggled()) return false;
        if (module.moduleCategory() == category.player && !player.isToggled()) return false;
        if (module.moduleCategory() == category.world && !world.isToggled()) return false;
        if (module.moduleCategory() == category.render && !render.isToggled()) return false;
        if (module.moduleCategory() == category.minigames && !minigames.isToggled()) return false;
        if (module.moduleCategory() == category.fun && !fun.isToggled()) return false;
        if (module.moduleCategory() == category.other && !other.isToggled()) return false;
        if (module.moduleCategory() == category.client && !client.isToggled()) return false;
        if (module.moduleCategory() == category.scripts && !scripts.isToggled()) return false;
        if (module.moduleCategory() == category.exploit && !exploit.isToggled()) return false;
        if (module.moduleCategory() == category.experimental && !experimental.isToggled()) return false;

        return true;
    }

    @Override
    public boolean isDisabled() {
        return !this.isEnabled();
    }

    @Override
    public void moveX(int amount) {
        this.minX += amount;
        this.maxX += amount;
    }

    @Override
    public void moveY(int amount) {
        this.minY += amount;
        this.maxY += amount;
    }
}
