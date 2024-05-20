package keystrokesmod.utility;

import keystrokesmod.Raven;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.client.Settings;
import keystrokesmod.module.impl.combat.Velocity;
import keystrokesmod.module.impl.minigames.DuelsStats;
import keystrokesmod.module.impl.movement.BHop;
import keystrokesmod.module.impl.movement.Fly;
import keystrokesmod.module.impl.movement.Speed;
import keystrokesmod.module.impl.other.FakeChat;
import keystrokesmod.module.impl.other.NameHider;
import keystrokesmod.utility.profile.Profile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Commands {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static boolean f = true;
    private static final List<Integer> cs = Arrays.asList((new Color(170, 107, 148, 50)).getRGB(), (new Color(122, 158, 134, 50)).getRGB(), (new Color(16, 16, 16, 50)).getRGB(), (new Color(64, 114, 148, 50)).getRGB());
    private static int ccs = 0;
    private static int lccs = -1;
    public static List<String> rs = new ArrayList();
    private static final String invSyn = "&cInvalid syntax.";
    private static final String invCom = "&cInvalid command.";

    public static void rCMD(String c) {
        if (!c.isEmpty()) {
            String cm = c.toLowerCase();
            boolean hasArgs = c.contains(" ");
            String[] args = hasArgs ? c.split(" ") : null;
            String n;
            if (cm.startsWith("setkey".toLowerCase())) {
                if (!hasArgs) {
                    print(invSyn, 1);
                    return;
                }

                if (args.length != 2) {
                    print(invSyn, 1);
                    return;
                }

                print("Setting...", 1);
                n = args[1];
                Raven.getExecutor().execute(() -> {
                    if (URLUtils.isHypixelKeyValid(n)) {
                        URLUtils.k = n;
                        print("&a" + "success!", 0);
                    } else {
                        print("&c" + "Invalid key.", 0);
                    }

                });
            } else if (cm.startsWith("nick")) {
                if (!hasArgs) {
                    print(invSyn, 1);
                    return;
                }

                if (args.length != 2) {
                    print(invSyn, 1);
                    return;
                }

                if (args[1].equals("reset")) {
                    print("&aNick reset.", 1);
                    return;
                }

                DuelsStats.nick = args[1];
                print("&aNick has been set to:", 1);
                print("\"" + DuelsStats.nick + "\"", 0);
            } else if (cm.startsWith("cname")) {
                if (!hasArgs) {
                    print(invSyn, 1);
                    return;
                }

                if (args.length != 2) {
                    print(invSyn, 1);
                    return;
                }

                NameHider.n = args[1];
                print("&a" + Utils.uf("name") + "Nick has been set to:".substring(4), 1);
                print("\"" + NameHider.n + "\"", 0);
            } else if (cm.startsWith(FakeChat.command)) {
                if (!hasArgs) {
                    print(invSyn, 1);
                    return;
                }

                n = c.replaceFirst(FakeChat.command, "").substring(1);
                if (n.isEmpty() || n.equals("\\n")) {
                    print(FakeChat.c4, 1);
                    return;
                }

                FakeChat.msg = n;
                print("&aMessage set!", 1);
            } else if (cm.startsWith("Duels".toLowerCase())) {
                if (!hasArgs) {
                    print(invSyn, 1);
                    return;
                }

                if (args.length != 2) {
                    print(invSyn, 1);
                    return;
                }

                if (URLUtils.k.isEmpty()) {
                    print("&cAPI Key is empty!", 1);
                    print("Use \"setkey [api_key]\".", 0);
                    return;
                }

                n = args[1];
                print("Retrieving data...", 1);
                Raven.getExecutor().execute(() -> {
                    int[] s = ProfileUtils.getHypixelStats(n, ProfileUtils.DM.OVERALL);
                    if (s != null) {
                        if (s[0] == -1) {
                            print("&c" + (n.length() > 16 ? n.substring(0, 16) + "..." : n) + " does not exist!", 0);
                        } else {
                            double wlr = s[1] != 0 ? Utils.rnd((double) s[0] / (double) s[1], 2) : (double) s[0];
                            print("&e" + n + " stats:", 1);
                            print("Wins: " + s[0], 0);
                            print("Losses: " + s[1], 0);
                            print("WLR: " + wlr, 0);
                            print("Winstreak: " + s[2], 0);
                            print("Threat: " + DuelsStats.gtl(s[0], s[1], wlr, s[2]).substring(2), 0);
                        }
                    } else {
                        print("&cThere was an error.", 0);
                    }

                });
            } else if (cm.startsWith("setspeed")) {
                if (!hasArgs) {
                    print(invSyn, 1);
                    return;
                }

                if (args.length != 3) {
                    print(invSyn, 1);
                    return;
                }

                double value;

                try {
                    value = Double.parseDouble(args[2]);
                } catch (Exception e) {
                    print("&cInvalid value. [0 - 100]", 1);
                    return;
                }

                if (value > 100 || value < 0) {
                    print("&cInvalid value. [0 - 100]", 1);
                    return;
                }

                switch (args[1]) {
                    case "fly":
                        Fly.horizontalSpeed.setValueRaw(value);
                        break;
                    case "bhop":
                        BHop.speed.setValueRaw(value);
                        break;
                    case "speed":
                        Speed.speed.setValueRaw(value);
                        break;
                    default:
                        print(invSyn, 1);
                        return;
                }
                print("&aSet speed to ", 1);
                print(args[2], 0);
            } else if (cm.startsWith("setvelocity")) {
                if (!hasArgs) {
                    print(invSyn, 1);
                    return;
                }

                if (args.length != 3) {
                    print(invSyn, 1);
                    return;
                }

                double value;

                try {
                    value = Double.parseDouble(args[2]);
                } catch (Exception e) {
                    print("&cInvalid value. [-100 - 300]", 1);
                    return;
                }

                if (value > 300 || value < -100) {
                    print("&cInvalid value. [-100 - 300]", 1);
                    return;
                }

                switch (args[1]) {
                    case "horizontal":
                    case "h":
                        Velocity.horizontal.setValueRaw(value);
                        break;
                    case "vertical":
                    case "v":
                        Velocity.vertical.setValueRaw(value);
                        break;
                    default:
                        print(invSyn, 1);
                        return;
                }

                print("&aSet " + args[1] + " velocity to ", 1);
                print(args[2], 0);
            } else if (cm.startsWith("ping")) {
                Ping.checkPing();
            } else if (cm.startsWith("clear")) {
                rs.clear();
            } else if (cm.startsWith("hide")) {
                if (!hasArgs) {
                    print(invSyn, 1);
                    return;
                }

                if (args.length != 2) {
                    print(invSyn, 1);
                    return;
                }

                for (Module module : Raven.getModuleManager().getModules()) {
                    String name = module.getName().toLowerCase().replace(" ", "");
                    if (name.equals(args[1].toLowerCase())) {
                        module.setHidden(true);
                        print("&a" + module.getName() + " is now hidden in HUD", 1);
                    }
                }
            } else if (cm.startsWith("show")) {
                if (!hasArgs) {
                    print(invSyn, 1);
                    return;
                }

                if (args.length != 2) {
                    print(invSyn, 1);
                    return;
                }

                for (Module module : Raven.getModuleManager().getModules()) {
                    String name = module.getName().toLowerCase().replace(" ", "");
                    if (name.equals(args[1].toLowerCase())) {
                        module.setHidden(false);
                        print("&a" + module.getName() + " is now visible in HUD", 1);
                    }
                }
            } else if (cm.startsWith("friend") || cm.startsWith("f")) {
                if (!hasArgs) {
                    print(invSyn, 1);
                    return;
                }

                if (args.length != 2) {
                    print(invSyn, 1);
                    return;
                }

                if (args[1].equals("clear")) {
                    Utils.friends.clear();
                    print("&aFriends cleared.", 1);
                    return;
                }

                boolean added = Utils.addFriend(args[1]);
                if (added) {
                    print("&aAdded friend: " + args[1], 1);
                } else {
                    print("&aRemoved friend: " + args[1], 1);
                }
            } else if (cm.startsWith("enemy") || cm.startsWith("e")) {
                if (!hasArgs) {
                    print(invSyn, 1);
                    return;
                }

                if (args.length != 2) {
                    print(invSyn, 1);
                    return;
                }

                if (args[1].equals("clear")) {
                    Utils.enemies.clear();
                    print("&aEnemies cleared.", 1);
                    return;
                }

                boolean added = Utils.addEnemy(args[1]);
                if (!added) {
                    print("&aRemoved enemy: " + args[1], 1);
                } else {

                }
            } else if (cm.startsWith("Debug".toLowerCase())) {
                Raven.debugger = !Raven.debugger;
                print("Debug " + (Raven.debugger ? "enabled" : "disabled") + ".", 1);
            } else if (cm.startsWith("profiles") || cm.startsWith("p")) {
                if (!hasArgs) {
                    print("&aAvailable profiles:", 1);
                    if (Raven.profileManager.profiles.isEmpty()) {
                        print("None", 0);
                        return;
                    }
                    for (int i = 0; i < Raven.profileManager.profiles.size(); ++i) {
                        print(i + 1 + ". " + Raven.profileManager.profiles.get(i).getName(), 0);
                    }
                } else if (args[1].equals("save") || args[1].equals("s")) {
                    if (args.length != 3) {
                        print(invSyn, 1);
                        return;
                    }
                    String name = args[2];
                    if (name.length() < 2 || name.length() > 10 || !name.chars().allMatch(Character::isLetterOrDigit)) {
                        print("&cInvalid name.", 1);
                        return;
                    }
                    Raven.profileManager.saveProfile(new Profile(name, 0));
                    print("&aSaved profile:", 1);
                    print(name, 0);
                    Raven.profileManager.loadProfiles();
                } else if (args[1].equals("load") || args[1].equals("l")) {
                    if (args.length != 3) {
                        print(invSyn, 1);
                        return;
                    }
                    String name = args[2];
                    for (Profile profile : Raven.profileManager.profiles) {
                        if (profile.getName().equals(name)) {
                            Raven.profileManager.loadProfile(profile.getName());
                            print("&aLoaded profile:", 1);
                            print(name, 0);
                            if (Settings.sendMessage.isToggled()) {
                                Utils.sendMessage("&7Enabled profile: &b" + name);
                            }
                            return;
                        }
                    }
                    print("&cInvalid profile.", 1);
                } else if (args[1].equals("remove") || args[1].equals("r")) {
                    if (args.length != 3) {
                        print(invSyn, 1);
                        return;
                    }
                    String name = args[2];
                    for (Profile profile : Raven.profileManager.profiles) {
                        if (profile.getName().equals(name)) {
                            Raven.profileManager.deleteProfile(profile.getName());
                            print("&aRemoved profile:", 1);
                            print(name, 0);
                            Raven.profileManager.loadProfiles();
                            return;
                        }
                    }
                    print("&cInvalid profile.", 1);
                }
            } else if (!cm.startsWith("help") && !cm.startsWith("?")) {
                if (cm.startsWith("shoutout")) {
                    print("&eCelebrities:", 1);
                    print("- hevex", 0);
                    print("- jc", 0);
                    print("- mood", 0);
                    print("- charlotte", 0);
                } else {
                    print(invCom + " (" + (cm.length() > 5 ? cm.substring(0, 5) + "..." : cm) + ")", 1);
                }
            } else {
                print("&eAvailable commands:", 1);
                print("1 setkey [key]", 0);
                print("2 friend/enemy [name/clear]", 0);
                print("3 duels [player]", 0);
                print("4 nick [name/reset]", 0);
                print("5 ping", 0);
                print("6 hide/show [module]", 0);
                print("&eProfiles:", 0);
                print("1 profiles", 0);
                print("2 profiles save [profile]", 0);
                print("3 profiles load [profile]", 0);
                print("4 profiles remove [profile]", 0);
                print("&eModule-specific:", 0);
                print("1 cname [name]", 0);
                print("2 " + FakeChat.command + " [msg]", 0);
                print("3 setspeed [fly/bhop/speed] [value]", 0);
                print("4 setvelocity [h/v] [value]", 0);
            }

        }
    }

    public static void print(String m, int t) {
        if (t == 1 || t == 2) {
            rs.add("");
        }

        rs.add(m);
        if (t == 2 || t == 3) {
            rs.add("");
        }

    }

    public static void rc(FontRenderer fr, int h, int w, int s) {
        int x = w - 195;
        int y = h - 130;
        int sY = h - 345;
        int sH = 230;
        GL11.glEnable(3089);
        int mw = w * s;
        GL11.glScissor(0, mc.displayHeight - (sY + sH) * s, mw - (mw < 2 ? 0 : 2), sH * s - 2);
        RenderUtils.db(1000, 1000, ccs);
        rss(fr, rs, x, y);
        GL11.glDisable(3089);
    }

    private static void rss(FontRenderer fr, List<String> rs, int x, int y) {
        if (f) {
            f = false;
            print("Welcome,", 0);
            print("Use \"help\" for help.", 0);
        }

        if (!rs.isEmpty()) {
            for (int i = rs.size() - 1; i >= 0; --i) {
                String s = rs.get(i);
                int c = -1;
                if (s.contains("&a")) {
                    s = s.replace("&a", "");
                    c = Color.green.getRGB();
                } else if (s.contains("&c")) {
                    s = s.replace("&c", "");
                    c = Color.red.getRGB();
                } else if (s.contains("&e")) {
                    s = s.replace("&e", "");
                    c = Color.yellow.getRGB();
                }

                fr.drawString(s, x, y, c);
                y -= fr.FONT_HEIGHT + 5;
            }

        }
    }

    public static void setccs() {
        int val = Utils.getRandom().nextInt(cs.size());
        if (val == lccs) {
            val += val == 3 ? -3 : 1;
        }

        lccs = val;
        ccs = cs.get(val);
    }

    public static void od() {
        Ping.rs();
    }
}
