package keystrokesmod.module.impl.client.notification;

import keystrokesmod.module.impl.client.Notifications;
import keystrokesmod.module.setting.impl.SubMode;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.font.impl.FontRenderer;
import keystrokesmod.utility.render.ColorUtils;
import keystrokesmod.utility.render.RRectUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class DefaultNotification extends SubMode<Notifications> implements INotification {

    public DefaultNotification(String name, @NotNull Notifications parent) {
        super(name, parent);
    }

    @Override
    public void render(Notifications.@NotNull Notification notification) {
        RRectUtils.drawRound(notification.animationX.getValue(), notification.animationY.getValue(), 120, 25, 3, new Color(0, 0, 0, 128));
        FontManager.icon20.drawString(notification.type == Notifications.NotificationTypes.INFO ? "G" : "R", notification.animationX.getValue() + 12.5, notification.animationY.getValue() + 15.5, FontRenderer.CenterMode.XY, false, ColorUtils.getFontColor(2).getRGB());
        String[] messageParts = notification.message.split("§");
        double x = notification.animationX.getValue() + 25;
        double y = notification.animationY.getValue() + 15;
        if (messageParts.length == 1) {
            FontManager.regular16.drawString(notification.message, x, y, FontRenderer.CenterMode.Y, false, Color.WHITE.getRGB());
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
    }
}
