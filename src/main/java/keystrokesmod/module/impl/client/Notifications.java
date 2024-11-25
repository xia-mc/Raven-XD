package keystrokesmod.module.impl.client;

import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.client.notification.DefaultNotification;
import keystrokesmod.module.impl.client.notification.INotification;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.ModeValue;
import keystrokesmod.utility.CoolDown;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.render.AnimationUtils;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;

public class Notifications extends Module {
    public static final List<Notification> notifs = new ArrayList<>();

    public static ModeValue mode;
    public static ButtonSetting chatNoti;
    public static ButtonSetting moduleToggled;

    public Notifications() {
        super("Notifications", category.client);
        this.registerSetting(mode = new ModeValue("Mode", this)
                .add(new DefaultNotification("Default", this))
        );
        this.registerSetting(chatNoti = new ButtonSetting("Show in chat", false));
        this.registerSetting(moduleToggled = new ButtonSetting("Module toggled", true));
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

    @Override
    public void onEnable() {
        mode.enable();
        notifs.clear();
    }

    @Override
    public void onDisable() {
        mode.disable();
    }

    @SubscribeEvent
    public void onTick(TickEvent.RenderTickEvent event) {
        ScaledResolution sr = new ScaledResolution(mc);
        for (int index = 0; index < notifs.size(); index++) {
            Notification noti = notifs.get(index);
            noti.animationY.setAnimation(sr.getScaledHeight() - ((index + 1) * 30), 16);
            ((INotification) mode.getSelected()).render(noti);
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