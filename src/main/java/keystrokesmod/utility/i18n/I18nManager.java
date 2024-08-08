package keystrokesmod.utility.i18n;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import keystrokesmod.Raven;
import keystrokesmod.module.Module;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class I18nManager {
    public static final String[] LANGUAGE_LIST = new String[]{"简体中文"};

    private static boolean loaded = false;
    public static final List<Map<Module, I18nModule>> MODULE_MAP = new ArrayList<>(LANGUAGE_LIST.length);


    /**
     * call after load all modules
     */
    public static void init() {
        if (loaded) return;
        loaded = true;

        for (String s : LANGUAGE_LIST) {
            Map<Module, I18nModule> moduleMap = new HashMap<>();

            try (InputStream stream = Objects.requireNonNull(Raven.class.getResourceAsStream("/assets/keystrokesmod/i18n/" + s + ".json"))) {
                JsonObject jsonObject = getJsonObject(stream);

                if (jsonObject.has("modules")) {
                    JsonObject modulesObject = jsonObject.getAsJsonObject("modules");

                    for (Module module : Raven.getModuleManager().getModules()) {
                        if (modulesObject.has(module.getName())) {
                            JsonObject moduleObject = modulesObject.getAsJsonObject(module.getName());

                            String name = module.getName();
                            String toolTip = module.getToolTip();

                            if (moduleObject.has("name"))
                                name = moduleObject.get("name").getAsString();
                            if (moduleObject.has("toolTip"))
                                toolTip = moduleObject.get("toolTip").getAsString();

                            I18nModule i18nModule = new I18nModule(name, toolTip);
                            moduleMap.put(module, i18nModule);
                        }
                    }
                }

            } catch (IOException ignored) {
            }

            MODULE_MAP.add(moduleMap);
        }
    }

    private static JsonObject getJsonObject(InputStream inputStream) {
        StringBuilder sb = new StringBuilder();
        List<String> file = new BufferedReader(new InputStreamReader(inputStream)).lines()
                .collect(Collectors.toList());

        for (int i = 0; i < file.size() - 1; i++) {
            file.set(i, file.get(i) + '\n');
        }

        file.forEach(sb::append);

        return new Gson().fromJson(sb.toString(), JsonObject.class);
    }
}
