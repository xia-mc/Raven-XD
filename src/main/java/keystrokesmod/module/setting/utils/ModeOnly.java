package keystrokesmod.module.setting.utils;

import keystrokesmod.module.setting.impl.ModeSetting;
import keystrokesmod.module.setting.impl.ModeValue;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class ModeOnly implements Supplier<Boolean> {
    private final Object mode;
    private final Set<Integer> activeMode;

    public ModeOnly(@NotNull Object mode, int @NotNull ... activeMode) {
        if (!(mode instanceof ModeSetting || mode instanceof ModeValue)) {
            throw new IllegalArgumentException("Mode must be an instance of ModeSetting or ModeValue");
        }
        this.mode = mode;
        this.activeMode = new HashSet<>();
        for (int i : activeMode) {
            this.activeMode.add(i);
        }
    }
    public ModeOnly(@NotNull Object mode, @NotNull List<Integer> activeMode) {
        if (!(mode instanceof ModeSetting || mode instanceof ModeValue)) {
            throw new IllegalArgumentException("Mode must be an instance of ModeSetting or ModeValue");
        }
        this.mode = mode;
        this.activeMode = new HashSet<>(activeMode);
    }

    @Override
    public Boolean get() {
        if (mode instanceof ModeSetting) {
            return activeMode.contains((int) ((ModeSetting) mode).getInput());
        } else if (mode instanceof ModeValue) {
            return activeMode.contains((int) ((ModeValue) mode).getInput());
        }
        return false;
    }

    public ModeOnly reserve() {
        int max = 0;
        if (mode instanceof ModeSetting) {
            max = ((ModeSetting) mode).getMax();
        } else if (mode instanceof ModeValue) {
            max = ((ModeValue) mode).getMax();
        }
        List<Integer> options = new ArrayList<>(max + 1 - activeMode.size());
        for (int i = 0; i <= max; i++) {
            if (!activeMode.contains(i)) {
                options.add(i);
            }
        }
        return new ModeOnly(mode, options);
    }
}