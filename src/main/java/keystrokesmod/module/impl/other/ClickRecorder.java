package keystrokesmod.module.impl.other;

import com.mojang.realmsclient.gui.ChatFormatting;
import keystrokesmod.module.Module;
import keystrokesmod.module.setting.impl.ButtonSetting;
import keystrokesmod.module.setting.impl.DescriptionSetting;
import keystrokesmod.utility.Utils;
import keystrokesmod.utility.clicks.Pattern;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClickRecorder extends Module {
    private final ButtonSetting showMessage;
    private final List<Integer> delays = new ArrayList<>();
    private long lastClick = -1;

    public ClickRecorder() {
        super("ClickRecorder", category.other);
        this.registerSetting(new DescriptionSetting("Record your clicks, then you can use it in some modules."));
        this.registerSetting(showMessage = new ButtonSetting("Show message", true));
    }

    @Override
    public void onEnable() {
        Utils.sendMessage("Start record clicks.");
    }

    @Override
    public void onDisable() {
        if (delays.isEmpty()) {
            Utils.sendMessage(ChatFormatting.RED + "Failed: No valid clicks.");
        } else {
            final List<String> patterns = Arrays.asList(RecordClick.LOADED_PATTERNS_NAMES);

            String name = "pattern-";
            for (int i = 1; i <= 100; i++) {
                final String testName = name + i;
                if (patterns.contains(testName)) {
                    continue;
                }
                RecordClick.savePattern(new Pattern(testName, delays));
                Utils.sendMessage("&7Saved pattern: &b" + testName);
                RecordClick.loadPatterns();
                break;
            }
        }

        lastClick = -1;
        delays.clear();
    }

    @SubscribeEvent
    public void onMouseEvent(@NotNull MouseEvent event) {
        long time = System.currentTimeMillis();

        if (!event.buttonstate || mc.currentScreen != null || !Utils.nullCheck()) return;

        if (lastClick != -1) {
            delays.add((int) (time - lastClick));
        } else {
            delays.add(0);
        }
        lastClick = time;

        if (showMessage.isToggled()) {
            Utils.sendMessage("click: " + delays.size() + "  delay: " + delays.get(delays.size() - 1));
        }
    }
}
