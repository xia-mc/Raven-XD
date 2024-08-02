package keystrokesmod.module.impl.client;

import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.utility.CoolDown;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.font.impl.FontRenderer;
import keystrokesmod.utility.render.AnimationUtils;
import keystrokesmod.utility.render.ColorUtils;
import keystrokesmod.utility.render.RRectUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Notifications extends Module {
    public static final List<Notification> notifs = new ArrayList<>();
    public static ButtonSetting chatNoti;
    public static ButtonSetting moduleToggled;
    public Notifications() {
        super("Notifications", category.client);
        this.registerSetting(chatNoti = new ButtonSetting("Show in chat", false));
        this.registerSetting(moduleToggled = new ButtonSetting("Module toggled", true));
    }

    @Override
    public void onEnable() {
        notifs.clear();
    }

    public static void sendNotification(NotificationTypes notificationType, String message) {
        sendNotification(notificationType, message, 3000);
    }

    public static void sendNotification(NotificationTypes notificationType, String message, long duration) {
        if (!ModuleManager.notifications.isEnabled()) return;

        if (!chatNoti.isToggled()) {
            ScaledResolution sr = new ScaledResolution(mc);
            CoolDown coolDown = new CoolDown(duration);
            coolDown.start();
            AnimationUtils animationX = new AnimationUtils(sr.getScaledWidth());
            animationX.setAnimation(sr.getScaledWidth(), 16);
            notifs.add(new Notification(notificationType,
                    message, coolDown,
                    animationX,
                    new AnimationUtils(sr.getScaledHeight() - (notifs.size() * 30))
            ));
        } else {
            Utils.sendMessage("&7[&1LI&7-" + ((notificationType == NotificationTypes.INFO) ? "&1" : notificationType == NotificationTypes.WARN ? "&e" : "&4") + notificationType.toString() + "&7]&r " + message);
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.RenderTickEvent event) {
        ScaledResolution sr = new ScaledResolution(mc);
        for (int index = 0; index < notifs.size(); index++) {
            Notification noti = notifs.get(index);
            noti.animationY.setAnimation(sr.getScaledHeight() - ((index + 1) * 30), 16);
            RRectUtils.drawRound(noti.animationX.getValue(), noti.animationY.getValue(), 120, 25, 3, new Color(0, 0, 0, 128));
            FontManager.icon20.drawString(noti.type == NotificationTypes.INFO ? "G" : "R", noti.animationX.getValue() + 12.5, noti.animationY.getValue() + 15.5, FontRenderer.CenterMode.XY, false, ColorUtils.getFontColor(2).getRGB());
            String[] messageParts = noti.message.split("§");
            double x = noti.animationX.getValue() + 25;
            double y = noti.animationY.getValue() + 15;
            if (messageParts.length == 1) {
                FontManager.regular16.drawString(noti.message, x, y, FontRenderer.CenterMode.Y, false, Color.WHITE.getRGB());
            } else {
                for (String part : messageParts) {
                    if (part.isEmpty()) continue;
                    char colorCode = part.charAt(0);
                    String text = part.substring(1);
                    Color color = ColorUtils.getColorFromCode("§" + colorCode);
                    FontManager.regular16.drawString(text, x, y, FontRenderer.CenterMode.Y, false, color.getRGB());
                    x += FontManager.regular16.getStringWidth(text);
                }
            }
            //fontRegular.wrapText(noti.message, noti.animationX.getValue() + 25, noti.animationY.getValue() + 12.5, MinecraftFontRenderer.CenterMode.Y, false, ColorUtils.getFontColor(2).getRGB(), 95);
            if (noti.duration.hasFinished()) {
                notifs.remove(index);
                index--;
            } else if (noti.duration.getTimeLeft() < 500) {
                noti.animationX.setAnimation(sr.getScaledWidth(), 16);
            } else {
                noti.animationX.setAnimation(sr.getScaledWidth() - 125, 16);
            }
        }
    }

    public enum NotificationTypes {
        INFO,
        WARN,
        ERROR
    }

    public static class Notification {
        public final NotificationTypes type;
        public final String message;
        public final CoolDown duration;
        public final AnimationUtils animationX;
        public final AnimationUtils animationY;

        public Notification(NotificationTypes type, String message, CoolDown duration, AnimationUtils animationX, AnimationUtils animationY) {
            this.type = type;
            this.message = message;
            this.duration = duration;
            this.animationX = animationX;
            this.animationY = animationY;
        }
    }
}