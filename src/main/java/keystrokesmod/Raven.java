package keystrokesmod;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import keystrokesmod.keystroke.KeySrokeRenderer;
import keystrokesmod.keystroke.KeyStrokeConfigGui;
import keystrokesmod.keystroke.keystrokeCommand;
import keystrokesmod.module.Module;
import keystrokesmod.clickgui.ClickGui;
import keystrokesmod.module.ModuleManager;
import keystrokesmod.script.ScriptManager;
import keystrokesmod.utility.*;
import keystrokesmod.utility.clicks.CPSCalculator;
import keystrokesmod.utility.font.FontManager;
import keystrokesmod.utility.i18n.I18nManager;
import keystrokesmod.utility.profile.Profile;
import keystrokesmod.utility.profile.ProfileManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import org.jetbrains.annotations.NotNull;

@Mod(
        modid = "keystrokes",
        name = "KeystrokesMod",
        version = "KMV5",
        acceptedMinecraftVersions = "[1.8.9]"
)
public class Raven {
    public static boolean debugger = false;
    public static Minecraft mc = Minecraft.getMinecraft();
    private static KeySrokeRenderer keySrokeRenderer;
    private static boolean isKeyStrokeConfigGuiToggled;
    private static final ScheduledExecutorService ex = Executors.newScheduledThreadPool(4);
    public static ModuleManager moduleManager;
    public static ClickGui clickGui;
    public static ProfileManager profileManager;
    public static ScriptManager scriptManager;
    public static Profile currentProfile;
    public static BadPacketsHandler badPacketsHandler;
    private boolean loaded = false;

    public Raven() {
        moduleManager = new ModuleManager();
    }

    @EventHandler
    public void init(FMLInitializationEvent ignored) {
        Runtime.getRuntime().addShutdownHook(new Thread(ex::shutdown));
        ClientCommandHandler.instance.registerCommand(new keystrokeCommand());
        FMLCommonHandler.instance().bus().register(this);
        FMLCommonHandler.instance().bus().register(new DebugInfoRenderer());
        FMLCommonHandler.instance().bus().register(new CPSCalculator());
        FMLCommonHandler.instance().bus().register(new KeySrokeRenderer());
        FMLCommonHandler.instance().bus().register(new Ping());
        FMLCommonHandler.instance().bus().register(badPacketsHandler = new BadPacketsHandler());
        Reflection.getFields();
        Reflection.getMethods();
        FontManager.init();
        moduleManager.register();
        scriptManager = new ScriptManager();
        keySrokeRenderer = new KeySrokeRenderer();
        clickGui = new ClickGui();
        profileManager = new ProfileManager();
        profileManager.loadProfiles();
        profileManager.loadProfile();
        Reflection.setKeyBindings();
        scriptManager.loadScripts();
        scriptManager.loadScripts();
        FMLCommonHandler.instance().bus().register(ModuleManager.tower);
        FMLCommonHandler.instance().bus().register(ModuleManager.rotationHandler);
        FMLCommonHandler.instance().bus().register(ModuleManager.slotHandler);
        FMLCommonHandler.instance().bus().register(ModuleManager.dynamicManager);

        I18nManager.init();
    }

    @SubscribeEvent
    public void onTick(@NotNull ClientTickEvent e) {
        if (e.phase == Phase.END) {
            try {
                if (Utils.nullCheck()) {
                    if (Reflection.sendMessage) {
                        Utils.sendMessage("&cThere was an error, relaunch the game.");
                        Reflection.sendMessage = false;
                    }
                    for (Module module : getModuleManager().getModules()) {
                        if (mc.currentScreen == null && module.canBeEnabled()) {
                            module.keybind();
                        } else if (mc.currentScreen instanceof ClickGui) {
                            module.guiUpdate();
                        }

                        if (module.isEnabled()) {
                            module.onUpdate();
                        }
                    }
                    synchronized (Raven.profileManager.profiles) {
                        for (Profile profile : Raven.profileManager.profiles) {
                            if (mc.currentScreen == null) {
                                profile.getModule().keybind();
                            }
                        }
                    }
                    for (Module module : Raven.scriptManager.scripts.values()) {
                        if (mc.currentScreen == null) {
                            module.keybind();
                        }
                    }
                }

                if (isKeyStrokeConfigGuiToggled) {
                    isKeyStrokeConfigGuiToggled = false;
                    mc.displayGuiScreen(new KeyStrokeConfigGui());
                }
            } catch (Exception ignored) {
            }
        }
    }

    public static ModuleManager getModuleManager() {
        return moduleManager;
    }

    public static ScheduledExecutorService getExecutor() {
        return ex;
    }

    public static KeySrokeRenderer getKeyStrokeRenderer() {
        return keySrokeRenderer;
    }

    public static void toggleKeyStrokeConfigGui() {
        isKeyStrokeConfigGuiToggled = true;
    }
}
