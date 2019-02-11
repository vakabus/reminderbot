package cz.vakabus.reminderbot.utils;

import lombok.NonNull;
import lombok.Value;

@Value
public class Pair<E, F> {
    @NonNull E first;
    @NonNull F second;

    public static <E,F> Pair<E,F> of(@NonNull E first, @NonNull F second) {
        return new Pair<>(first, second);
    }
}
