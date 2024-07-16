package keystrokesmod.module.impl.other;

import com.google.gson.*;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.clicks.Pattern;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public final class RecordClick extends Module {
    private static File directory;
    private static List<Pattern> LOADED_PATTERNS = new ArrayList<>(Arrays.asList(Pattern.DEFAULT, Pattern.VAPE));
    public static String[] LOADED_PATTERNS_NAMES = new String[]{Pattern.DEFAULT.getName(), Pattern.VAPE.getName()};

    private static ModeSetting currentPattern;
    private int lastPattern = 0;

    private static Pattern pattern = Pattern.DEFAULT;
    private static int index = 0;
    private static long lastClick = -1;

    public RecordClick() {
        super("RecordClick", category.other);
        this.registerSetting(new DescriptionSetting("Manage click patterns."));
        this.registerSetting(new ButtonSetting("Load patterns", RecordClick::loadPatterns));
        this.registerSetting(currentPattern = new ModeSetting("Pattern", LOADED_PATTERNS_NAMES, 0));
        this.canBeEnabled = false;

        directory = new File(mc.mcDataDir + File.separator + "keystrokes", "clickPatterns");
        if (!directory.exists()) {
            boolean success = directory.mkdirs();
            if (!success) {
                System.out.println("There was an issue creating clickPatterns directory.");
            }
        }

        loadPatterns();
    }

    @Override
    public void onUpdate() {
        if (lastPattern != (int) currentPattern.getInput()) {
            reset();
        }
        lastPattern = (int) currentPattern.getInput();
    }

    public static void click() {
        lastClick = System.currentTimeMillis();
        index++;
    }

    public static long getNextClickTime() {
        long currentTime = System.currentTimeMillis();
        if (lastClick == -1) {
            return currentTime;
        }
        long time = lastClick + getDelay(index);
        if (time < currentTime) {
            reset();
            return -1;
        }
        return time;
    }

    private static int getDelay(int index) {
        if (pattern.delays.isEmpty()) return Integer.MAX_VALUE;
        if (pattern.delays.size() <= index) {
            RecordClick.index = 0;
            return pattern.delays.get(0);
        }
        return pattern.delays.get(index);
    }

    public static void reset(int index) {
        Pattern pattern = LOADED_PATTERNS.get((int) currentPattern.getInput());

        if (index >= pattern.delays.size()) {
            RecordClick.index = 0;
        } else {
            RecordClick.index = index;
        }
        lastClick = -1;
        RecordClick.pattern = pattern;
    }

    public static void reset() {
        reset(0);
    }

    public static void loadPatterns() {
        File[] requireNonNull = Objects.requireNonNull(directory.listFiles());

        LOADED_PATTERNS = new ArrayList<>(Arrays.asList(Pattern.DEFAULT, Pattern.VAPE));
        LOADED_PATTERNS_NAMES = new String[requireNonNull.length + 2];
        LOADED_PATTERNS_NAMES[0] = Pattern.DEFAULT.getName();
        LOADED_PATTERNS_NAMES[1] = Pattern.VAPE.getName();

        for (int i = 0, requireNonNullLength = requireNonNull.length; i < requireNonNullLength; i++) {
            File file = requireNonNull[i];

            if (!file.exists() || !file.isFile()) continue;
            if (!file.getName().endsWith(".json")) continue;
            String fileName = file.getName().substring(0, file.getName().length() - 5);

            try (FileReader fileReader = new FileReader(file)) {
                JsonParser jsonParser = new JsonParser();
                JsonObject profileJson = jsonParser.parse(fileReader).getAsJsonObject();

                String name = fileName;
                List<Integer> delays = new ArrayList<>();

                if (profileJson.has("name")) {
                    name = profileJson.get("name").getAsString();
                }
                if (profileJson.has("delays")) {
                    for (JsonElement element : profileJson.get("delays").getAsJsonArray()) {
                        int delay = element.getAsJsonPrimitive().getAsInt();
                        delays.add(delay);
                    }
                }

                LOADED_PATTERNS.add(new Pattern(name, delays));
                LOADED_PATTERNS_NAMES[i + 1] = name;
                Utils.sendMessage("&aLoaded pattern: &b" + name + "&a with &b" + delays.size() + "&a delays.");
            } catch (Exception e) {
                Utils.sendMessage(e.getLocalizedMessage());
                Utils.sendMessage(Arrays.toString(e.getStackTrace()));
                Utils.sendMessage("&cFailed to load pattern: &b" + fileName);
            }
        }
        currentPattern.setOptions(LOADED_PATTERNS_NAMES);
    }

    public static void savePattern(@NotNull Pattern pattern) {
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        for (Integer delay : pattern.delays) {
            if (delay == null) continue;
            jsonArray.add(new JsonPrimitive(delay));
        }

        jsonObject.addProperty("name", pattern.getName());
        jsonObject.add("delays", jsonArray);

        try (FileWriter fileWriter = new FileWriter(new File(directory, pattern.getName() + ".json"))) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(jsonObject, fileWriter);
        } catch (Exception e) {
            Utils.sendMessage("&cFailed to save pattern: &b" + pattern.getName());
        }
    }


}
