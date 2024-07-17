package keystrokesmod.module.setting.utils;

import keystrokesmod.module.setting.interfaces.InputSetting;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class ModeOnly implements Supplier<Boolean> {
    private final InputSetting mode;
    private final Set<Double> activeMode;

    public ModeOnly(@NotNull InputSetting mode, double @NotNull ... activeMode) {
        this.mode = mode;
        this.activeMode = new HashSet<>();
        for (double i : activeMode) {
            this.activeMode.add(i);
        }
    }
    public ModeOnly(@NotNull InputSetting mode, @NotNull List<Double> activeMode) {
        this.mode = mode;
        this.activeMode = new HashSet<>(activeMode);
    }

    @Override
    public Boolean get() {
        return activeMode.contains(mode.getInput());
    }

    public ModeOnly reserve() {
        double max = mode.getMax();

        List<Double> options = new ArrayList<>((int) (max + 1 - activeMode.size()));
        for (double i = 0; i <= max; i++) {
            if (!activeMode.contains(i)) {
                options.add(i);
            }
        }
        return new ModeOnly(mode, options);
    }
}