package keystrokesmod.module.setting.utils;

import keystrokesmod.module.setting.interfaces.InputSetting;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ModeOnly implements Supplier<Boolean> {
    private final InputSetting mode;
    private final Set<Double> activeMode;

    public ModeOnly(@NotNull InputSetting mode, double @NotNull ... activeMode) {
        this.mode = mode;
        this.activeMode = Arrays.stream(activeMode).boxed().collect(Collectors.toSet());
    }

    public ModeOnly(@NotNull InputSetting mode, @NotNull Collection<Double> activeMode) {
        this.mode = mode;
        this.activeMode = new HashSet<>(activeMode);
    }

    @Override
    public Boolean get() {
        return activeMode.contains(mode.getInput());
    }

    public ModeOnly reserve() {
        int max = (int) mode.getMax();
        List<Double> options = IntStream.rangeClosed(0, max)
                .filter(i -> !activeMode.contains((double) i))
                .mapToObj(i -> (double) i)
                .collect(Collectors.toList());
        return new ModeOnly(mode, options);
    }

    @Contract(pure = true)
    @SafeVarargs
    public final @NotNull Supplier<Boolean> extend(Supplier<Boolean>... suppliers) {
        if (suppliers == null || suppliers.length == 0) {
            return this;
        }
        return () -> this.get() && Arrays.stream(suppliers).allMatch(Supplier::get);
    }

    @Contract("_ -> new")
    public final @NotNull ModeOnly extend(double @NotNull ... activeMode) {
        Set<Double> modes = Arrays.stream(activeMode).boxed().collect(Collectors.toSet());
        modes.addAll(this.activeMode);
        return new ModeOnly(mode, modes);
    }
}