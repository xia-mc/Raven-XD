package keystrokesmod.utility.profile;

import com.google.gson.*;
import keystrokesmod.Raven;
import keystrokesmod.clickgui.ClickGui;
import keystrokesmod.clickgui.components.impl.CategoryComponent;
import keystrokesmod.module.Module;
import keystrokesmod.module.impl.render.HUD;
import keystrokesmod.module.setting.Setting;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.script.Manager;
import keystrokesmod.utility.Utils;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ProfileManager {
    public static Minecraft mc = Minecraft.getMinecraft();
    public File directory;
    public List<Profile> profiles = new ArrayList<>();

    public ProfileManager() {
        directory = new File(mc.mcDataDir + File.separator + "keystrokes", "profiles");
        if (!directory.exists()) {
            boolean success = directory.mkdirs();
            if (!success) {
                System.out.println("There was an issue creating profiles directory.");
                return;
            }
        }
        if (directory.listFiles().length == 0) { // if theres no profile in the folder upon launch, create new default profile
            saveProfile(new Profile("default", 0));
        }
    }

    public void saveProfile(Profile profile) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("keybind", profile.getModule().getKeycode());
        JsonArray jsonArray = new JsonArray();
        for (Module module : Raven.moduleManager.getModules()) {
            if (module.ignoreOnSave) {
                continue;
            }
            JsonObject moduleInformation = getJsonObject(module);
            jsonArray.add(moduleInformation);
        }
        if (Raven.scriptManager != null && Raven.scriptManager.scripts != null) {
            for (Module module : Raven.scriptManager.scripts.values()) {
                if (module.ignoreOnSave) {
                    continue;
                }
                JsonObject moduleInformation = getJsonObject(module);
                jsonArray.add(moduleInformation);
            }
        }
        jsonObject.add("modules", jsonArray);
        try (FileWriter fileWriter = new FileWriter(new File(directory, profile.getName() + ".json"))) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(jsonObject, fileWriter);
        } catch (Exception e) {
            failedMessage("save", profile.getName());
            e.printStackTrace();
        }
    }

    private static JsonObject getJsonObject(Module module) {
        JsonObject moduleInformation = new JsonObject();
        moduleInformation.addProperty("name", (module.moduleCategory() == Module.category.scripts && !(module instanceof Manager)) ?  "sc-" + module.getName() :  module.getName());
        if (module.canBeEnabled) {
            moduleInformation.addProperty("enabled", module.isEnabled());
            moduleInformation.addProperty("hidden", module.isHidden());
            moduleInformation.addProperty("keybind", module.getKeycode());
        }
        if (module instanceof HUD) {
            moduleInformation.addProperty("posX", HUD.hudX);
            moduleInformation.addProperty("posY", HUD.hudY);
        }
        for (Setting setting : module.getSettings()) {
            if (setting instanceof ButtonSetting && !((ButtonSetting) setting).isMethodButton) {
                moduleInformation.addProperty(setting.getName(), ((ButtonSetting) setting).isToggled());
            } else if (setting instanceof SliderSetting) {
                moduleInformation.addProperty(setting.getName(), ((SliderSetting) setting).getInput());
            }
        }
        return moduleInformation;
    }

    public void loadProfile(String name) {
        for (File file : getProfileFiles()) {
            if (!file.exists()) {
                failedMessage("load", name);
                System.out.println("Failed to load " + name);
                return;
            }
            if (!file.getName().equals(name + ".json")) {
                continue;
            }
            if (Raven.scriptManager != null) {
                for (Module module : Raven.scriptManager.scripts.values()) {
                    if (module.canBeEnabled()) {
                        module.disable();
                        module.setBind(0);
                    }
                }
            }
            for (Module module : Raven.getModuleManager().getModules()) {
                if (module.canBeEnabled()) {
                    module.disable();
                    module.setBind(0);
                }
            }
            try (FileReader fileReader = new FileReader(file)) {
                JsonParser jsonParser = new JsonParser();
                JsonObject profileJson = jsonParser.parse(fileReader).getAsJsonObject();
                if (profileJson == null) {
                    failedMessage("load", name);
                    return;
                }
                JsonArray modules = profileJson.getAsJsonArray("modules");
                if (modules == null) {
                    failedMessage("load", name);
                    return;
                }
                for (JsonElement moduleJson : modules) {
                    JsonObject moduleInformation = moduleJson.getAsJsonObject();
                    String moduleName = moduleInformation.get("name").getAsString();

                    if (moduleName == null || moduleName.isEmpty()) {
                        continue;
                    }

                    Module module = Raven.moduleManager.getModule(moduleName);
                    if (module == null && moduleName.startsWith("sc-") && Raven.scriptManager != null) {
                        for (Module module1 : Raven.scriptManager.scripts.values()) {
                            if (module1.getName().equals(moduleName.substring(3))) {
                                module = module1;
                            }
                        }
                    }

                    if (module == null) {
                        continue;
                    }

                    if (module.canBeEnabled()) {
                        if (moduleInformation.has("enabled")) {
                            boolean enabled = moduleInformation.get("enabled").getAsBoolean();
                            if (enabled) {
                                module.enable();
                            } else {
                                module.disable();
                            }
                        }
                        if (moduleInformation.has("hidden")) {
                            boolean hidden = moduleInformation.get("hidden").getAsBoolean();
                            module.setHidden(hidden);
                        }
                        if (moduleInformation.has("keybind")) {
                            int keybind = moduleInformation.get("keybind").getAsInt();
                            module.setBind(keybind);
                        }
                    }

                    if (module.getName().equals("HUD")) {
                        if (moduleInformation.has("posX")) {
                            int hudX = moduleInformation.get("posX").getAsInt();
                            HUD.hudX = hudX;
                        }
                        if (moduleInformation.has("posY")) {
                            int hudY = moduleInformation.get("posY").getAsInt();
                            HUD.hudY = hudY;
                        }
                    }

                    for (Setting setting : module.getSettings()) {
                        setting.loadProfile(moduleInformation);
                    }

                    Raven.currentProfile = getProfile(name);
                }
            } catch (Exception e) {
                failedMessage("load", name);
                e.printStackTrace();
            }
        }
    }

    public void deleteProfile(String name) {
        Iterator<Profile> iterator = profiles.iterator();
        while (iterator.hasNext()) {
            Profile profile = iterator.next();
            if (profile.getName().equals(name)) {
                iterator.remove();
            }
        }
        if (directory.exists()) {
            File[] files = directory.listFiles();
            for (File file : files) {
                if (file.getName().equals(name + ".json")) {
                    file.delete();
                }
            }
        }
    }

    public void loadProfiles() {
        profiles.clear();
        if (directory.exists()) {
            File[] files = directory.listFiles();
            for (File file : files) {
                try (FileReader fileReader = new FileReader(file)) {
                    JsonParser jsonParser = new JsonParser();
                    JsonObject profileJson = jsonParser.parse(fileReader).getAsJsonObject();
                    String profileName = file.getName().replace(".json", "");

                    if (profileJson == null) {
                        failedMessage("load", profileName);
                        return;
                    }

                    int keybind = 0;

                    if (profileJson.has("keybind")) {
                        keybind = profileJson.get("keybind").getAsInt();
                    }

                    Profile profile = new Profile(profileName, keybind);
                    profiles.add(profile);
                } catch (Exception e) {
                    Utils.sendMessage("&cFailed to load profiles.");
                    e.printStackTrace();
                }
            }

            for (CategoryComponent categoryComponent : ClickGui.categories) {
                if (categoryComponent.categoryName == Module.category.profiles) {
                    categoryComponent.reloadModules(true);
                }
            }
            Utils.sendMessage("&b" + Raven.profileManager.getProfileFiles().size() + " &7profiles loaded.");
        }
    }

    public List<File> getProfileFiles() {
        List<File> profileFiles = new ArrayList<>();
        if (directory.exists()) {
            File[] files = directory.listFiles();
            for (File file : files) {
                if (!file.getName().endsWith(".json")) {
                    continue;
                }
                profileFiles.add(file);
            }
        }
        return profileFiles;
    }

    public Profile getProfile(String name) {
        for (Profile profile : profiles) {
            if (profile.getName().equals(name)) {
                return profile;
            }
        }
        return null;
    }

    public void failedMessage(String reason, String name) {
        Utils.sendMessage("&cFailed to " + reason + ": &b" + name);
    }
}
