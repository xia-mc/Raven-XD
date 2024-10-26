package keystrokesmod.utility.i18n;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import keystrokesmod.Raven;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.Setting;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.SliderSetting;
import keystrokesmod.utility.i18n.settings.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class I18nManager {
    public static final String[] LANGUAGE_LIST = new String[]{"简体中文", "Deutsch", "UwU", "French", "Español"};

    private static boolean loaded = false;
    public static final List<Map<Module, I18nModule>> MODULE_MAP = new ArrayList<>(LANGUAGE_LIST.length);
    public static final List<Map<String, String>> REPLACE_MAP = new ArrayList<>(LANGUAGE_LIST.length);

    /**
     * call after load all modules
     */
    public static void init() {
        if (loaded) return;
        loaded = true;

        for (String s : LANGUAGE_LIST) {
            Map<Module, I18nModule> moduleMap = new Object2ObjectOpenHashMap<>();
            Map<String, String> replaceMap = new Object2ObjectOpenHashMap<>();

            try (InputStream stream = Objects.requireNonNull(Raven.class.getResourceAsStream("/assets/keystrokesmod/i18n/" + s + ".json"))) {
                JsonObject jsonObject = getJsonObject(stream);

                if (jsonObject.has("modules")) {
                    JsonObject modulesObject = jsonObject.getAsJsonObject("modules");

                    for (Module module : Raven.getModuleManager().getModules()) {
                        if (modulesObject.has(module.getName())) {
                            moduleMap.put(module, getI18nModule(module, modulesObject.getAsJsonObject(module.getName())));
                        }
                    }
                }

                if (jsonObject.has("replace")) {
                    JsonObject replaceObject = jsonObject.getAsJsonObject("replace");

                    for (Map.Entry<String, JsonElement> entry : replaceObject.entrySet()) {
                        replaceMap.put(entry.getKey(), entry.getValue().getAsString());
                    }
                }

            } catch (IOException ignored) {
            }

            MODULE_MAP.add(moduleMap);
            REPLACE_MAP.add(replaceMap);
        }
    }

    @Contract("_, _ -> new")
    private static @NotNull I18nModule getI18nModule(@NotNull Module module, @NotNull JsonObject moduleObject) {
        String name = module.getName();
        String toolTip = module.getToolTip();
        Map<Setting, I18nSetting> settings = new HashMap<>();

        if (moduleObject.has("name"))
            name = moduleObject.get("name").getAsString();
        if (moduleObject.has("toolTip"))
            toolTip = moduleObject.get("toolTip").getAsString();
        if (moduleObject.has("settings")) {
            JsonObject settingsObject = moduleObject.getAsJsonObject("settings");

            for (Setting setting : module.getSettings()) {
                if (settingsObject.has(setting.getName())) {
                    I18nSetting i18nSetting = getI18nSetting(setting, settingsObject.getAsJsonObject(setting.getName()));
                    settings.put(setting, i18nSetting);
                }
            }
        }

        return new I18nModule(name, toolTip, settings);
    }

    @Contract("_, _ -> new")
    private static @NotNull I18nSetting getI18nSetting(@NotNull Setting setting, @NotNull JsonObject settingObject) {
        String toolTip = setting.getToolTip();

        if (settingObject.has("toolTip"))
            toolTip = settingObject.get("toolTip").getAsString();

        if (setting instanceof ButtonSetting) {
            String name = setting.getName();

            if (settingObject.has("name"))
                name = settingObject.get("name").getAsString();

            return new I18nButtonSetting(toolTip, name);
        } else if (setting instanceof DescriptionSetting) {
            String desc = ((DescriptionSetting) setting).getDesc();

            if (settingObject.has("desc"))
                desc = settingObject.get("desc").getAsString();

            return new I18nDescriptionSetting(toolTip, desc);
        } else if (setting instanceof ModeSetting) {
            String settingName = setting.getName();
            String[] options = ((ModeSetting) setting).getOptions().clone();

            if (settingObject.has("settingName"))
                settingName = settingObject.get("settingName").getAsString();
            if (settingObject.has("options")) {
                JsonArray optionsObject = settingObject.getAsJsonArray("options");
                for (int i = 0; i < optionsObject.size(); i++) {
                    options[i] = optionsObject.get(i).getAsString();
                }
            }

            return new I18nModeSetting(toolTip, settingName, options);
        } else if (setting instanceof SliderSetting) {
            String settingName = setting.getName();
            String settingInfo = ((SliderSetting) setting).getInfo();

            if (settingObject.has("settingName"))
                settingName = settingObject.get("settingName").getAsString();
            if (settingObject.has("settingInfo"))
                settingInfo = settingObject.get("settingInfo").getAsString();

            return new I18nSliderSetting(toolTip, settingName, settingInfo);
        } else {
            return new I18nSetting(toolTip);
        }
    }

    private static JsonObject getJsonObject(InputStream inputStream) {
        StringBuilder sb = new StringBuilder();
        List<String> file = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines()
                .collect(Collectors.toList());

        for (int i = 0; i < file.size() - 1; i++) {
            file.set(i, file.get(i) + '\n');
        }

        file.forEach(sb::append);

        return new Gson().fromJson(sb.toString(), JsonObject.class);
    }
}
