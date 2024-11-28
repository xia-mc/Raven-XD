package keystrokesmod.module.impl.other;

import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.utility.Utils;
import net.minecraft.client.network.NetworkPlayerInfo;

public class NameHider extends Module {
    public static DescriptionSetting a;
    public static String n = "raven";
    public static ButtonSetting hideAllNames;

    public NameHider() {
        super("Name Hider", Module.category.other);
        this.registerSetting(a = new DescriptionSetting(Utils.uf("command") + ": cname [name]"));
        this.registerSetting(hideAllNames = new ButtonSetting("Hide all names", false));
    }

    public static String getFakeName(String s) {
        if (mc.thePlayer != null) {
            if (hideAllNames.isToggled()) {
                s = s.replace(Utils.getServerName(), "You");
                NetworkPlayerInfo getPlayerInfo = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
                for (NetworkPlayerInfo networkPlayerInfo : mc.getNetHandler().getPlayerInfoMap()) {
                    if (networkPlayerInfo.equals(getPlayerInfo)) {
                        continue;
                    }
                    s = s.replace(networkPlayerInfo.getGameProfile().getName(), n);
                }
            } else {
                s = s.replace(Utils.getServerName(), n);
            }
        }
        return s;
    }
}
