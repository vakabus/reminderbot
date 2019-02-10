package cz.vakabus.reminderbot.utils;

import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
public class Pair<E, F> {
    @NotNull E first;
    @NotNull F second;

    public static <E,F> Pair<E,F> of(@NotNull E first, @NotNull F second) {
        return new Pair<>(first, second);
    }
}
