package keystrokesmod.utility;

import com.mojang.realmsclient.gui.ChatFormatting;
import keystrokesmod.Raven;
import keystrokesmod.clickgui.ClickGui;
import keystrokesmod.module.Module;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.module.impl.client.Settings;
import keystrokesmod.module.impl.fun.NoteBot;
import keystrokesmod.module.impl.minigames.DuelsStats;
import keystrokesmod.module.impl.other.FakeChat;
import keystrokesmod.module.impl.other.KillMessage;
import keystrokesmod.module.impl.other.NameHider;
import keystrokesmod.module.impl.render.Watermark;
import keystrokesmod.utility.font.IFont;
import keystrokesmod.utility.profile.Profile;
import keystrokesmod.utility.render.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.network.play.client.C01PacketChatMessage;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Commands {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static boolean f = true;
    private static final List<Integer> cs = Arrays.asList((new Color(170, 107, 148, 50)).getRGB(), (new Color(122, 158, 134, 50)).getRGB(), (new Color(16, 16, 16, 50)).getRGB(), (new Color(64, 114, 148, 50)).getRGB());
    private static int ccs = 0;
    private static int lccs = -1;
    public static List<String> rs = new ArrayList<>();
    private static final String invSyn = "&cInvalid syntax.";
    private static final String invCom = "&cInvalid command.";

    public static void rCMD(@NotNull String c) {
        if (!c.isEmpty()) {
            String cm = c.toLowerCase();
            List<String> args = Arrays.asList(c.split(" "));  // maybe bug
            boolean hasArgs = args.size() > 1;
            String n;
            String firstArg = args.get(0).toLowerCase();
            
            if (firstArg.equals("setkey")) {
                if (!hasArgs) {
                    print(invSyn, 1);
                    return;
                }

                if (args.size() != 2) {
                    print(invSyn, 1);
                    return;
                }

                print("Setting...", 1);
                n = args.get(1);
                Raven.getExecutor().execute(() -> {
                    if (URLUtils.isHypixelKeyValid(n)) {
                        URLUtils.k = n;
                        print("&a" + "success!", 0);
                    } else {
                        print("&c" + "Invalid key.", 0);
                    }

                });
            } else if (firstArg.equals("nick")) {
                if (!hasArgs) {
                    print(invSyn, 1);
                    return;
                }

                if (args.size() != 2) {
                    print(invSyn, 1);
                    return;
                }

                if (args.get(1).equals("reset")) {
                    print("&aNick reset.", 1);
                    return;
                }

                DuelsStats.nick = args.get(1);
                print("&aNick has been set to:", 1);
                print("\"" + DuelsStats.nick + "\"", 0);
            } else if (firstArg.equals("cname")) {
                if (!hasArgs) {
                    print(invSyn, 1);
                    return;
                }

                NameHider.n = args.get(1).replace("&", "§");

                for (int i = 2; i < args.size(); i++) {
                    NameHider.n += " "+args.get(i).replace("&", "§");
                }


                print("&a" + Utils.uf("name") + "Nick has been set to:".substring(4), 1);
                print("\"" + NameHider.n + "\"", 0);
            } else if (firstArg.equals(FakeChat.command)) {
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
            } else if (firstArg.equals("duels")) {
                if (!hasArgs) {
                    print(invSyn, 1);
                    return;
                }

                if (args.size() != 2) {
                    print(invSyn, 1);
                    return;
                }

                if (URLUtils.k.isEmpty()) {
                    print("&cAPI Key is empty!", 1);
                    print("Use \"setkey [api_key)\".", 0);
                    return;
                }

                n = args.get(1);
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
            } else if (firstArg.equals("ping")) {
                Ping.checkPing();
            } else if (firstArg.equals("clear")) {
                rs.clear();
            } else if (firstArg.equals("hide")) {
                if (!hasArgs) {
                    print(invSyn, 1);
                    return;
                }

                if (args.size() != 2) {
                    print(invSyn, 1);
                    return;
                }

                for (Module module : Raven.getModuleManager().getModules()) {
                    String name = module.getName().toLowerCase().replace(" ", "");
                    if (name.equals(args.get(1).toLowerCase())) {
                        module.setHidden(true);
                        print("&a" + module.getName() + " is now hidden in HUD", 1);
                    }
                }
            } else if (firstArg.equals("show")) {
                if (!hasArgs) {
                    print(invSyn, 1);
                    return;
                }

                if (args.size() != 2) {
                    print(invSyn, 1);
                    return;
                }

                for (Module module : Raven.getModuleManager().getModules()) {
                    String name = module.getName().toLowerCase().replace(" ", "");
                    if (name.equals(args.get(1).toLowerCase())) {
                        module.setHidden(false);
                        print("&a" + module.getName() + " is now visible in HUD", 1);
                    }
                }
            } else if (firstArg.equals("panic")) {
                List<Module> modulesToDisable = new ArrayList<>();
                for (Module m : Raven.getModuleManager().getModules()) {
                    if (m.isEnabled()) {
                        modulesToDisable.add(m);
                    }
                }
                for (Module m : modulesToDisable) {
                    m.disable();

                }
            }else if (firstArg.equals("rename")) {
                if (!hasArgs) {
                    print(invSyn, 1);
                    return;
                }

                if (args.size() != 3 && args.size() != 4) {
                    print(invSyn, 1);
                    return;
                }

                for (Module module : Raven.getModuleManager().getModules()) {
                    String name = module.getName().toLowerCase().replace(" ", "");
                    if (name.equals(args.get(1).toLowerCase())) {
                        if (args.size() == 3) {
                            module.setPrettyName(args.get(2));
                            print("&a" + module.getName() + " is now called " + module.getRawPrettyName(), 1);
                        } else {
                            module.setPrettyName(args.get(2));
                            module.setPrettyInfo(args.get(3));
                            print("&a'" + module.getName() + " " + module.getInfo() + "' is now called '" + module.getRawPrettyName() + " " + module.getRawPrettyInfo() + "'", 1);
                        }
                    }
                }
            } else if (firstArg.equals("resetgui")) {
                ClickGui.resetPosition();
                print(ChatFormatting.GREEN + "Reset ClickGUI position!", 1);
            } else if (firstArg.equals("say")) {
                if (!hasArgs) {
                    print(invSyn, 1);
                    return;
                }

                PacketUtils.sendPacketNoEvent(new C01PacketChatMessage(c.substring(firstArg.length() + 1)));
            } else if (firstArg.equals("clientname")) {
                if (!hasArgs) {
                    print(invSyn, 1);
                    return;
                }

                String s = c.substring(11);
                s = s.replace('&', '§');
                Watermark.customName = s;
                print("&aSet client name to " + Watermark.customName, 1);
            } else if (firstArg.equals("killmessage")) {
                if (!hasArgs) {
                    print(invSyn, 1);
                    return;
                }

                KillMessage.killMessage = c.substring(12);
                print("&aSet killmessage to " + KillMessage.killMessage, 1);
            } else if (firstArg.equals("binds")) {
                for (Module module : Raven.getModuleManager().getModules()) {
                    if (module.getKeycode() != 0) {
                        print(ChatFormatting.AQUA + module.getName() + ": " + Utils.getKeyName(module.getKeycode()), 1);
                    }
                }
            } else if (firstArg.equals("bind")) {
                if (!hasArgs) {
                    print(invSyn, 1);
                    return;
                }

                if (args.size() != 3) {
                    print(invSyn, 1);
                    return;
                }

                Module targetModule = null;
                for (Module module : Raven.getModuleManager().getModules()) {
                    if (Objects.equals(module.getPrettyName(), args.get(1))) {
                        targetModule = module;
                        break;
                    }
                }
                if (targetModule == null) {
                    print(ChatFormatting.RED + "Module '" + ChatFormatting.RESET + args.get(1) + ChatFormatting.RED + "' is not found.", 1);
                    return;
                }

                int keyCode = Utils.getKeyCode(args.get(2));
                if (keyCode == Keyboard.KEY_NONE) {
                    print(ChatFormatting.RED + "Key '" + ChatFormatting.RESET + args.get(2) + ChatFormatting.RED + "' is invalid.", 1);
                    return;
                }

                targetModule.setBind(keyCode);
                print(ChatFormatting.GREEN + "Bind '" + ChatFormatting.RESET + args.get(2) + ChatFormatting.GREEN + "' to " + targetModule.getPrettyName() + ".", 1);
            } else if (firstArg.equals("notebot")) {
                if (!hasArgs) {
                    print(invSyn, 1);
                    return;
                }

                NoteBot.fileName = c.substring(8);
                print("&aSet noteBot file to " + NoteBot.fileName, 1);
            } else if (firstArg.equals("friend") || firstArg.equals("f")) {
                if (!hasArgs) {
                    print(invSyn, 1);
                    return;
                }

                if (args.size() != 2) {
                    print(invSyn, 1);
                    return;
                }

                if (args.get(1).equals("clear")) {
                    Utils.friends.clear();
                    print("&aFriends cleared.", 1);
                    return;
                }

                boolean added = Utils.addFriend(args.get(1));
                if (added) {
                    print("&aAdded friend: " + args.get(1), 1);
                } else {
                    print("&aRemoved friend: " + args.get(1), 1);
                }
            } else if (firstArg.equals("enemy") || firstArg.equals("e")) {
                if (!hasArgs) {
                    print(invSyn, 1);
                    return;
                }

                if (args.size() != 2) {
                    print(invSyn, 1);
                    return;
                }

                if (args.get(1).equals("clear")) {
                    Utils.enemies.clear();
                    print("&aEnemies cleared.", 1);
                    return;
                }

                boolean added = Utils.addEnemy(args.get(1));
                if (!added) {
                    print("&aRemoved enemy: " + args.get(1), 1);
                }
            } else if (firstArg.equals("Debug".toLowerCase())) {
                Raven.debugger = !Raven.debugger;
                print("Debug " + (Raven.debugger ? "enabled" : "disabled") + ".", 1);
            } else if (firstArg.equals("profiles") || firstArg.equals("p")) {
                if (!hasArgs) {
                    print("&aAvailable profiles:", 1);
                    if (Raven.profileManager.profiles.isEmpty()) {
                        print("None", 0);
                        return;
                    }
                    for (int i = 0; i < Raven.profileManager.profiles.size(); ++i) {
                        print(i + 1 + ". " + Raven.profileManager.profiles.get(i).getName(), 0);
                    }
                } else if (args.get(1).equals("save") || args.get(1).equals("s")) {
                    if (args.size() != 3) {
                        print(invSyn, 1);
                        return;
                    }
                    String name = args.get(2);
                    if (name.length() < 2 || name.length() > 10 || !name.chars().allMatch(Character::isLetterOrDigit)) {
                        print("&cInvalid name.", 1);
                        return;
                    }
                    Raven.profileManager.saveProfile(new Profile(name, 0));
                    print("&aSaved profile:", 1);
                    print(name, 0);
                    Raven.profileManager.loadProfiles();
                } else if (args.get(1).equals("load") || args.get(1).equals("l")) {
                    if (args.size() != 3) {
                        print(invSyn, 1);
                        return;
                    }
                    String name = args.get(2);
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
                } else if (args.get(1).equals("remove") || args.get(1).equals("r")) {
                    if (args.size() != 3) {
                        print(invSyn, 1);
                        return;
                    }
                    String name = args.get(2);
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
            } else if (!firstArg.equals("help") && !firstArg.equals("?")) {
                if (firstArg.equals("shoutout")) {
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
                print("7 rename [module] [name] <info>", 0);
                print("8 say [message]", 0);
                print("9 panic", 0);
                print("10 resetGUI", 0);
                print("&eProfiles:", 0);
                print("1 profiles", 0);
                print("2 profiles save [profile]", 0);
                print("3 profiles load [profile]", 0);
                print("4 profiles remove [profile]", 0);
                print("5 binds", 0);
                print("6 bind [module] [key]", 0);
                print("&eModule-specific:", 0);
                print("1 cname [name]", 0);
                print("2 " + FakeChat.command + " [msg]", 0);
                print("4 killmessage [message]", 0);
                print(String.format("5 clientname [name (current is '%s')]", Watermark.customName), 0);
            }

        }
    }

    public static void print(String m, int t) {
        if (ModuleManager.commandChat.isEnabled() && (mc.currentScreen instanceof GuiChat || mc.currentScreen == null)) {
            if (t == 1 || t == 2) {
                Utils.sendRawMessage("");
            }
            Utils.sendMessage(m);
            if (t == 2 || t == 3) {
                Utils.sendRawMessage("");
            }
        } else {
            if (t == 1 || t == 2) {
                rs.add("");
            }
            rs.add(m);
            if (t == 2 || t == 3) {
                rs.add("");
            }
        }
    }

    public static void rc(IFont fr, int h, int w, int s) {
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

    private static void rss(IFont fr, List<String> rs, int x, int y) {
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
                y -= (int) Math.round(fr.height() + 5);
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
