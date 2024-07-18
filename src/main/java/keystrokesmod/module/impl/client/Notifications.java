package keystrokesmod.module.impl.client;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.utility.CoolDown;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.font.Font;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.font.impl.MinecraftFontRenderer;
import keystrokesmod.utility.render.AnimationUtils;
import keystrokesmod.utility.render.ColorUtils;
import keystrokesmod.utility.render.RRectUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Notifications extends Module {
    public static final List<NotificationTypes> notifs = new ArrayList<>();
    private static final List<String> messages = new ArrayList<>();
    private static final List<CoolDown> durations = new ArrayList<>();
    private static final List<AnimationUtils> animationsX = new ArrayList<>();
    private static final List<AnimationUtils> animationsY = new ArrayList<>();
    public static ButtonSetting chatNoti;
    private final Font fontRegular = FontManager.getRegular(16);
    private final Font fontIcon = FontManager.getIcons(20);

    public Notifications() {
        super("Notifications", category.client);
        this.registerSetting(chatNoti = new ButtonSetting("Show in chat", false));
    }

    public static void sendNotification(NotificationTypes notificationType, String message, long duration) {
        if (!chatNoti.isToggled()) {
            ScaledResolution sr = new ScaledResolution(mc);
            notifs.add(notificationType);
            messages.add(message);
            durations.add(new CoolDown(duration));
            durations.get(notifs.size() - 1).start();
            animationsX.add(new AnimationUtils(sr.getScaledWidth()));
            animationsY.add(new AnimationUtils(sr.getScaledHeight() - (notifs.size() * 30)));
            animationsX.get(notifs.size() - 1).setAnimation(sr.getScaledWidth(), 16);
        } else {
            Utils.sendMessage("&7[&1LI&7-" + ((notificationType == NotificationTypes.INFO) ? "&1" : notificationType == NotificationTypes.WARN ? "&e" : "&4") + notificationType.toString() + "&7]&r " + message);
        }
    }

    @SubscribeEvent
    public void onTick(RenderGameOverlayEvent event) {
        ScaledResolution sr = new ScaledResolution(mc);
        for (int index = 0; index < notifs.size(); index++) {
            animationsY.get(index).setAnimation(sr.getScaledHeight() - ((index + 1) * 30), 16);
            RRectUtils.drawRound(animationsX.get(index).getValue(), animationsY.get(index).getValue(), 120, 25, 3, new Color(0, 0, 0, 92));
            fontIcon.drawString(notifs.get(index) == NotificationTypes.INFO ? "G" : "R", animationsX.get(index).getValue() + 12.5, animationsY.get(index).getValue() + 15.5, MinecraftFontRenderer.CenterMode.XY, false, ColorUtils.getFontColor(2).getRGB());
            fontRegular.wrapText(messages.get(index), animationsX.get(index).getValue() + 25, animationsY.get(index).getValue() + 12.5, MinecraftFontRenderer.CenterMode.Y, false, ColorUtils.getFontColor(2).getRGB(), 95);
            if (durations.get(index).hasFinished()) {
                notifs.remove(index);
                messages.remove(index);
                durations.remove(index);
                animationsX.remove(index);
                animationsY.remove(index);
                index--;
            } else if (durations.get(index).getTimeLeft() < 500) {
                animationsX.get(index).setAnimation(sr.getScaledWidth(), 16);
            } else {
                animationsX.get(index).setAnimation(sr.getScaledWidth() - 125, 16);
            }
        }
    }

    public enum NotificationTypes {
        INFO,
        WARN,
        ERROR
    }
}